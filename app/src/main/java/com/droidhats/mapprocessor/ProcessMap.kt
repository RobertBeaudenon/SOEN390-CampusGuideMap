package com.droidhats.mapprocessor

import java.io.File

class ProcessMap {
    //    private lateinit var hello: File
    private var rectangles: MutableList<String> = mutableListOf()
    private var paths: MutableList<String> = mutableListOf()

    fun readSVG() {
        var element: String = ""
        var inRect: Boolean = false
        var inPath: Boolean = false

        File("Hall-8.svg").forEachLine {
            if (it.contains("<rect")) {
                inRect = true
            }
            if (inRect) element += it

            if (it.contains("/>") && inRect) {
                inRect = false
                rectangles.add(element)
                element = ""
            }

            if (it.contains("<path")) {
                inPath = true
            }
            if (inPath) element += it

            if (it.contains("/>") && inPath) {
                inPath = false
                paths.add(element)
                element = ""
            }
        }

        rectangles.forEach{ it ->
            println(it)
            println(extractAttr("id", it))
        }

        paths.forEach{ it ->
            println(it)
        }

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
