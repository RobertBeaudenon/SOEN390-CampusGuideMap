package com.droidhats.mapprocessor

class Vertex(circle: Circle, endPoint: Pair<Double, Double>) {
    val pos: Pair<Double, Double> = Pair(circle.cx, circle.cy)
    val heuristic: Double = getDistance(pos, endPoint)
    var prev: Vertex? = null
    private var value: Double? = null
    private var sum: Double? = null
    val neighbors: MutableList<Vertex> = mutableListOf()

    fun setValue(value: Double) {
        this.value = value
        sum = value + heuristic
    }
    fun getValue(): Double? = value
    fun getSum(): Double? = sum
    fun removeFromNeighbors(poppedNode: Vertex) {
        for (neighborInd in neighbors.indices) {
            if (neighbors[neighborInd] == poppedNode) {
                neighbors.removeAt(neighborInd)
                return
            }
        }
    }
}

/**
 * A priority queue of next nodes to examine.
 * They are ordered from lowest sum to highest
 */
class PriorityQueue {
    private val queue: MutableList<Vertex> = mutableListOf()
    fun insert(vertex: Vertex) {
        for (indVertex in queue.indices) {
            if (vertex.getSum()!! < queue[indVertex].getSum()!!) {
                // set it in its proper position
                queue.add(indVertex, vertex)
                return
            }
        }
        // case that it has the biggest sum
        queue.add(vertex)
    }

    fun pop(): Vertex? {
        if (queue.size == 0) return null
        return queue.removeAt(0)
    }

    fun isNotWithin(vertex: Vertex): Boolean {
        return vertex !in queue
    }

    fun removeVertex(vertex: Vertex) {
        queue.remove(vertex)
    }
}

fun findNearestPoint(mapElement: MapElement, pathElements: List<Vertex>): Vertex {
    var nearestNode: Vertex = pathElements[0]
    var smallestDistance: Double = getDistance(mapElement.getCenter(), nearestNode.pos)
    for (vertex in pathElements) {
        val distanceToNode = getDistance(mapElement.getCenter(), vertex.pos)
        if(distanceToNode < smallestDistance) {
            nearestNode = vertex
            smallestDistance = distanceToNode
        }
    }
    return nearestNode
}

fun A_Star(start: MapElement, end: MapElement, pathElements: MutableList<Vertex>): String {
    val startVertex = findNearestPoint(start, pathElements)
    val endVertex = findNearestPoint(end, pathElements)
    val queue = PriorityQueue()

    // A* algorithm part
    var poppedNode: Vertex? = startVertex
    while (poppedNode != endVertex) {
        for (neighbor in poppedNode!!.neighbors) {
            if (neighbor.getValue() == null || poppedNode.getValue()!! < neighbor.getValue()!!) {
                neighbor.setValue(getDistance(poppedNode.pos, neighbor.pos))
                neighbor.prev = poppedNode
                if (!queue.isNotWithin(neighbor)) {
                    queue.removeVertex(neighbor)
                }
            }
            neighbor.removeFromNeighbors(poppedNode)
            if (queue.isNotWithin(neighbor)) {
                queue.insert(neighbor)
            }
        }
        poppedNode = queue.pop()
        if (poppedNode == null) {
            return A_Star(start, end, removeStartAndEnd(startVertex, endVertex, pathElements))
        }
    }

    // converting path to string
    var cur: Vertex? = endVertex
    val string: StringBuilder = StringBuilder()
    while (cur?.prev != null) {
        string.append(Path.createPath(cur.pos!!, cur.prev!!.pos))
        cur = cur.prev
    }
    string.append(Circle.getPoint(endVertex.pos.first, endVertex.pos.second))
    string.append(Circle.getPoint(startVertex.pos.first, startVertex.pos.second))

    // Check if the path exists
    if(endVertex.prev != null) {
        // Get the start and end point of the last drawn path and use them to draw the arrow head paths
        val lastDrawnPathStartPoint =
            Circle(endVertex.prev!!.pos.first, endVertex.prev!!.pos.second, 2.0)
        val lastDrawnPathEndPoint = Circle(endVertex.pos.first, endVertex.pos.second, 2.0)

        val arrowHeadPaths: List<String> =
            getArrowHeadPaths(lastDrawnPathStartPoint, lastDrawnPathEndPoint)

        // Append the two arrow heads' paths to the string to be drawn on the map.
        string.append(arrowHeadPaths[0])
        string.append(arrowHeadPaths[1])
    }

    return string.toString()
}

fun removeStartAndEnd(start: Vertex, end: Vertex, list: MutableList<Vertex>): MutableList<Vertex> {
    for (vertex in list) {
        if (vertex == start || vertex == end) {
            for (neighbor in vertex.neighbors) {
                neighbor.removeFromNeighbors(vertex)
            }
        }
    }
    list.remove(start)
    list.remove(end)
    return list
}
