package com.droidhats.mapprocessor
import java.io.File

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
        println("d: $d")
        return Path(id, d, style, isClosed)
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
        val writeFile = File("writeFile.svg")
        var wrote: Boolean = false
        File("Hall-8.svg").forEachLine { it ->
            if (it.contains("</g>") && !wrote) {
                writeFile.appendText(createText() + "\n")
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
                string += Circle(center.first, center.second, 10.0)
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
                if (circles[i].isWithin(circle.cx, circle.cy, 35.0)) {
                    circlesList.add(circle)
                    circles.removeAt(x)
                }
                x++
            }
            string += findCenter(circlesList)
            i++
        }

        return string
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
