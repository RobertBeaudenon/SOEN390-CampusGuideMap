package com.droidhats.mapprocessor

import com.droidhats.campuscompass.views.FloorFragment
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Class ProcessMap is where all the parsing of the svg formatted map, and interacts with the other
 * components to generate a path on the map
 */
class ProcessMap {
    // list of classrooms
    private var classes: MutableList<ClassRoom> = mutableListOf()

    fun getClasses() = classes

    // list of indoor transportation modes
    private val indoorTransportations: MutableList<SVG> = mutableListOf()

    fun getIndoorTransportationMethods(): List<SVG> = indoorTransportations

    // list of all elements
    private val allElements: MutableList<MapElement> = mutableListOf()

    fun getAllElements(): List<MapElement> = allElements

    // first element in the map (holds all the other elements within its dimensions
    internal var firstElement: ClassRoom? = null
    private lateinit var stringArray: List<String>

    /**
     * Template method for reading the svg file that provides the ability to react upon finding
     * any of the map elements.
     * It assumes that the svg file in the form of a string is properly formatted with new line
     * characters.
     *
     * @param svgFile the string of the svg file to read
     * @param onEachLine lambda function to execute for each line
     * @param onRectElement lambda function to execute on each rect element
     * @param onPathElement lambda function execute on each path element
     * @param onSvgElement lambda function to execute on each svg element
     */
    private fun readSVG(
        svgFile: String,
        onEachLine: (String) -> Unit,
        onRectElement: (String) -> Unit,
        onPathElement: (String) -> Unit,
        onSvgElement: (String) -> Unit
    ) {
        var element: StringBuilder = StringBuilder()
        var inRect: Boolean = false
        var inPath: Boolean = false
        var firstElement: Boolean = true

        stringArray = svgFile.split("\n")
        var it = 0
        while (it < stringArray.size) {


            if (stringArray[it].contains("<svg") && !firstElement) {
                it = collectElement(it, onSvgElement, onEachLine, "</svg>")
            }

            onEachLine(stringArray[it])

            if (stringArray[it].contains("<rect")) {
                inRect = true
            }
            if (inRect) {
                element.append(stringArray[it])
            }

            if (stringArray[it].contains("/>") && inRect) {
                if (firstElement) {
                    this.firstElement = createRect(element.toString())
                    firstElement = false
                } else {
                    onRectElement(element.toString())
                }
                inRect = false
                element = StringBuilder()
            }

            if (stringArray[it].contains("<path")) {
                inPath = true
            }
            if (inPath) {
                element.append(stringArray[it])
            }

            if (stringArray[it].contains("/>") && inPath) {
                if (firstElement) {
                    this.firstElement = createPath(element.toString())
                    firstElement = false
                } else {
                    onPathElement(element.toString())
                }
                inPath = false
                element = StringBuilder()
            }
            it++
        }
    }

    /**
     * Collects an svg element and returns the iterator at the next iteration
     * @param it current iterator
     * @param onSvgElement function to call on the string of svg attributes
     * @param onEachLine function to call on each line of the svg file
     * @param endString string to define the end of the element
     * @return next iteration after the collected element
     */
    private fun collectElement(
        it: Int,
        onSvgElement: (String) -> Unit,
        onEachLine: (String) -> Unit,
        endString: String
    ): Int {
        var iterator = it
        val stringBuilder: StringBuilder = StringBuilder()
        var retrievedOne: Boolean = false
        while (!stringArray[iterator].contains(endString)) {
            stringBuilder.append(stringArray[iterator])
            onEachLine(stringArray[iterator])
            if (stringArray[iterator].contains(">") && !retrievedOne) {
                retrievedOne = true
                onSvgElement(stringBuilder.toString())
            }
            iterator++
        }
        return iterator
    }


    /**
     * This method takes as input an svg file in the form of a string and digests the elements into
     * classes and points of interest.
     * @param svgFile svg file as a string
     */
    fun readSVGFromString(svgFile: String) {
        readSVG(svgFile,
            fun(str: String) {
                // do nothing for each line
            },
            fun(rect: String) {
                val newRect = createRect(rect)
                classes.add(newRect)
                allElements.add(newRect)
            },
            fun(path: String) {
                val mapElement = createPath(path)
                if (mapElement.isClosed) {
                    classes.add(mapElement)
                    allElements.add(mapElement)
                }
            },
            fun(svg: String) {
                val newSVG = createSVG(svg)
                if (newSVG.transportationType != "") indoorTransportations.add(newSVG)
                allElements.add(newSVG)
            }
        )
    }

