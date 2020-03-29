package com.droidhats.mapprocessor

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

abstract class MapElement(){
    abstract override fun toString(): String
    abstract fun isWithin(x: Double, y: Double): Boolean
    abstract fun getWidth(): Pair<Double, Double>
    abstract fun getHeight(): Pair<Double, Double>
    abstract fun getCenter(): Pair<Double, Double>
}

class Rect (val id: String, val x: Double, val y: Double, val height: Double, val width: Double, val style: String) : MapElement() {
    override fun toString(): String {
        return "<rect id=\"$id\" x=\"$x\" y=\"$y\" height=\"$height\" width=\"$width\" style=\"$style\" />"
    }

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

class Path(val id: String, val d: String, val style: String, val isClosed: Boolean) : MapElement() {

    var xMin: Double
    var xMax: Double
    var yMin: Double
    var yMax: Double
    var vertices: MutableList<Pair<Double, Double>> = mutableListOf()

    init {
        if (d[0] == 'm' && isClosed) {
            var anArray = d.substring(2, d.length-2).split(" ")
            println(d)
            var prevVertex = Pair<Double, Double>(0.0, 0.0)
            anArray.forEach{ it ->
                println(it)
                var element = it.split(",")
                println(element)
                var vert = Pair<Double, Double>(element[0].toDouble() + prevVertex.first, element[1].toDouble() + prevVertex.second)
                vertices.add(Pair<Double, Double>(element[0].toDouble() + prevVertex.first, element[1].toDouble() + prevVertex.second))
                prevVertex = vert
            }

            var initialVertex = vertices[0]
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
            xMin = -1.0
            xMax = -1.0
            yMin = -1.0
            yMax = -1.0
        }
    }

    override fun toString(): String {
        return "<path id=\"$id\" d=\"$d\" style=\"$style\"/>"
    }

    override fun isWithin(x: Double, y: Double): Boolean {

        if (xMin < 0 || xMax < 0 || yMin < 0 || yMax < 0) {
            return false
        }

        if (x < xMin + 5 || x > xMax - 5 || y < yMin + 5 || y > yMax - 5) {
            return true
        }
        return false
    }

    override fun getWidth(): Pair<Double, Double> {
        return Pair<Double,Double> (0.0,0.0)
    }

    override fun getHeight(): Pair<Double, Double> {
        return Pair<Double,Double> (0.0,0.0)
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

class Circle(val cx: Double, val cy: Double, val r: Double) : MapElement() {
    override fun toString(): String {
        return "<circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" />"
    }

    companion object {
        fun getPoint(x: Double, y: Double): Circle {
            return Circle(x, y, 5.0)
        }
    }

    override fun isWithin(x: Double, y: Double): Boolean {
        return false
    }

    fun isWithin(x: Double, y: Double, range: Double): Boolean {
        val distance: Double = sqrt(abs(cx - x).pow(2.0) + abs(cy - y).pow(2.0))
        return distance < range
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

}
