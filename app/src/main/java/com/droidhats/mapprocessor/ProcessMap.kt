package com.droidhats.mapprocessor

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Class ProcessMap is where all the parsing of the svg formatted map, and interacts with the other
 * components to generate a path on the map
 */
class ProcessMap {
    private var classes: MutableList<MapElement> = mutableListOf()
    fun getClasses() = classes

    private val indoorTransportations: MutableList<SVG> = mutableListOf()
    fun getIndoorTransportationMethods(): List<SVG> = indoorTransportations

    internal var firstElement: MapElement? = null
    private lateinit var stringArray: List<String>

    /**
     * This method takes as input an svg file in the form of a string and digests the elements.
     * It assumes that the svg file in the form of a string is properly formatted with new line characters.
     * @param svgFile svg file as a string
     */
    fun readSVGFromString(svgFile: String) {
        var element: StringBuilder = StringBuilder()
        var inRect: Boolean = false
        var inPath: Boolean = false
        var inSVG: Boolean = false
        var firstElement: Boolean = true

        stringArray = svgFile.split("\n")

        for (it in stringArray) {

            if (it.contains("<svg") && !firstElement) {
                inSVG = true
                indoorTransportations.add(createSVG(it))
            }

            if (it.contains("</svg>")) {
                inSVG = false
                continue
            }

            if (inSVG) {
                element.append(it)
                continue
            }

            if (it.contains("<rect")) {
                inRect = true
            }
            if (inRect) element.append(it)

            if (it.contains("/>") && inRect) {
                if (firstElement) {
                    this.firstElement = createRect(element.toString())
                    firstElement = false
                } else {
                    classes.add(createRect(element.toString()))
                }
                inRect = false
                element = StringBuilder()
            }

            if (it.contains("<path")) {
                inPath = true
            }
            if (inPath) element.append(it)

            if (it.contains("/>") && inPath) {
                val path: Path = createPath(element.toString())
                if (firstElement) {
                    this.firstElement = path
                    firstElement = false
                } else if (path.isClosed) {
                    classes.add(path)
                }
                inPath = false
                element = StringBuilder()
            }
        }
    }

    /**
     * This method takes the element declaration as a string and converts it into an SVG object
     * @param elmnt String of the element
     * @return SVG object
     */
    internal fun createSVG(elmnt: String): SVG {
        val id = extractAttr("id", elmnt)
        var type: String = ""
        when (id.substring(0, 6)) {
            "stairs" -> type = "stairs"
            "escala" -> type = "escalators"
            "elevat" -> type = "elevators"
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
        val isClosed = d[d.length - 1] == 'z'
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

    /**
     * Return the time in seconds between two classrooms, using their ids as strings
     * @param start the starting id
     * @param end the ending id
     * @return the time required to travel between one classroom and the other
     */
    fun getTimeInSeconds(start: String, end: String): Int {
        val startAndEndClasses = getStartAndEndClasses(Pair(start, end))
        if (startAndEndClasses.first == null || startAndEndClasses.second == null) return 0
        val startClass: MapElement = classes[startAndEndClasses.first!!]
        val endClass: MapElement = classes[startAndEndClasses.second!!]

        val topLeft = Pair(firstElement!!.getWidth().first, firstElement!!.getHeight().first)
        val bottomRight = Pair(firstElement!!.getWidth().second, firstElement!!.getHeight().second)
        val maxDistance = getDistance(topLeft, bottomRight)
        val scale = 150/maxDistance // seconds/unit distance
        return (getDistance(startClass.getCenter(), endClass.getCenter()) * scale).toInt()
    }

    /**
     * Method used to get the index of classes given their ids
     * @param startAndEnd a pair with the start being the first element and the second being the last element
     * @return pair of start and end indices for the classes
     */
    fun getStartAndEndClasses(startAndEnd: Pair<String, String>): Pair<Int?, Int?> {
        var startInt: Int? = null
        var endInt: Int? = null
        var x = 0
        for (aClass in classes) {
            var start = startAndEnd.first
            if (start[start.length - 1] == '0' && start[start.length - 2] == '.') {
                start = start.substring(0, start.length - 1)
            }
            if (aClass.getID().equals(startAndEnd.first)) {
                startInt = x
            }

            if(aClass.getID().equals(startAndEnd.second)) {
                endInt = x
            }
            x++
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
        val startAndEndClasses = getStartAndEndClasses(startAndEnd)
        val startInt: Int? = startAndEndClasses.first
        val endInt: Int? = startAndEndClasses.second

        if (startInt == null || endInt == null) {
            return ""
        }

        val list: MutableList<Circle> = generatePointsAcrossMap()

        val string: StringBuilder = StringBuilder()
        var wrote: Boolean = false
        stringArray.forEach { it ->
            if (it.contains("</g>") && !wrote) {
                string.append(Dijkstra(classes[startInt], classes[endInt], createPaths(list)) + "\n")
                wrote = true
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
        for (x in firstElement!!.getWidth().first.toInt() until firstElement!!.getWidth().second.toInt() step 20) {
            for (y in firstElement!!.getHeight().first.toInt() until firstElement!!.getHeight().second.toInt() step 20) {
                if (inPath(x.toDouble(), y.toDouble())) {
                    pathPoints.add(Circle(x.toDouble(), y.toDouble(), 5.0))
                }
            }
        }
        return pathPoints
    }

    /**
     * This method takes as input the points in a path and creates paths between them in a Node data structure.
     * It creates these paths in between nodes first by checking to make sure that this path won't go through any
     * classes
     * @param pathPoints list of points in a path
     * @return list of Node elements linked together to form a graph
     */
    internal fun createPaths(pathPoints: MutableList<Circle>): MutableList<Node> {
        val nodeList: MutableList<Node> = mutableListOf()
        val finalList: MutableList<Node> = mutableListOf()

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

    /**
     * The check path method will do the check to make sure that a path doesn't cross any classrooms
     * @param pointA the first point
     * @param pointB the second point
     * @return returns whether the path is good (true, doesn't cross anything) or not
     */
    internal fun checkPath(pointA: Circle, pointB: Circle): Boolean {
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

    /**
     * This method iterates over all the classes and returns whether a point is within any of them
     * @param x the x coordinate
     * @param y the y coordinate
     * @return return whether the point is in the path (true) or within a classroom (false)
     */
    internal fun inPath(x: Double, y: Double): Boolean {
        classes.forEach{ it ->
            if (it.isWithin(x, y)) return false
        }
        return true
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