    /**
     * Automate changing the floor number for the class in the svg file. This also takes into
     * consideration whether the options under settings are enable or disabled in order to
     * properly handle the icon being shown on the floor map.
     * @param svg to update
     * @return svg as string (updated with new floor numbers)
     */
    fun automateSVG(svg: String, floorNumber: String): String {
        val prepreBuilding = svg.split("docname=\"")
        val preBuilding = prepreBuilding[1].split("-")
        val preNumBuilding = preBuilding[1].split(".svg")
        val numBuilding = preNumBuilding[0]
        var newFileStr = ""

        val patternText = Regex("<text")
        val patternDocName = Regex("docname=")
        val originalSVG: List<String> = svg.split("\n")
        var svgArray = mutableListOf<String>()

        //Add all of the svg in the
        for (line in originalSVG) {
            if (FloorFragment.settingsOff.isNotEmpty()) {
                if (line.contains("id=\"") && checkArrayForLine(FloorFragment.settingsOff, line)) {
                    svgArray.add(line.replace(">", "display=\"none\">"))
                } else {
                    svgArray.add(line)
                }
            } else  {
                svgArray.add(line)
            }
        }

        for (i in svgArray) {
            val resultText = patternText.containsMatchIn(i)
            val resultDocName = patternDocName.containsMatchIn(i)

            if (!resultText && !resultDocName) {
                newFileStr += i + "\n"
                continue
            }
            if (resultText) {
                val textArray = i.split("> ")
                val str = textArray[1].split(" </")
                val roomNum = str[0]
                val roomNumRegex = Regex(numBuilding)
                val newFloor = roomNumRegex.replaceFirst(roomNum, floorNumber)
                val newTextTag =
                    textArray.elementAt(0) + "> " + "$newFloor" + " </" + str.elementAt(1)
                newFileStr += newTextTag + "\n"
            }
            if (resultDocName) {
                val textArray = i.split("-")
                val str = textArray[1].split(".")
                val docNum = str[0]
                val docNumRegex = Regex(numBuilding)
                val newDocNum = docNumRegex.replaceFirst(docNum, floorNumber)
                val newDocTag =
                    textArray.elementAt(0) + "-" + "$newDocNum" + "." + str.elementAt(1)
                newFileStr += newDocTag + "\n"
            }
        }
        return newFileStr
    }

    /**
     * Compares the id in line with the content of the array to see if there is a match.
     * @param settingArray which contains all the settings options that are turned off
     * @param lineToModify the line that might need to be modified
     * @return true if the line needs modification else false
     */
    private fun checkArrayForLine(settingArray: ArrayList<String>, lineToModify: String): Boolean {
        var check = false

        for (element in settingArray) {
            if (lineToModify.contains(element)) {
                check = true
            }
        }
        return check
    }

    /**
     * This method takes as input the svg file string and searches for an element with a matching
     * id. Once found, it will add another element right on top with a different colour to
     * higlight it
     * @param svg String of the svg file
     * @param id String of the id to look for
     * @return String of the svg file with a highlighted building
     */
    fun highlightClassroom(svg: String, id: String): String {
        val highlightedSVG: StringBuilder = StringBuilder()
        val highlightedColor: String = "#bca878"

        readSVG(svg,
            fun(line) { highlightedSVG.append(line).append("\n") },
            fun(rect) {
                val rectangle = createRect(rect)
                if (rectangle.getID().equals(id)) {
                    rectangle.style = "fill:$highlightedColor;fill-opacity:1;stroke:#000000;" +
                            "stroke-width:1.36025514;stroke-miterlimit:4;stroke-opacity:1;" +
                            "stroke-dasharray:none"
                    highlightedSVG.append(rectangle.toString()).append("\n")
                }
            },
            fun(path) {
                val thePath = createPath(path)
                if (thePath.getID().equals(id)) {
                    thePath.style = "fill:$highlightedColor;fill-opacity:1;stroke:#000000;" +
                            "stroke-width:1.36025514;stroke-linecap:butt;" +
                            "stroke-linejoin:miter;stroke-miterlimit:4;" +
                            "stroke-opacity:1;stroke-dasharray:none"
                    highlightedSVG.append(thePath.toString()).append("\n")
                }
            },
            fun(svg) {
                // do nothing for svg elements, because we don't highlight them
            }
        )
        return highlightedSVG.toString()
    }

