package com.droidhats.mapprocessor

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Double.max
import java.lang.Double.min
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Abstract class for holding any element in the svg map
 */
abstract class MapElement() {

    /**
     * For subclasses to implement toString method
     */
    abstract override fun toString(): String

    /**
     * Method that returns the center of the element
     * @return pair of x and y coordinates of the center
     */
    abstract fun getCenter(): Pair<Double, Double>

    /**
     * Method that return the id of the element
     * @return ID as a string
     */
    abstract fun getID(): String
}

abstract class ClassRoom() : MapElement() {
    /**
     * Method that takes as parameters an x and y coordinate and returns whether it is within this element
     * @param x the x coordinate
     * @param y the y coordinate
     * @return whether the coordinate is within the element
     */
    abstract fun isWithin(x: Double, y: Double): Boolean

    /**
     * Method that returns the leftmost and rightmost x coordinate of the element
     * @return Pair of leftmost and rightmost x coordinate
     */
    abstract fun getWidth(): Pair<Double, Double>

    /**
     * Method that returns the topmost and bottommost y coordinate of the element
     * @return Pair of topmost and bottommost x coordinate
     */
    abstract fun getHeight(): Pair<Double, Double>

}

/**
 * This class represents the rect element of the svg file
 */
class Rect (val id: String, val x: Double, val y: Double, val height: Double, val width: Double, var style: String) : ClassRoom() {
    override fun toString(): String {
        return "<rect id=\"$id\" x=\"$x\" y=\"$y\" height=\"$height\" width=\"$width\" style=\"$style\" />"
    }

    override fun getID() = id

    override fun isWithin(x: Double, y: Double): Boolean {
        return (x > this.x - 5 && x < this.x + width + 5 && y > this.y - 5 && y < this.y + this.height + 5)
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair(x, x + width)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair(y, y + height)
    }

    override fun getCenter(): Pair<Double, Double> {
        return Pair(x + width/2, y + height/2)
    }

}

/**
 * This class represents the path element of the svg file. Since these elements have vertices, we need to
 * iterate through to points to find the max and minimum x and y coordinates
 */
class Path(val id: String, val d: String, var transform: String, var style: String, val isClosed: Boolean) : ClassRoom() {

    var xMin: Double
    var xMax: Double
    var yMin: Double
    var yMax: Double
    var vertices: MutableList<Pair<Double, Double>> = mutableListOf()
    var matrix: MutableList<Double> = mutableListOf()
    private lateinit var theCenter: Pair<Double, Double>

    init {
        if (d.isNotEmpty() && isClosed) {
            // Parsing path
            val anArray = d.substring(2, d.length - 2).split(" ")
            var prevVertex = Pair(0.0, 0.0)
            var c = 0 // if there is a c variable, this will count the number of skips
            var diffMode = true
            if (d[0] == 'M') diffMode = false

            for (it in anArray) {
                // the intention of this block of code is to handle the cubic bezier
                // We handle it by completely ignoring it and cutting a rectangle through the
                // arc that was supposed to be formed.
                if (it == "l") continue
                if (it == "c") {
                    c = 2
                }
                if (c > 0) {
                    c--
                    continue
                }
                if (it == "L") {
                    diffMode = false
                    continue
                }
                if (it == "m") {
                    diffMode = true
                    continue
                }
                val element = it.split(",")
                prevVertex = if (diffMode) {
                    val vert = Pair(element[0].toDouble() + prevVertex.first, element[1].toDouble() + prevVertex.second)
                    vertices.add(vert)
                    vert
                } else {
                    val vert = Pair(element[0].toDouble(), element[1].toDouble())
                    vertices.add(vert)
                    vert
                }
            }

            // transforms (moves) the vertices if there
            if (transform.contains("matrix(")) {
                transformVertices()
            }

            // finding the minimum and maximum values
            val initialVertex = vertices[0]
            xMax = initialVertex.first
            xMin = xMax
            yMax = initialVertex.second
            yMin = yMax

            for (vertex in vertices) {
                if (vertex.first > xMax) xMax = vertex.first
                if (vertex.first < xMin) xMin = vertex.first
                if (vertex.second > yMax) yMax = vertex.second
                if (vertex.second < yMin) yMin = vertex.second
            }
            theCenter = findCenter()
        } else {
            // default because nothing will actually be negative
            xMin = -1.0
            xMax = -1.0
            yMin = -1.0
            yMax = -1.0
        }
    }

    companion object {

        /**
         * For a pair of coordinates, returns a path that can be used to draw
         * @param pointA first coordinate
         * @param pointB second coordinate
         * @return path element connecting 2 points
         */
        fun createPath(pointA: Pair<Double, Double>, pointB: Pair<Double, Double>): Path {
            val diffX: Double = pointB.first - pointA.first
            val diffY: Double = pointB.second - pointA.second
            return Path("", "m " + pointA.first + "," + pointA.second + " $diffX,$diffY", "", "stroke:#000000;stroke-width:2.01184581;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1", false)
        }
    }

