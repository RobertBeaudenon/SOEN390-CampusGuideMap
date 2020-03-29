package com.droidhats.mapprocessor

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
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
            println(extractAttr("id=", it))
        }

        paths.forEach{ it ->
            println(it)
        }

    }

    fun extractAttr(string: String, line: String): String? {
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

//            println(string)
//            println(string.length)
//            println(line)
//            println(line.length)
//            println(i)
//            line.substring(i, i + string.length)
//            string.equals(line.substring(i, string.length-1))

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

//fun convertTo2DWithoutUsingGetRGB(image: BufferedImage): Array<IntArray>? {
//    val pixels = (image.raster.dataBuffer as DataBufferByte).data
//    val width = image.width
//    val height = image.height
//    val hasAlphaChannel = image.alphaRaster != null
//    val result = Array(height) { IntArray(width) }
//    if (hasAlphaChannel) {
//        val pixelLength = 4
//        var pixel = 0
//        var row = 0
//        var col = 0
//        while (pixel + 3 < pixels.size) {
//            var argb = 0
//            argb += pixels[pixel].toInt() and 0xff shl 24 // alpha
//            argb += pixels[pixel + 1].toInt() and 0xff // blue
//            argb += pixels[pixel + 2].toInt() and 0xff shl 8 // green
//            argb += pixels[pixel + 3].toInt() and 0xff shl 16 // red
//            result[row][col] = argb
//            col++
//            if (col == width) {
//                col = 0
//                row++
//            }
//            pixel += pixelLength
//        }
//    } else {
//        val pixelLength = 3
//        var pixel = 0
//        var row = 0
//        var col = 0
//        while (pixel + 2 < pixels.size) {
//            var argb = 0
//            argb += -16777216 // 255 alpha
//            argb += pixels[pixel].toInt() and 0xff // blue
//            argb += pixels[pixel + 1].toInt() and 0xff shl 8 // green
//            argb += pixels[pixel + 2].toInt() and 0xff shl 16 // red
//            result[row][col] = argb
//            col++
//            if (col == width) {
//                col = 0
//                row++
//            }
//            pixel += pixelLength
//        }
//    }
//    return result
//}