    /**
     * This method takes the element declaration as a string and converts it into an SVG object
     * @param elmnt String of the element
     * @return SVG object
     */
    internal fun createSVG(elmnt: String): SVG {
        val id = extractAttr("id", elmnt)
        var type: String = ""
        when (id.substring(0, 5)) {
            "stair" -> type = "stairs"
            "escal" -> type = "escalators"
            "eleva" -> type = "elevators"
        }
        val x = extractAttr("x", elmnt)
        val y = extractAttr("y", elmnt)
        return SVG(id, type, x.toDouble(), y.toDouble())
    }

    /**
     * This method takes as input the element and converts it into a Rect object
     * @param elmnt the svg element as a string
     * @return Rect object
     */
    internal fun createRect(elmnt: String): Rect {
        val id = extractAttr("id", elmnt)
        val x = extractAttr("x", elmnt).toDouble()
        val y = extractAttr("y", elmnt).toDouble()
        val height = extractAttr("height", elmnt).toDouble()
        val width = extractAttr("width", elmnt).toDouble()
        val style = extractAttr("style", elmnt)
        return Rect(id, x, y, height, width, style)
    }

    /**
     * This method takes as input the element and converts it into a Path object
     * @param elmnt the svg element as a string
     * @return Path object
     */
    internal fun createPath(elmnt: String): Path {
        val id = extractAttr("id", elmnt)
        val d = extractAttr("d", elmnt)
        val style = extractAttr("style", elmnt)
        val isClosed = d[d.length - 1] == 'z' || d[d.length - 1] == 'Z'
        val transform: String = extractAttr("transform", elmnt)
        return Path(id, d, transform, style, isClosed)
    }

    /**
     * This method extracts the attribute from a line of an SVG element
     * @param attribute to extract
     * @param line of the element
     */
    internal fun extractAttr(attribute: String, line: String): String {
        val string: String = " $attribute="

        for (i in line.indices) {
            if (
                (i + string.length) < line.length
                && string == line.substring(i, i + string.length)
            ) {
                return extractString(i + string.length + 1, line)
            }
        }
        return ""
    }

    /**
     * This method will extract the portion of a string up until a quote
     * @param iteration start of extraction point
     * @param line to extract from
     * @return extracted string
     */
    private fun extractString(iteration: Int, line: String): String {
        var it = iteration
        val string = StringBuilder()
        while(line[it] != '"') {
            string.append(line[it])
            it++
        }
        return string.toString()
    }

    /**
     * Return the time in seconds between two classrooms, using their ids as strings
     * @param start the starting id
     * @param end the ending id
     * @return the time required to travel between one classroom and the other
     */
    fun getTimeInSeconds(start: String, end: String): Int {
        val startAndEnd = getStartAndEnd(Pair(start, end))
        if (startAndEnd.first == null || startAndEnd.second == null) return 0
        val startClass: MapElement = allElements[startAndEnd.first!!]
        val endClass: MapElement = allElements[startAndEnd.second!!]

        val topLeft = Pair(firstElement!!.getWidth().first, firstElement!!.getHeight().first)
        val bottomRight = Pair(firstElement!!.getWidth().second, firstElement!!.getHeight().second)
        val maxDistance = getDistance(topLeft, bottomRight)
        val scale = 150 / maxDistance // seconds/unit distance
        return (getDistance(startClass.getCenter(), endClass.getCenter()) * scale).toInt()
    }

