package com.droidhats.mapprocessor
import java.io.File

class ProcessMap {
    private var rectangles: MutableList<Rect> = mutableListOf()
    private var paths: MutableList<Path> = mutableListOf()
    private var firstElement: MapElement? = null

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
                    this.firstElement = createRect(it)
                    firstElement = false
                } else {
                    rectangles.add(createRect(it))
                }
                inRect = false
                element = ""
            }

            if (it.contains("<path")) {
                inPath = true
            }
            if (inPath) element += it

            if (it.contains("/>") && inPath) {
                if (firstElement) {
                    this.firstElement = createPath(it)
                    firstElement = false
                } else {
                    paths.add(createPath(it))
                }
                inPath = false
                element = ""
            }
        }

    }

    fun createRect(it: String): Rect {
        val id = extractAttr("id", it)!!
        val x = extractAttr("x", it)!!.toDouble()
        val y = extractAttr("y", it)!!.toDouble()
        val height = extractAttr("height", it)!!.toDouble()
        val width = extractAttr("width", it)!!.toDouble()
        val style = extractAttr("style", it)!!
        return Rect(id, x, y, height, width, style)
    }

    fun createPath(it: String): Path {
        val id = extractAttr("id", it)!!
        val d = extractAttr("d", it)!!
        val style = extractAttr("style", it)!!
        val isClosed = d.contains("z")
        return Path(id, d, style, isClosed)
    }

    fun extractAttr(attribute: String, line: String): String? {
        val string: String = "$attribute="
        if (!line.contains(string)) return null

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

}