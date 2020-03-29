package com.droidhats.mapprocessor

fun Dijkstra(start: MapElement, end: MapElement, pathElements: MutableList<Node>): String {
    val startPoint: Node = findNearestPoint(start, pathElements)
    val endPoint: Node = findNearestPoint(end, pathElements)

    if (startPoint.circle.equals(endPoint.circle)) {
        var string: String = endPoint.circle.toString() + Circle(end.getCenter().first, end.getCenter().second, 5.0)
        string += Path.createPath(endPoint.circle.getCenter(), end.getCenter())
        return string
    }

    var visited: MutableList<Node> = mutableListOf()
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

fun pathToString(nodeList: List<Circle>): String {
    var string: StringBuilder = StringBuilder()
    var x: Int = 0
    while (x < nodeList.size - 1) {
        val path = Path.createPath(nodeList[x].getCenter(), nodeList[x + 1].getCenter())
        string.append(path)
        x++
    }
    return string.toString()
}

fun copyList(list: List<Circle>): MutableList<Circle> {
    var mutList: MutableList<Circle> = mutableListOf()
    for (node in list) {
        mutList.add(node)
    }
    return mutList
}

fun isInList(point: Node, list: List<Node>): Boolean {
    for (node in list) {
        if(point.circle.equals(node.circle)) return true
    }
    return false
}

fun findNearestPoint(mapElement: MapElement, pathElements: List<Node>): Node {
    var nearestNode: Node = pathElements[0]
    var smallestDistance: Double = getDistance(mapElement, nearestNode)
    for (node in pathElements) {
        var distanceToNode = getDistance(mapElement, node)
        if(distanceToNode < smallestDistance) {
            nearestNode = node
            smallestDistance = distanceToNode
        }
    }
    return nearestNode
}

fun getDistance(mapElement: MapElement, nodeB: Node): Double {
    return getDistance(mapElement.getCenter(), nodeB.circle.getCenter())
}

fun getDistance(nodeA: Node, nodeB: Node): Double {
    return getDistance(nodeA.circle.getCenter(), nodeB.circle.getCenter())
}

class Node(val circle: Circle, var neighbors: MutableList<Node>) {
    var value: Double = -1.0
    var shortestPath: MutableList<Circle> = mutableListOf()
    var visitedBy: MutableList<Node> = mutableListOf()

    fun drawAllPaths(): String {
        var string: StringBuilder = StringBuilder()
        for (neighbor in neighbors) {
            string.append(Path.createPath(circle.getCenter(), neighbor.circle.getCenter()))
        }
        return string.toString()
    }
}