    /**
     * Method used to get the index of classes given their ids
     * @param startAndEnd a pair with the start being the first element and the second being the last element
     * @return pair of start and end indices for the classes
     */
    fun getStartAndEnd(startAndEnd: Pair<String, String>): Pair<Int?, Int?> {
        var startInt: Int? = null
        var endInt: Int? = null
        for ((x, aClass) in allElements.withIndex()) {
            if (aClass.getID().equals(startAndEnd.first)) {
                startInt = x
            }
            if (aClass.getID().equals(startAndEnd.second)) {
                endInt = x
            }
        }
        return Pair(startInt, endInt)
    }

    /**
     * Method for getting an SVG file as a string after it has processed a file. Takes 2 ids (for the start and end)
     * class rooms to display directions on it
     * @param startAndEnd pair of start and end ids
     * @return svg file as a string
     */
    fun getSVGStringFromDirections(startAndEnd: Pair<String, String>): String {
        val startAndEnd = getStartAndEnd(startAndEnd)
        val startInt: Int? = startAndEnd.first
        val endInt: Int? = startAndEnd.second

        if (startInt == null || endInt == null) {
            return ""
        }

        val list: MutableList<Circle> = generatePoints()
        val paths = createPaths(list, allElements[endInt])

        val string: StringBuilder = StringBuilder()
        stringArray.forEach {

            if (it.contains("<!-- PATH ARROW HERE  -->")) {
                string.append(
                    A_Star(
                        allElements[startInt],
                        allElements[endInt],
                        paths
                    ) + "\n"
                )
            }
            string.append(it + "\n")
        }
        return string.toString()
    }

    /**
     * This method will attempt to create points in a grid and add points to the pathPoints list if the point is
     * not within any other classroom
     * @return list of points that are in path
     */
    internal fun generatePointsAcrossMap(): MutableList<Circle> {
        val pathPoints: MutableList<Circle> = mutableListOf()

        // scatter points in missing spots, using average distance as a scale for step size
        for (x in firstElement!!.getWidth().first.toInt() until firstElement!!.getWidth().second.toInt() step 50) {
            for (y in firstElement!!.getHeight().first.toInt() until firstElement!!.getHeight().second.toInt() step 50) {
                if (inPath(x.toDouble(), y.toDouble())) {
                    pathPoints.add(Circle(x.toDouble(), y.toDouble(), 5.0))
                }
            }
        }
        return pathPoints
    }

    /**
     * Generates points using a technique where we add points surrounding the class room if they
     * are within the path
     * @return list of generated points
     */
    fun generatePoints(): MutableList<Circle> {

        var pathPoints: MutableList<Circle> = mutableListOf()

        // average distance is used for scaling the distance between points in the next step
        var averageDistance: Double = 0.0
        var totalPoints: Int = 0
        findNearestPointToClasses { closestPoint: Pair<Pair<Double, Double>, Pair<Double, Double>> ->
            pathPoints.add(
                Circle(
                    (closestPoint.first.first + closestPoint.second.first) / 2,
                    (closestPoint.first.second + closestPoint.second.second) / 2, 5.0
                )
            )
            averageDistance += getDistance(closestPoint.first, closestPoint.second)
            totalPoints++
        }
        averageDistance /= totalPoints
        pathPoints.addAll(generatePointsAcrossMap())
        return pathPoints
    }

    /**
     * Find the nearest point to the classes
     * @param doForClosestPoint runs a method on the closest point
     */
    fun findNearestPointToClasses(doForClosestPoint: (Pair<Pair<Double, Double>, Pair<Double, Double>>) -> Unit) {

        val stepSize: Double = 15.0
        classes.forEach { it ->
            val center = it.getCenter()
            //var closestPoint: Pair<Pair<Double, Double>, Pair<Double, Double>>? = null

            // draw a line in 8 directions (top, left, up, down, top-left, top-right, bottom-left, bottom-right)
            // and get the nearest point and furthest point of the path, if it traverses the path
            var nearestPoints: MutableList<Pair<Pair<Double, Double>, Pair<Double, Double>>?> =
                mutableListOf()
            nearestPoints.add(getNearestPathPoint(center, stepSize, stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, stepSize, -stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, -stepSize, true, true))
            nearestPoints.add(getNearestPathPoint(center, -stepSize, stepSize, true, true))
            // find the closests point our of all the nearest points
            for (point in nearestPoints) {
                if (point != null) {
                    doForClosestPoint(point)
                }
            }
        }
    }

