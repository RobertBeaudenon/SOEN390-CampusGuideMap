package com.droidhats.mapprocessor

class Vertex(circle: Circle, endPoint: Pair<Double, Double>, var neighbors: MutableList<Vertex>) {
    val pos: Pair<Double, Double> = Pair(circle.cx, circle.cy)
    val heuristic: Double = getDistance(pos, endPoint)
    var prev: Vertex? = null
    private var value: Double? = null
    private var sum: Double? = null

    fun setValue(value: Double) {
        this.value = value
        sum = value + heuristic
    }
    fun getValue(): Double? = value
    fun getSum(): Double? = sum
}
