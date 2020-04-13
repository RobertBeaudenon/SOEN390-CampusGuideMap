package com.droidhats.mapprocessor

import kotlin.math.pow
import kotlin.math.sqrt

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
        point.shortestPath = mutableListOf()
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
    if (currentPoint.circle.equals(endPoint.circle)) return
    visited.add(currentPoint)

    for (point in currentPoint.neighbors) {
        val dist = getDistance(point, currentPoint) + currentPoint.value
        if (point.value == -1.0) {
            point.value = dist
            point.shortestPath = copyList(currentPoint.shortestPath)
            point.shortestPath.add(currentPoint.circle)
        }
        if (point.value > dist) {
            point.value = dist
            point.shortestPath = copyList(currentPoint.shortestPath)
            point.shortestPath.add(currentPoint.circle)
            SubDijkstra(point, endPoint, visited)
        }
        point.visitedBy.add(currentPoint)
    }

    for (point in currentPoint.neighbors) {
        if (!isInList(point, visited)){
            SubDijkstra(point, endPoint, visited)
        }
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

    // Get the start and end point of the last drawn path and use them to draw the arrow head paths
    val lastDrawnPathStartPoint = nodeList[x-1]
    val lastDrawnPathEndPoint = nodeList[x]
    val arrowHeadPaths: List<String> = getArrowHeadPaths(lastDrawnPathStartPoint, lastDrawnPathEndPoint)

    // Append the two arrow heads' paths to the string to be drawn on the map.
    string.append(arrowHeadPaths[0])
    string.append(arrowHeadPaths[1])

    return string.toString()
}


/**
 * Takes the start and end point of a path and returns a list of strings represnting paths for
 * two arrow heads originating from the end of the path
 * @param startPoint The start point of the navigation path
 * @param endPoint The end point of the navigation path
 * @return returns a list of two strings of the arrow heads path elements
 */
fun getArrowHeadPaths(startPoint: Circle, endPoint: Circle) : List<String> {

    // Get the x and y coordinates of the start and end points of the path
    val x1: Double = startPoint.getCenter().first
    val y1: Double = startPoint.getCenter().second
    val x2: Double = endPoint.getCenter().first
    val y2: Double = endPoint.getCenter().second

    // The value of cosine at 45 degrees
    val cos = 0.707

    // The lengths of the path between the start and end points
    val pathLength: Double = sqrt(((x2-x1).pow(2)+(y2-y1).pow(2)))

    // The lengths of the path between the end point and the arrow head point
    val arrowLength = 25.0

    // Calculate the x and y coordinates for the two arrow head points.
    val x3: Double = x2 + (arrowLength/pathLength)*((x1-x2)*cos + (y1-y2)*cos)
    val y3: Double = y2 + (arrowLength/pathLength)*((y1-y2)*cos - (x1-x2)*cos)
    val x4: Double = x2 + (arrowLength/pathLength)*((x1-x2)*cos - (y1-y2)*cos)
    val y4: Double = y2 + (arrowLength/pathLength)*((y1-y2)*cos + (x1-x2)*cos)

    // Use the x and y coordinates to create arrow head circles.
    val firstArrowEnd = Circle(x3, y3, 2.0 )
    val secondArrowEnd = Circle(x4, y4, 2.0 )

    // Draw two paths each connecting the end point of the navigation path to one of the arrow head ends
    val firstArrowPath = Path.createPath(endPoint.getCenter(), firstArrowEnd.getCenter())
    val secondArrowPath = Path.createPath(endPoint.getCenter(), secondArrowEnd.getCenter())

    return listOf(firstArrowPath.toString(), secondArrowPath.toString())
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