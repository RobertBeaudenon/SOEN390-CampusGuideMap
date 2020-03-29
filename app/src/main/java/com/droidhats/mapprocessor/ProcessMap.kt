package com.droidhats.mapprocessor

import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

class ProcessMap {
    //    private var rectangles: MutableList<Rect> = mutableListOf()
//    private var paths: MutableList<Path> = mutableListOf()
    private var classes: MutableList<MapElement> = mutableListOf()
    private var firstElement: MapElement? = null
    private var finalPath: MutableList<Circle> = mutableListOf()

    fun readSVG() {
        var element: String = ""
        var inRect: Boolean = false
        var inPath: Boolean = false
        var firstElement: Boolean = true

        File("Hall-8.svg").forEachLine {
            if (it.contains("<rect")) {
                inRect = true
            }
            if (inRect) element += it

            if (it.contains("/>") && inRect) {
                if (firstElement) {
                    this.firstElement = createRect(element)
                    firstElement = false
                } else {
                    classes.add(createRect(element))
                }
                inRect = false
                element = ""
            }

            if (it.contains("<path")) {
                inPath = true
            }
            if (inPath) element += it

            if (it.contains("/>") && inPath) {
                val path: Path = createPath(element)
                if (firstElement) {
                    this.firstElement = path
                    firstElement = false
                } else if (path.isClosed) {
                    println(path)
                    classes.add(path)
                }
                inPath = false
                element = ""
            }
        }
    }

    private fun createRect(it: String): Rect {
        val id = extractAttr("id", it)
        val x = extractAttr("x", it).toDouble()
        val y = extractAttr("y", it).toDouble()
        val height = extractAttr("height", it).toDouble()
        val width = extractAttr("width", it).toDouble()
        val style = extractAttr("style", it)
        return Rect(id, x, y, height, width, style)
    }

    private fun createPath(it: String): Path {
        val id = extractAttr("id", it)
        val d = extractAttr("d", it)
        val style = extractAttr("style", it)
        val isClosed = d[d.length - 1] == 'z'
        val transform: String = extractAttr("transform", it)
        println("d: $d")
        return Path(id, d, transform, style, isClosed)
    }

    private fun extractAttr(attribute: String, line: String): String {
        val string: String = " $attribute="
        if (!line.contains(string)) return ""

        var inString: Boolean = false
        var startExtractingString: Boolean = false
        var inAttrString: Boolean = false
        var value: String = ""
        for (i in line.indices){

            if (line[i+1] == '"' && line[i] != '\\') {

                inString = !inString
                if (startExtractingString) {
                    if (inAttrString) break
                    inAttrString = true
                }
                continue
            }

            if ((i + string.length) < line.length && string.equals(line.substring(i, i + string.length)) && !inString) {
                startExtractingString = true
            }

            if (startExtractingString && inString) {
                value += line[i+1]
            }
        }
        return value
    }

    fun writeSVG() {
        val writeFile = File("writeFile7.svg")
        var wrote: Boolean = false
        File("Hall-8.svg").forEachLine { it ->
            if (it.contains("</g>") && !wrote) {
                writeFile.appendText(generatePoints() + "\n")
                wrote = true
            }
            writeFile.appendText(it + "\n")
        }
    }

    fun createText(): String {
        var string: String = ""
        classes.forEach{ it ->
            if (it.toString()[1] == 'p') {

                val center: Pair<Double, Double> = it.getCenter()
                println("center: $center")
                string += Circle(center.first, center.second, 5.0)
                //string += (it as Path).getVertices()
            } else {

                val center: Pair<Double, Double> = it.getCenter()
                string += Circle(center.first, center.second, 5.0)
            }
        }
        return string
    }

    fun coverBackground(): String {
        var string: String = ""
        var circles: MutableList<Circle> = mutableListOf()

        val span = firstElement!!.getWidth()
        val spanHeight = firstElement!!.getHeight()
        for (i in span.first.toInt() until span.second.toInt() step 15) {
            for (y in spanHeight.first.toInt() until spanHeight.second.toInt() step 15) {
                var notInPath = false
                classes.forEach{rect ->
                    if(rect.isWithin(i.toDouble(), y.toDouble())) notInPath = true
                }

                if (notInPath) continue
                circles.add(Circle.getPoint(i.toDouble(), y.toDouble()))
            }
        }

        var i: Int = 0
        while (i < (circles.size - 1)) {
            var circlesList: MutableList<Circle> = mutableListOf()
            var x: Int = 0
            while (x < circles.size - 1) {
                println(circles[x])
                val circle: Circle = circles[x]
                if (circles[i].isWithin(circle.cx, circle.cy, 25.0)) {
                    circlesList.add(circle)
                    circles.removeAt(x)
                }
                x++
            }

            val circlesListSize: Int = circlesList.size
            var z: Int = 0
            while (z < circlesListSize - 1) {
                var y: Int = 0
                while (y < circles.size - 1) {
                    val circle: Circle = circles[y]
                    if(circlesList[z].isWithin(circle.cx, circle.cy, 25.0)) {
                        circlesList.add(circle)
                        circles.removeAt(y)
                    }
                    y++
                }
                z++
            }
            string += findCenter(circlesList)
            i++
        }

//        while (i < (circles.size - 1)) {
//            var circlesList: MutableList<Circle> = mutableListOf()
//            var x: Int = 0
//            while (x < circles.size - 1) {
//                println(circles[x])
//                val circle: Circle = circles[x]
//                if (circles[i].isWithinY(circle.cx, circle.cy, 35.0)) {
//                    circlesList.add(circle)
//                    circles.removeAt(x)
//                }
//                x++
//            }
//            string += findCenter(circlesList)
//            i++
//        }

        return string
    }