    override fun getID() = id

    /**
     * Performs a matrix multiplication on the vertices along the path (essentially produces a movement)
     */
    fun transformVertices() {
        var matrixString = transform.substring(7, transform.length-1).split(',')
        for (elmt in matrixString) {
            matrix.add(elmt.toDouble())
        }

        for (vertex in 0 until vertices.size) {
            vertices[vertex] = transform(vertices[vertex])
        }
    }

    /**
     * Performs the multiplication on the coordinate
     */
    fun transform(coordinate: Pair<Double, Double>):  Pair<Double, Double> {
        val first = matrix[0]*coordinate.first + matrix[2]*coordinate.second + matrix[4]
        val second = matrix[1]*coordinate.first + matrix[3]*coordinate.second + matrix[5]
        return Pair(first, second)
    }

    override fun toString(): String {
        return "<path id=\"$id\" d=\"$d\" style=\"$style\" transform=\"$transform\"/>"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun isWithin(x: Double, y: Double): Boolean {
        var intersections = 0
        for (it in 0 until vertices.size - 1) {
            val x1 = vertices[it].first
            val x2 = vertices[it+1].first
            val line: Line = Line.create(vertices[it], vertices[it + 1])
            if (line.getY(x) >= y && x <= max(x1,x2) && x >= min(x1, x2)) {
                intersections++
            }
        }
        val x1 = vertices[0].first
        val x2 = vertices[vertices.size - 1].first
        val line: Line = Line.create(vertices[0], vertices[vertices.size - 1])
        if (line.getY(x) >= y && x <= max(x1,x2) && x >= min(x1, x2)) {
            intersections++
        }

        return intersections == 1
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair(xMin, xMax)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair(yMin,yMax)
    }

    override fun getCenter(): Pair<Double, Double> {
        return theCenter
    }

    private fun findCenter(): Pair<Double, Double> {
        var xSum = 0.0
        var ySum = 0.0
        for (vertex in vertices) {
            xSum += vertex.first
            ySum += vertex.second
        }
        return Pair(xSum/vertices.size, ySum/vertices.size)
    }
}

/**
 * Used by path object and the following class represents the equation of a line
 */
class Line(private val m: Double, private val b: Double) {
    companion object {
        /**
         * Given 2 points, generates a line equation based on the that
         * @param pointA first point
         * @param pointB second point
         * @return line
         */
        fun create(pointA: Pair<Double, Double>, pointB: Pair<Double, Double>): Line {
            val diffX: Double = (pointB.first - pointA.first)
            val diffY: Double = (pointB.second - pointA.second)
            if (diffX == 0.0) return Line(0.0, 0.0)
            val m = diffY/diffX
            val b = pointA.second - m * pointA.first
            return Line(m, b)
        }
    }

    /**
     * Plug x into equation to get y
     * @param x
     * @return y value for x
     */
    fun getY(x: Double): Double {
        return m*x + b
    }
}

/**
 * Class for the circle element of SVG's. It is often used to make points on the map
 */
class Circle(val cx: Double, val cy: Double, val r: Double) : MapElement() {
    override fun toString(): String {
        return "<circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" />"
    }

    override fun getID() = Random().nextInt().toString()

    companion object {
        /**
         * Creates a point using an x and y coordinate
         * @param x coordinate
         * @param y coordinate
         * @return Circle
         */
        fun getPoint(x: Double, y: Double): Circle {
            return Circle(x, y, 5.0)
        }
    }

    override fun getCenter(): Pair<Double, Double> {
        return Pair(cx, cy)
    }

    fun isWithin(x: Double, y: Double, range: Double): Boolean {
        return (cx.roundToInt() == x.roundToInt() && abs(y - cy) < range) || (cy.roundToInt() == y.roundToInt() && abs(cx - x) < range) //distance < range
    }
    fun isWithinRange(x: Double, y: Double, range: Double): Boolean {
        val distance: Double = sqrt(abs(cx - x).pow(2.0) + abs(cy - y).pow(2.0))

        return distance < range
    }

    /**
     * Returns whether 2 circles are at the same position
     * @param circle 2nd circle object
     * @return whether they have the same position
     */
    fun equals(circle: Circle): Boolean {
        return cx == circle.cx && cy == circle.cy
    }
}

/**
 * SVG element of an svg file. This element will hold the contents of any svg element in the file
 */
class SVG(val id: String, val transportationType: String, val x: Double, val y: Double) : MapElement() {

    override fun toString(): String {
        // not very functional for being put back in the svg, since we don't take in the inner elements
        return id + transportationType + x + y
    }

    override fun getCenter(): Pair<Double, Double> = Pair(x, y)

    override fun getID(): String = id
}