    /**
     * Get the nearest path point to a center in a certain direction given by the parameters.
     * @param center to find nearest point to
     * @param stepSizeX step size in the x directions
     * @param stepSizeY step size in the y directions
     * @param applyX whether to apply x or not
     * @param applyY whether to apply y or not
     */
    fun getNearestPathPoint(
        center: Pair<Double, Double>,
        stepSizeX: Double,
        stepSizeY: Double,
        applyX: Boolean,
        applyY: Boolean
    ): Pair<Pair<Double, Double>, Pair<Double, Double>>? {
        var navPointX = center.first
        var navPointY = center.second
        var closestPoint: Pair<Double, Double>? = null
        var points: Pair<Pair<Double, Double>, Pair<Double, Double>>? = null
        var pathFound: Boolean = false

        while (isWithinBounds(navPointX, navPointY)) {
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

    /**
     * This method takes as input the points in a path and creates paths between them in a Node data structure.
     * It creates these paths in between nodes first by checking to make sure that this path won't go through any
     * classes
     * this is used for the dijkstra method
     *
     * @param pathPoints list of points in a path
     * @return list of Node elements linked together to form a graph
     */
    internal fun createPaths(pathPoints: MutableList<Circle>): MutableList<Node> {
        val nodeList: MutableList<Node> = mutableListOf()

        val num: Int = pathPoints.size / 8
        val threads: MutableList<Thread> = mutableListOf()

        for (point in pathPoints) {
            nodeList.add(Node(point, mutableListOf()))
        }

        for (i in 0..7) {
            threads.add(
                thread(start = true) {
                    connectNeighbours(nodeList.subList(i * num, (i + 1) * num), nodeList)
                }
            )
        }

        for (thread in threads) {
            thread.join()
        }

        // for any that might be missed
        for (point in nodeList) {
            if (point.neighbors.size == 0) {
                connectNeighbours(mutableListOf(point), nodeList)
            }
        }
        return nodeList
    }

    /**
     * This method takes as input the points in a path and creates paths between them in a Node data structure.
     * It creates these paths in between nodes first by checking to make sure that this path won't go through any
     * classes.
     * This particular method is used for the A* algorithm
     *
     * @param pathPoints circles to make into vertices
     * @param endPoint to calculate the heuristic function
     */
    internal fun createPaths(
        pathPoints: MutableList<Circle>,
        endPoint: MapElement
    ): MutableList<Vertex> {
        val vertices: MutableList<Vertex> = mutableListOf()

        val num: Int = pathPoints.size / 8
        val threads: MutableList<Thread> = mutableListOf()

        for (point in pathPoints) {
            vertices.add(Vertex(point, endPoint.getCenter()))
        }

        for (i in 0..7) {
            threads.add(
                thread(start = true) {
                    connectNeighboursA(vertices.subList(i * num, (i + 1) * num), vertices)
                }
            )
        }

        for (thread in threads) {
            thread.join()
        }

        // for any that might be missed
        for (point in vertices) {
            if (point.neighbors.size == 0) {
                connectNeighboursA(mutableListOf(point), vertices)
            }
        }
        return vertices
    }

    /**
     * This method connects all points that can be added as neighbors and adds them
     * @param subsection sublist of nodeList for this thread
     * @param nodeList total list of nodes (used with all the threads but not mutated)
     * @return subsection with neighbors added
     */
    fun connectNeighbours(
        subsection: MutableList<Node>,
        nodeList: MutableList<Node>
    ): MutableList<Node> {
        for (pointA in subsection) {
            for (pointB in nodeList) {
                if (checkPath(
                        pointA.circle,
                        pointB.circle
                    ) && !pointA.circle.equals(pointB.circle)
                ) {
                    pointA.neighbors.add(pointB)
                }
            }
        }
        return subsection
    }

    /**
     * Connect the neighbors for the A* algorithm
     * @param subsection of the list
     * @param whole list to find neighbors for
     * @return list of connected vertices
     */
    fun connectNeighboursA(
        subsection: MutableList<Vertex>,
        nodeList: MutableList<Vertex>
    ): MutableList<Vertex> {
        for (pointA in subsection) {
            for (pointB in nodeList) {
                if (
                    checkPath(
                        Circle.getPoint(pointA.pos.first, pointA.pos.second),
                        Circle.getPoint(pointB.pos.first, pointB.pos.second)
                    )
                    && pointA.pos != pointB.pos
                ) {
                    pointA.neighbors.add(pointB)
                }
            }
        }
        return subsection
    }

    /**
     * The check path method will do the check to make sure that a path doesn't cross any classrooms
     * @param pointA the first point
     * @param pointB the second point
     * @return returns whether the path is good (true, doesn't cross anything) or not
     */
    internal fun checkPath(pointA: Circle, pointB: Circle): Boolean {
        if (getDistance(pointA.getCenter(), pointB.getCenter()) > 110.0) return false
        // making line equation y = mx + b
        val m: Double = (pointA.cy - pointB.cy) / (pointA.cx - pointB.cx)
        val b: Double = pointA.cy - (m * pointA.cx)

        if (abs(pointA.cx - pointB.cx) > abs(pointA.cy - pointB.cy)) {

            val x: Double = if (pointA.cx < pointB.cx) pointA.cx else pointB.cx
            for (step in 10 until abs(pointB.cx - pointA.cx).toInt() step 15) {
                val newX: Double = x + step
                if (!inPath(newX, (m * newX) + b)) {
                    return false
                }
            }
        } else if (abs(pointA.cx - pointB.cx) > 1.0) {

            val y: Double = if (pointA.cy < pointB.cy) pointA.cy else pointB.cy
            for (step in 10 until abs(pointB.cy - pointA.cy).toInt() step 15) {
                val newY: Double = y + step
                if (!inPath((newY - b) / m, newY)) {
                    return false
                }
            }
        } else {
            val y: Double = if (pointA.cy < pointB.cy) pointA.cy else pointB.cy
            for (step in 10 until abs(pointB.cy - pointA.cy).toInt() step 15) {
                val newY: Double = y + step
                if (!inPath(pointA.cx, newY)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * This method iterates over all the classes and returns whether a point is within any of them
     * @param x the x coordinate
     * @param y the y coordinate
     * @return return whether the point is in the path (true) or within a classroom (false)
     */
    internal fun inPath(x: Double, y: Double): Boolean {
        classes.forEach { it ->
            if (it.isWithin(x, y)) return false
        }
        return isWithinBounds(x, y)
    }

    /**
     * This method checks whether the point is within the map
     * @param x the x coordinate
     * @param y the y coordinate
     * @return whether the point is within the map
     */
    internal fun isWithinBounds(x: Double, y: Double): Boolean {
        if (firstElement!!.isWithin(x, y)) return true
        return false
    }

    /**
     * Get the position on the map given the element's id
     * @param id to find center for
     * @return position on the map given an x and y
     */
    fun getPositionWithId(id: String): Pair<Double, Double>? {
        for (elmnt in allElements) {
            if (elmnt.getID() == id) return elmnt.getCenter()
        }
        return null
    }

    /**
     * Given a position, find the nearest transportation method while taking into account
     * @param pos x and y coordinates
     * @param goingUp boolean whether the user will be going up
     * @return SVG element as indoor transportation method
     */
    fun findNearestIndoorTransportation(pos: Pair<Double, Double>, goingUp: Boolean): SVG {
        var closestTransport: SVG? = null
        for (transport in indoorTransportations) {
            if (closestTransport == null
                || getDistance(
                    transport.getCenter(),
                    pos
                ) < getDistance(closestTransport.getCenter(), pos)
            ) {
                if (goingUp && !transport.id.contains("down")) closestTransport = transport
                if (!goingUp && !transport.id.contains("up")) closestTransport = transport
            }
        }
        return closestTransport!!
    }
}

/**
 * This method gets the distance between 2 pairs of coordinates
 * @param x first coordinate
 * @param y second coordinate
 * @return distance
 */
internal fun getDistance(x: Pair<Double, Double>, y: Pair<Double, Double>): Double {
    return sqrt(abs(x.first - y.first).pow(2.0) + abs(x.second - y.second).pow(2.0))
}
