package com.droidhats.mapprocessor

abstract class MapElement(){
    abstract override fun toString(): String
}

class Rect (val id: String, val x: Double, val y: Double, val height: Double, val width: Double, val style: String) : MapElement() {
    override fun toString(): String {
        return "<rect id=\"$id\" x=\"$x\" y=\"$y\" height=\"$height\" width=\"$width\" style=\"$style\" />"
    }
}

class Path(val id: String, val d: String, val style: String, val isClosed: Boolean) : MapElement() {
    override fun toString(): String {
        return "<path id=\"$id\" d=\"$d\" style=\"$style\"/>"
    }
}
