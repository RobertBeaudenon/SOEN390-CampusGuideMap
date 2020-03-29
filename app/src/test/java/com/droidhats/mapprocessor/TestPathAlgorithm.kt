package com.droidhats.mapprocessor

import org.junit.Assert
import org.junit.Test

class TestPathAlgorithm {

    @Test
    fun TestNode() {
        val neighborNode = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        val testNode = Node(Circle(0.0, 0.0, 5.0), mutableListOf(neighborNode))
        Assert.assertEquals(testNode.value, -1.0, 0.1)
        Assert.assertEquals(testNode.shortestPath, mutableListOf<Circle>())
        Assert.assertEquals(testNode.visitedBy, mutableListOf<Node>())

        Assert.assertEquals(testNode.drawAllPaths(), Path.createPath(testNode.circle.getCenter(), neighborNode.circle.getCenter()).toString())
        Assert.assertTrue(testNode.circle.equals(Circle(0.0, 0.0, 5.0)))
    }

    @Test
    fun TestGetDistance() {
        val mapElement: MapElement = Circle(0.0, 0.0, 5.0)
        val node: Node = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        Assert.assertEquals(getDistance(mapElement, node), getDistance(mapElement.getCenter(), node.circle.getCenter()), 0.001)

        val nodeB: Node = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        Assert.assertEquals(getDistance(node, nodeB), getDistance( nodeB.circle.getCenter(), node.circle.getCenter()), 0.001)
    }

    @Test
    fun TestCopyList() {
        val list = mutableListOf<Circle>(Circle(0.0, 5.0, 5.0),Circle(5.0, 0.0, 5.0),Circle(0.0, 0.0, 5.0))
        val copiedList = copyList(list)
        for (item in 0 until copiedList.size) {
            Assert.assertTrue(list[item].equals(copiedList[item]))
        }
    }

    @Test
    fun TestIsInList() {
        val list = mutableListOf<Node>(
                Node(Circle(0.0, 5.0, 5.0), mutableListOf()),
                Node(Circle(5.0, 5.0, 5.0), mutableListOf()),
                Node(Circle(5.0, 0.0, 5.0), mutableListOf())
        )
        Assert.assertFalse(isInList(Node(Circle(2.0,0.0,5.0), mutableListOf()), list))
        Assert.assertTrue(isInList(Node(Circle(5.0,0.0,5.0), mutableListOf()), list))
    }

    @Test
    fun TestFindNearestPoint() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        val pathElements: MutableList<Node> = mutableListOf(endPoint, endPoint2, middlePoint, currentPoint2)

        val mapElement: MapElement = Circle(20.0, 20.0, 5.0)
        Assert.assertTrue(findNearestPoint(mapElement, pathElements).circle.equals(endPoint.circle))
    }

    @Test
    fun TestPathToString() {
        val list = mutableListOf<Circle>(Circle(0.0, 5.0, 5.0),Circle(5.0, 0.0, 5.0),Circle(0.0, 0.0, 5.0))
        Assert.assertEquals(pathToString(list), Path.createPath(list[0].getCenter(), list[1].getCenter()).toString() +
                Path.createPath(list[1].getCenter(), list[2].getCenter()).toString())

    }

    @Test
    fun TestSubDijkstra() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val currentPoint: Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf(endPoint))

        val visited: MutableList<Node> = mutableListOf()
        SubDijkstra(currentPoint, endPoint, visited)
        Assert.assertEquals(endPoint.shortestPath.size, 0)

        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        SubDijkstra(currentPoint2, endPoint, mutableListOf())

        val string: String = "<path id=\"\" d=\"m 0.0,5.0 5.0,-0.5\" style=\"\"/>"
        Assert.assertEquals(pathToString(endPoint.shortestPath), string)
    }

    @Test
    fun TestDijkstra() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        val pathElements: MutableList<Node> = mutableListOf(endPoint, endPoint2, middlePoint, currentPoint2)

        val start: MapElement = Circle(0.0, 5.0, 5.0)
        var end: MapElement = Circle(0.0, 5.0, 5.0)
        Assert.assertEquals(Dijkstra(start, end, pathElements), "<circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" />" +
                "<circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" /><path id=\"\" d=\"m 0.0,5.0 0.0,0.0\" style=\"\"/>")

        end = Circle(5.0, 5.0, 5.0)
        Assert.assertEquals(Dijkstra(start, end, pathElements), "<path id=\"\" d=\"m 0.0,5.0 5.0,-0.5\" " +
                "style=\"\"/><path id=\"\" d=\"m 5.0,4.5 0.0,0.5\" style=\"\"/>" +
                "<circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" /><circle cx=\"5.0\" cy=\"5.0\" r=\"5.0\" />")

        end = Circle(20.0, 20.0, 5.0)
        pathElements.add(Node(end, mutableListOf()))
        Assert.assertEquals(Dijkstra(start, end, pathElements), "<circle cx=\"5.0\" cy=\"4.5\" r=\"5.0\" />" +
                "<circle cx=\"20.0\" cy=\"20.0\" r=\"5.0\" /><path id=\"\" d=\"m 5.0,4.5 15.0,15.5\" style=\"\"/>")
    }

}
