package com.droidhats.mapprocessor

import org.junit.Assert
import org.junit.Test

class TestPathAlgorithm {

    @Test
    fun testNode() {
        val neighborNode = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        val testNode = Node(Circle(0.0, 0.0, 5.0), mutableListOf(neighborNode))
        Assert.assertEquals(testNode.value, -1.0, 0.1)
        Assert.assertEquals(testNode.shortestPath, mutableListOf<Circle>())
        Assert.assertEquals(testNode.visitedBy, mutableListOf<Node>())

        Assert.assertTrue(testNode.circle.equals(Circle(0.0, 0.0, 5.0)))
    }

    @Test
    fun testGetDistance() {
        val mapElement: MapElement = Circle(0.0, 0.0, 5.0)
        val node: Node = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        Assert.assertEquals(getDistance(mapElement, node), getDistance(mapElement.getCenter(), node.circle.getCenter()), 0.001)

        val nodeB: Node = Node(Circle(0.0, 0.0, 5.0), mutableListOf())
        Assert.assertEquals(getDistance(node, nodeB), getDistance( nodeB.circle.getCenter(), node.circle.getCenter()), 0.001)
    }

    @Test
    fun testCopyList() {
        val list = mutableListOf<Circle>(Circle(0.0, 5.0, 5.0),Circle(5.0, 0.0, 5.0),Circle(0.0, 0.0, 5.0))
        val copiedList = copyList(list)
        for (item in 0 until copiedList.size) {
            Assert.assertTrue(list[item].equals(copiedList[item]))
        }
    }

    @Test
    fun testIsInList() {
        val list = mutableListOf<Node>(
                Node(Circle(0.0, 5.0, 5.0), mutableListOf()),
                Node(Circle(5.0, 5.0, 5.0), mutableListOf()),
                Node(Circle(5.0, 0.0, 5.0), mutableListOf())
        )
        Assert.assertFalse(isInList(Node(Circle(2.0,0.0,5.0), mutableListOf()), list))
        Assert.assertTrue(isInList(Node(Circle(5.0,0.0,5.0), mutableListOf()), list))
    }

    @Test
    fun testFindNearestPoint() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        val pathElements: MutableList<Node> = mutableListOf(endPoint, endPoint2, middlePoint, currentPoint2)

        val mapElement: MapElement = Circle(20.0, 20.0, 5.0)
        Assert.assertTrue(findNearestPoint(mapElement, pathElements).circle.equals(endPoint.circle))
    }

    @Test
    fun testPathToString() {
        val list = mutableListOf<Circle>(Circle(0.0, 5.0, 5.0),Circle(5.0, 0.0, 5.0),Circle(0.0, 0.0, 5.0))
        val string = "<path id=\"\" d=\"m 0.0,5.0 5.0,-5.0\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,0.0 -5.0,0.0\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 0.0,0.0 17.674999999999997,-17.674999999999997\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 0.0,0.0 17.674999999999997,17.674999999999997\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/>"
        Assert.assertEquals(pathToString(list), string)
    }

    @Test
    fun testSubDijkstra() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val currentPoint: Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf(endPoint))

        val visited: MutableList<Node> = mutableListOf()
        dijsktra(currentPoint, endPoint, visited)
        Assert.assertEquals(endPoint.shortestPath.size, 0)

        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        dijsktra(currentPoint2, endPoint, mutableListOf())

        val string: String = "<path id=\"\" d=\"m 0.0,5.0 5.0,-0.5\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,4.5 -15.828554103265402,19.346010570657715\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,4.5 -19.346010570657715,-15.828554103265402\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/>"
        Assert.assertEquals(pathToString(endPoint.shortestPath), string)
    }

    @Test
    fun testDijkstra() {
        val endPoint:Node = Node(Circle(5.0, 5.0, 5.0), mutableListOf())
        val endPoint2: Node = Node(Circle(5.0, 4.5, 5.0), mutableListOf(endPoint))
        val middlePoint: Node = Node(Circle(5.0, 4.4, 5.0), mutableListOf(endPoint2, endPoint))
        val currentPoint2: Node = Node(Circle(0.0, 5.0, 5.0), mutableListOf(middlePoint, endPoint2))
        val pathElements: MutableList<Node> = mutableListOf(endPoint, endPoint2, middlePoint, currentPoint2)

        val start: MapElement = Circle(0.0, 5.0, 5.0)
        var end: MapElement = Circle(0.0, 5.0, 5.0)
        Assert.assertEquals(getShortestPath(start, end, pathElements), "<circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" style=\"fill:#7317ad\"/><circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" style=\"fill:#7317ad\"/><path id=\"\" d=\"m 0.0,5.0 0.0,0.0\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/>")

        end = Circle(5.0, 5.0, 5.0)
        Assert.assertEquals(getShortestPath(start, end, pathElements),
            "<path id=\"\" d=\"m 0.0,5.0 5.0,-0.5\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,4.5 0.0,0.5\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,5.0 -17.675,-17.675\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><path id=\"\" d=\"m 5.0,5.0 17.675,-17.675\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/><circle cx=\"0.0\" cy=\"5.0\" r=\"5.0\" style=\"fill:#7317ad\"/><circle cx=\"5.0\" cy=\"5.0\" r=\"5.0\" style=\"fill:#7317ad\"/>")

        end = Circle(20.0, 20.0, 5.0)
        pathElements.add(Node(end, mutableListOf()))
        Assert.assertEquals(getShortestPath(start, end, pathElements), "<circle cx=\"5.0\" cy=\"4.5\" r=\"5.0\" style=\"fill:#7317ad\"/><circle cx=\"20.0\" cy=\"20.0\" r=\"5.0\" style=\"fill:#7317ad\"/><path id=\"\" d=\"m 5.0,4.5 15.0,15.5\" style=\"stroke:#7317ad;stroke-width:6;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" transform=\"\"/>")
    }

}
