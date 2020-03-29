package com.droidhats.mapprocessor

import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

class ProcessMap {
    private var classes: MutableList<MapElement> = mutableListOf()
    private var firstElement: MapElement? = null
    private var finalPath: MutableList<Circle> = mutableListOf()

    fun readSVG(svgFile: String) {
        var element: String = ""
        var inRect: Boolean = false
        var inPath: Boolean = false
        var firstElement: Boolean = true

        File(svgFile).forEachLine {
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
        val writeFile = File("writeFile25.svg")
        var wrote: Boolean = false
        File("Hall-8.svg").forEachLine { it ->
            if (it.contains("</g>") && !wrote) {
                writeFile.appendText(randomPath() + "\n")
                wrote = true
            }
            writeFile.appendText(it + "\n")
        }
    }

    fun identifyClassCenters(): String {
        var string: String = ""
        classes.forEach {
            val center: Pair<Double, Double> = it.getCenter()
            string += Circle(center.first, center.second, 5.0)
        }
        return string
    }

    fun pathPointsToString(pathPoints: MutableList<Circle>): String {
        var string: StringBuilder = StringBuilder()

        for (circle in pathPoints) {
            string.append(circle)
        }
        var nodes = createPaths(pathPoints)
        for (path in nodes) {
            string.append(path.drawAllPaths())
        }

        return string.toString()
    }

    fun randomPath(): String {
        //var list: MutableList<Circle> = generatePoints()
        var list: MutableList<Circle> = generatePointsAcrossMap()

        val start: Int = (Math.random() * classes.size).toInt()
        val end: Int = (Math.random() * classes.size).toInt()
        println("Start: $start")
        println("End: $end")
        return Dijkstra(classes[33], classes[26], createPaths(list))
    }

    fun generatePointsAcrossMap(): MutableList<Circle> {
        var pathPoints: MutableList<Circle> = mutableListOf()

        // scatter points in missing spots, using average distance as a scale for step size
        for (i in firstElement!!.getWidth().first.toInt() until firstElement!!.getWidth().second.toInt() step 20) {
            for (y in firstElement!!.getHeight().first.toInt() until firstElement!!.getHeight().second.toInt() step 20) {
                if (inPath(i.toDouble(), y.toDouble())) {
                    pathPoints.add(Circle(i.toDouble(), y.toDouble(), 5.0))
                }
            }
        }
        return pathPoints
    }

    fun generatePoints(): MutableList<Circle> {

        var pathPoints: MutableList<Circle> = mutableListOf()

        // average distance is used for scaling the distance between points in the next step
        var averageDistance: Double = 0.0
        var totalPoints: Int = 0
        findNearestPointToClasses() { closestPoint: Pair<Pair<Double, Double>, Pair<Double, Double>> ->
            pathPoints.add(Circle((closestPoint.first.first + closestPoint.second.first)/2,
                    (closestPoint.first.second + closestPoint.second.second)/2, 5.0))
            averageDistance += getDistance(closestPoint.first, closestPoint.second)
            totalPoints++
        }
        averageDistance /= totalPoints

        // scatter points in missing spots, using average distance as a scale for step size
        for (i in firstElement!!.getWidth().first.toInt() until firstElement!!.getWidth().second.toInt() step (averageDistance/4).toInt()) {
            for (y in firstElement!!.getHeight().first.toInt() until firstElement!!.getHeight().second.toInt() step (averageDistance/4).toInt()) {
                if (inPath(i.toDouble(), y.toDouble()) && notInRange(pathPoints, i.toDouble(), y.toDouble(), averageDistance)) {
                    pathPoints.add(Circle(i.toDouble(), y.toDouble(), 5.0))
                }
            }
        }

        var x: Int = 0
        var y: Int = 0
        var pathPointsSize: Int = pathPoints.size

        // clean up points that are too close and too many together
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

        return pathPoints
    }

    fun findNearestPointToClasses(doForClosestPoint: (Pair<Pair<Double, Double>, Pair<Double, Double>>) -> Unit) {

        val stepSize: Double = 2.0
        classes.forEach{ it ->
            val center = it.getCenter()
            var closestPoint: Pair<Pair<Double, Double>, Pair<Double, Double>>? = null

            // draw a line in 8 directions (top, left, up, down, top-left, top-right, bottom-left, bottom-right)
            // and get the nearest point and furthest point of the path, if it traverses the path
            var nearestPoints: MutableList<Pair<Pair<Double, Double>, Pair<Double, Double>>?> = mutableListOf()
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, true, false))
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, false, true))
            nearestPoints.add(getNearestPathPoint(center, stepSize, -stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize, true, false))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize,true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, stepSize,true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize,false, true))

            // find the closests point our of all the nearest points
            for (point in nearestPoints) {
                if (point != null) {
                    if (closestPoint == null || getDistance(closestPoint.first, center) > getDistance(point.first, center)) {
                        closestPoint = point
                    }
                }
            }

            // if a closest point was found do stuff
            if(closestPoint != null) {
                doForClosestPoint(closestPoint)
            }
        }
    }

    fun createPaths(pathPoints: MutableList<Circle>): MutableList<Node> {
        var nodeList: MutableList<Node> = mutableListOf()
        var finalList: MutableList<Node> = mutableListOf()

        for(point in pathPoints) {
            nodeList.add(Node(point, mutableListOf()))
        }

        for (pointA in nodeList) {
            var numberOfNeighbors: Int = 0
            for (pointB in nodeList) {
                if (checkPath(pointA.circle, pointB.circle) && !pointA.circle.equals(pointB.circle)) {
                    pointA.neighbors.add(pointB)
                    numberOfNeighbors++
                }
            }
            if (numberOfNeighbors > 0) {
                finalList.add(pointA)
            }
        }

        return finalList
    }

    fun checkPath(pointA: Circle, pointB: Circle): Boolean {
        // making line equation y = mx + b
        val m: Double = (pointA.cy - pointB.cy)/(pointA.cx - pointB.cx)
        val b: Double = pointA.cy - (m*pointA.cx)

        if (abs(pointA.cx - pointB.cx) > abs (pointA.cy - pointB.cy)) {

            val x: Double = if (pointA.cx < pointB.cx) pointA.cx else pointB.cx
            for (step in 10 until abs(pointB.cx - pointA.cx).toInt() step 10) {
                val newX: Double = x + step
                if (!inPath(newX, (m*newX) + b)) {
                    return false
                }
            }
        } else if (abs(pointA.cx - pointB.cx) > 1.0){

            val y: Double = if (pointA.cy < pointB.cy) pointA.cy else pointB.cy
            for (step in 10 until abs(pointB.cy - pointA.cy).toInt() step 10) {
                val newY: Double = y + step
                if (!inPath((newY-b)/m, newY)) {
                    return false
                }
            }
        } else {
            val y: Double = if (pointA.cy < pointB.cy) pointA.cy else pointB.cy
            for (step in 10 until abs(pointB.cy - pointA.cy).toInt() step 10) {
                val newY: Double = y + step
                if (!inPath(pointA.cx, newY)) {
                    return false
                }
            }
        }

        return true
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

fun getDistance(x: Pair<Double, Double>, y: Pair<Double, Double>): Double {
    return sqrt(abs(x.first - y.first).pow(2.0) + abs(x.second - y.second).pow(2.0))
}