    fun generatePoints(): String {
        var string: String = ""
        var pathPoints: MutableList<Circle> = mutableListOf()
        var closestPoints: MutableList<Pair<Pair<Double, Double>, Pair<Double, Double>>?> = mutableListOf()
        val stepSize: Double = 2.0

        classes.forEach{ it ->
            val center = it.getCenter()
            var closestPoint: Pair<Pair<Double, Double>, Pair<Double, Double>>? = null

            var nearestPoints: MutableList<Pair<Pair<Double, Double>, Pair<Double, Double>>?> = mutableListOf()
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, true, false))
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, false, true))
            nearestPoints.add(getNearestPathPoint(center, stepSize, -stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize, true, false))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize,true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, stepSize,true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize,false, true))

            for (point in nearestPoints) {
                if (point != null) {
                    if (closestPoint == null || getDistance(closestPoint.first, center) > getDistance(point.first, center)) {
                        closestPoint = point
                    }
                }
            }

            if(closestPoint != null) {
                pathPoints.add(Circle((closestPoint.first.first + closestPoint.second.first)/2,
                        (closestPoint.first.second + closestPoint.second.second)/2, 5.0))
                closestPoints.add(closestPoint)
            }

        }

        var averageDistance: Double = 0.0
        for (points in closestPoints) {
            averageDistance += getDistance(points!!.first, points.second)
        }
        averageDistance /= closestPoints.size

        for (i in firstElement!!.getWidth().first.toInt() until firstElement!!.getWidth().second.toInt() step (averageDistance/2).toInt()) {
            for (y in firstElement!!.getHeight().first.toInt() until firstElement!!.getHeight().second.toInt() step (averageDistance/2).toInt()) {
                if (inPath(i.toDouble(), y.toDouble()) && notInRange(pathPoints, i.toDouble(), y.toDouble(), averageDistance)) {
                    pathPoints.add(Circle(i.toDouble(), y.toDouble(), 5.0))
                }
            }
        }

        var x: Int = 0
        var y: Int = 0
        var pathPointsSize: Int = pathPoints.size

        while (x < pathPointsSize) {
            var circle1: Circle = pathPoints[x]
            y = 0
            while (y < pathPointsSize) {
                var circle: Circle = pathPoints[y]
                if (circle1.isWithinRange(circle.cx, circle.cy, 20.0) && circle1.cx != circle.cx && circle.cy != circle1.cy) {
                    if (x > y) x = y
                    pathPoints.removeAt(y)
                    pathPointsSize--
                }
                y++
            }
            x++
        }

        for (circle in pathPoints) {
            string += circle
        }

        return string
    }

    fun notInRange(pathPoints: MutableList<Circle>, x: Double, y: Double, averageDistance: Double):Boolean {
        for (circle in pathPoints) {
            if (circle.isWithin(x, y, averageDistance)) return false
        }
        return true
    }

    fun getNearestPathPoint(center: Pair<Double, Double>, stepSizeX: Double, stepSizeY: Double, applyX: Boolean, applyY: Boolean): Pair<Pair<Double, Double>, Pair<Double, Double>>? {
        var navPointX = center.first
        var navPointY = center.second
        var closestPoint: Pair<Double, Double>? = null
        var points: Pair<Pair<Double, Double>, Pair<Double, Double>>? = null
        var pathFound: Boolean = false

        while(isWithinBounds(navPointX, navPointY)) {
            //take step in direction
            if (applyX) navPointX += stepSizeX
            if (applyY) navPointY += stepSizeY

            // if it's in the path return the point as it is the first
            if (inPath(navPointX, navPointY) && !pathFound) {
                closestPoint = Pair(navPointX, navPointY)
                pathFound = true
                continue
            }

            if (!inPath(navPointX, navPointY) && pathFound) {
                if (applyX) navPointX -= stepSizeX
                if (applyY) navPointY -= stepSizeY
                points = Pair(closestPoint!!, Pair(navPointX, navPointY))
                break
            }
        }

        if (closestPoint == null || isWithinBounds(closestPoint.first, closestPoint.second))
            return points
        return null
    }

    fun getDistance(x: Pair<Double, Double>, y: Pair<Double, Double>): Double {
        return sqrt(abs(x.first - y.first).pow(2.0) + abs(x.second - y.second).pow(2.0))

    }

    fun inPath(x: Double, y: Double): Boolean {
        classes.forEach{ it ->
            if (it.isWithin(x, y)) return false
        }
        return true
    }

    fun isWithinBounds(x: Double, y: Double): Boolean {
        if (firstElement!!.isWithin(x, y)) return true
        return false
    }

    fun findCenter(pointList: MutableList<Circle>): Circle {
        var xSum: Double = 0.0
        var ySum: Double = 0.0
        val size = pointList.size

        for (i in 0 until size) {
            xSum += pointList[i].cx
            ySum += pointList[i].cy
        }

        return Circle.getPoint(xSum/size, ySum/size)
    }

}
