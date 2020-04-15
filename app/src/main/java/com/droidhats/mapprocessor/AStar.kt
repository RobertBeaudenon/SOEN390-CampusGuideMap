package com.droidhats.mapprocessor

/**
 * Main data structure that we use to hold the vertices in the graph
 */
class Vertex(circle: Circle, endPoint: Pair<Double, Double>) {
    val pos: Pair<Double, Double> = Pair(circle.cx, circle.cy)
    val heuristic: Double = getDistance(pos, endPoint)
    var prev: Vertex? = null
    private var value: Double? = null
    private var sum: Double? = null
    val neighbors: MutableList<Vertex> = mutableListOf()

    /**
     * set the value of this vertex and update its sum based on the heuristic function
     * @param value to update
     */
    fun setValue(value: Double) {
        this.value = value
        sum = value + 1.5 * heuristic
    }

    /**
     * Return the value of this vertex
     * @return value
     */
    fun getValue(): Double? = value

    /**
     * Return the sum
     * @return sum
     */
    fun getSum(): Double? = sum

    /**
     * Remove popped node from the neighbors of this vertex
     * @param poppedNode
     */
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

    /**
     * Inserts the vertex at the proper position
     * @param vertex to insert
     */
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

    /**
     * Returns the first vertex in the list
     * @return vertex
     */
    fun pop(): Vertex? {
        if (queue.size == 0) return null
        return queue.removeAt(0)
    }

    /**
     * Determine whether the vertex is within the queue
     * @param vertex to check
     * @return whether it is not within the queue
     */
    fun isNotWithin(vertex: Vertex): Boolean {
        return vertex !in queue
    }

    /**
     * Remove the vertex in the queue
     * @param vertex to remove
     */
    fun removeVertex(vertex: Vertex) {
        queue.remove(vertex)
    }
}

/**
 * Find the nearest vertex to the given map element
 * @param mapElement
 * @param pathElements to search for the nearest point in
 * @return nearest vertex
 */
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

/**
 * Takes 2 map elements and returns the shortest path in between them given the list of path elements
 * as an arrow
 * @param start element where the user is starting from
 * @param end or destination element to reach
 * @param list of vertices connected in a graph
 * @return string of the path
 */
fun getPath(start: MapElement, end: MapElement, pathElements: MutableList<Vertex>): String {
    val startVertex = findNearestPoint(start, pathElements)
    val endVertex = findNearestPoint(end, pathElements)

    val path = aStar(startVertex, endVertex, start, end, pathElements)
    if (path != null)
        return path

    // converting path to string
    var cur: Vertex? = endVertex
    val string: StringBuilder = StringBuilder()
    while (cur?.prev != null) {
        string.append(Path.createPath(cur.pos, cur.prev!!.pos))
        cur = cur.prev
    }

    // check if the start and end vertices are the same
    if(endVertex != cur) {
        string.append(Circle.getPoint(cur!!.pos.first, cur.pos.second))
    }

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

/**
 * A* algorithm for getting the shortest path quickly
 * @param startVertex the vertex at the start of the path
 * @param endVertex the vertex at the end of the path
 * @param start the start element or place where the user is starting from
 * @param end the end element or destination for where the user wants to go
 * @param pathElements the elements along the path connected in a weighted graph
 */
fun aStar(
    startVertex: Vertex,
    endVertex: Vertex,
    start: MapElement,
    end: MapElement,
    pathElements: MutableList<Vertex>
): String? {
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
            return getPath(start, end, removeStartAndEnd(startVertex, endVertex, pathElements))
        }
    }
    return null
}

/**
 * Removes the found start and end vertices from the inputted list and returns it.
 * @param start vertex
 * @param end vertex
 * @param list of vertices
 * @return new list of vertices
 */
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
