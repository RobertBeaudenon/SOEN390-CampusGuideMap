package com.droidhats.mapprocessor

import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Abstract class for holding any element in the svg map
 */
abstract class MapElement(){

    /**
     * For subclasses to implement toString method
     */
    abstract override fun toString(): String

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

/**
 * This class represents the rect element of the svg file
 */
class Rect (val id: String, val x: Double, val y: Double, val height: Double, val width: Double, val style: String) : MapElement() {
    override fun toString(): String {
        return "<rect id=\"$id\" x=\"$x\" y=\"$y\" height=\"$height\" width=\"$width\" style=\"$style\" />"
    }

    override fun getID() = id

    override fun isWithin(x: Double, y: Double): Boolean {
        return (x > this.x - 5 && x < this.x + width + 5 && y > this.y - 5 && y < this.y + this.height + 5)
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair<Double,Double> (x, x + width)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair<Double,Double> (y, y + height)
    }

    override fun getCenter(): Pair<Double, Double> {
        return Pair<Double, Double> (x + width/2, y + height/2)
    }

}

/**
 * This class represents the path element of the svg file. Since these elements have vertices, we need to
 * iterate through to points to find the max and minimum x and y coordinates
 */
class Path(val id: String, val d: String, var transform: String, val style: String, val isClosed: Boolean) : MapElement() {

    var xMin: Double
    var xMax: Double
    var yMin: Double
    var yMax: Double
    var vertices: MutableList<Pair<Double, Double>> = mutableListOf()
    var matrix: MutableList<Double> = mutableListOf()

    init {
        if (d.isNotEmpty() && d[0] == 'm' && isClosed) {
            // Parsing path
            val anArray = d.substring(2, d.length - 2).split(" ")
            var prevVertex = Pair<Double, Double>(0.0, 0.0)
            anArray.forEach { it ->
                val element = it.split(",")
                val vert = Pair<Double, Double>(element[0].toDouble() + prevVertex.first, element[1].toDouble() + prevVertex.second)
                vertices.add(Pair<Double, Double>(element[0].toDouble() + prevVertex.first, element[1].toDouble() + prevVertex.second))
                prevVertex = vert
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
            return Path("", "m " + pointA.first + "," + pointA.second + " $diffX,$diffY", "", "", false)
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
        return "<path id=\"$id\" d=\"$d\" style=\"$style\"/>"
    }

    override fun isWithin(x: Double, y: Double): Boolean {

        if (xMin < 0 || xMax < 0 || yMin < 0 || yMax < 0) {
            return false
        }

        if (x > xMin - 5 && x < xMax + 5 && y > yMin - 5 && y < yMax + 5) {
            return true
        }
        return false
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair<Double,Double> (xMax, xMin)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair<Double,Double> (yMax,yMin)
    }

    override fun getCenter(): Pair<Double, Double> {
        var xSum: Double = 0.0
        var ySum: Double = 0.0
        for (vertex in vertices) {
            xSum += vertex.first
            ySum += vertex.second
        }
        return Pair(xSum/vertices.size, ySum/vertices.size)
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

    // returns false by default and shouldn't be used
    override fun isWithin(x: Double, y: Double): Boolean {
        return false
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair<Double,Double> (0.0,0.0)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair<Double,Double> (0.0,0.0)
    }

    override fun getCenter(): Pair<Double, Double> {
        return Pair<Double, Double>(cx, cy)
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
class SVG(val id: String, val transportationType: String, val x: Double, val y: Double) {
    fun getLocation(): Pair<Double, Double> = Pair(x, y)
}
