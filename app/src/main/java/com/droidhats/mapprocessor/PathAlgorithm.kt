package com.droidhats.mapprocessor

/**
 * Function that returns a string of path elements that would display the shortest path between 2 elements
 * in a graph of path Nodes.
 * @param start starting element
 * @param end ending element
 * @param pathElements graph of connected Nodes
 * @return SVG path elements as a string
 */
fun Dijkstra(start: MapElement, end: MapElement, pathElements: MutableList<Node>): String {
    val startPoint: Node = findNearestPoint(start, pathElements)
    val endPoint: Node = findNearestPoint(end, pathElements)

    if (startPoint.circle.equals(endPoint.circle)) {
        var string: String = endPoint.circle.toString() + Circle(end.getCenter().first, end.getCenter().second, 5.0)
        string += Path.createPath(endPoint.circle.getCenter(), end.getCenter())
        return string
    }

    val visited: MutableList<Node> = mutableListOf()
    startPoint.value = 0.0
    visited.add(startPoint)

    for (point in startPoint.neighbors) {
        val dist = getDistance(startPoint, point)
        point.value = dist
        point.shortestPath.add(startPoint.circle)
    }

    for(point in startPoint.neighbors) {
        SubDijkstra(point, endPoint, visited)
    }

    if(endPoint.shortestPath.size == 0) {
        var x: Int = 0
        while (x < pathElements.size) {
            if(pathElements[x].circle.equals(startPoint.circle) || pathElements[x].circle.equals(endPoint.circle)) {
                pathElements.removeAt(x)
            }
            x++
        }
        return Dijkstra(start, end, pathElements)
    }

    endPoint.shortestPath.add(endPoint.circle)

    var string = pathToString(endPoint.shortestPath)

    string += startPoint.circle
    string += endPoint.circle
    return string
}

/**
 * This recursive function implements the main functionality of the Dijkstra algorithm for finding the
 * shortest path. If it identifies a shorter path to a node, it will append itself to it, in the hopes
 * of reaching the end point. As a result, the end point will have the shortest path to it.
 * @param currentPoint the current node
 * @param endPoint the end node
 * @param visited the list of nodes already visited
 */
fun SubDijkstra(currentPoint: Node, endPoint: Node, visited: MutableList<Node>) {
    if (currentPoint.circle.equals(endPoint.circle) || isInList(currentPoint, visited)) return
    visited.add(currentPoint)

    for (point in currentPoint.neighbors) {
        val dist = getDistance(point, currentPoint) + currentPoint.value
        if (point.value == -1.0) {
            point.value = dist
            point.shortestPath = copyList(currentPoint.shortestPath)
            point.shortestPath.add(currentPoint.circle)
        }
        if (point.value > dist && !isInList(point, currentPoint.visitedBy)) {
            point.value = dist
            point.shortestPath = copyList(currentPoint.shortestPath)
            point.shortestPath.add(currentPoint.circle)
        }
        point.visitedBy.add(currentPoint)
    }

    for (point in currentPoint.neighbors) {
        SubDijkstra(point, endPoint, visited)
    }
}

/**
 * Takes a list of circle elements (points) and returns a string of path elements between them
 * @param nodeList list of circles to connect in a path
 * @return returns a string of the path elements
 */
fun pathToString(nodeList: List<Circle>): String {
    val string: StringBuilder = StringBuilder()
    var x: Int = 0
    while (x < nodeList.size - 1) {
        val path = Path.createPath(nodeList[x].getCenter(), nodeList[x + 1].getCenter())
        string.append(path)
        x++
    }
    return string.toString()
}

/**
 * Copies a list of references to Nodes (the Nodes don't get duplicated), so that every node has a unique list
 * of nodes representing its shortest path
 * @param list of circles (point) to copy
 * @return a new list of circles
 */
fun copyList(list: List<Circle>): MutableList<Circle> {
    val mutList: MutableList<Circle> = mutableListOf()
    for (node in list) {
        mutList.add(node)
    }
    return mutList
}

/**
 * Checks whether a Node is in a list of Nodes
 * @param point Node to check whether it is in a list
 * @param list to check if the point is in it
 * @return whether the Node is in the list
 */
fun isInList(point: Node, list: List<Node>): Boolean {
    for (node in list) {
        if(point.circle.equals(node.circle)) return true
    }
    return false
}

/**
 * Find the nearest point in a list of Nodes to a MapElement
 * @param mapElement map element to find the closest node to
 * @param pathElements list of path elements to search in
 * @return Closest Node to mapElement
 */
fun findNearestPoint(mapElement: MapElement, pathElements: List<Node>): Node {
    var nearestNode: Node = pathElements[0]
    var smallestDistance: Double = getDistance(mapElement, nearestNode)
    for (node in pathElements) {
        val distanceToNode = getDistance(mapElement, node)
        if(distanceToNode < smallestDistance) {
            nearestNode = node
            smallestDistance = distanceToNode
        }
    }
    return nearestNode
}

/**
 * Get distance between map element and a node
 */
fun getDistance(mapElement: MapElement, nodeB: Node): Double {
    return getDistance(mapElement.getCenter(), nodeB.circle.getCenter())
}

/**
 *  Get distance between 2 nodes
 */
fun getDistance(nodeA: Node, nodeB: Node): Double {
    return getDistance(nodeA.circle.getCenter(), nodeB.circle.getCenter())
}

/**
 * The Node class which consists of a vertex (Circle) and its neighbors. It holds a value which is the
 * shortest distance to it, the shortest path to it from a start node, and a list of nodes that have visited
 * this node
 */
class Node(val circle: Circle, var neighbors: MutableList<Node>) {
    var value: Double = -1.0
    var shortestPath: MutableList<Circle> = mutableListOf()
    var visitedBy: MutableList<Node> = mutableListOf()
}