package com.droidhats.mapprocessor

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class TestProcessMap {

    private lateinit var map: ProcessMap

    @Before
    fun BeforeStuff() {
        map = ProcessMap()
        val file = File("test-file.svg")
        val svg: String = file.bufferedReader().use { it.readText() }
        map.readSVGFromString(svg)
    }

    @Test
    fun TestGetDistance() {
        val x = Pair(0.0, 0.0)
        val y = Pair(3.0, 4.0)
        Assert.assertEquals(5.0, getDistance(x, y), 0.0001)

        val z = Pair(-3.0, -4.0)
        Assert.assertEquals(5.0, getDistance(x, z), 0.0001)
    }

    @Test
    fun TestIsWithinBounds() {
        Assert.assertFalse(map.isWithinBounds(0.0, 0.0))
        Assert.assertFalse(map.isWithinBounds(0.0, 250.0))
        Assert.assertFalse(map.isWithinBounds(50.0, 0.0))

        Assert.assertTrue(map.isWithinBounds(50.0, 250.0))
    }

    @Test
    fun TestInPath() {
        Assert.assertTrue(map.inPath(400.0, 350.0))

        for (classRoom in map.getClasses()) {
            val center = classRoom.getCenter()
            val top = classRoom.getHeight().first
            val bottom = classRoom.getHeight().second
            val left = classRoom.getWidth().first
            val right = classRoom.getWidth().second
            Assert.assertFalse(map.inPath(center.first, center.second))
            Assert.assertFalse(map.inPath(left, top))
            Assert.assertFalse(map.inPath(right, top))
            Assert.assertFalse(map.inPath(left, bottom))
            Assert.assertFalse(map.inPath(right, bottom))
        }
    }

    @Test
    fun TestCheckPath() {
        Assert.assertFalse(map.checkPath(Circle(189.0, 129.0, 5.0), Circle(129.0, 230.0, 5.0)))
        Assert.assertFalse(map.checkPath(Circle(150.0, 530.0, 5.0), Circle(189.0, 330.0, 5.0)))
        Assert.assertFalse(map.checkPath(Circle(189.0, 730.0, 5.0), Circle(189.0, 330.0, 5.0)))
        Assert.assertTrue(map.checkPath(Circle(150.0, 330.0, 5.0), Circle(189.0, 330.0, 5.0)))
    }

    @Test
    fun TestCreatePaths() {
        val nodeList = map.createPaths(map.generatePointsAcrossMap())

        // sanity check
        Assert.assertTrue(nodeList.size > 0)
        for (node in nodeList) {
            for (neighbor in node.neighbors) {
                Assert.assertTrue(map.checkPath(node.circle, neighbor.circle))
            }
        }
    }

    @Test
    fun TestGeneratePointsAcrossMap() {
        val circleList = map.generatePointsAcrossMap()

        // sanity check
        Assert.assertTrue(circleList.size > 0)
        for (point in circleList) {
            Assert.assertTrue(map.inPath(point.cx, point.cy))
        }
    }

    @Test
    fun TestGetSVGStringFromDirections() {
        val svg = map.getSVGStringFromDirections(Pair(3803, 3805))
        val stringArray = svg.split("\n")
        var lastPath = ""
        for (string in 10 until stringArray.size) {
            if (stringArray[string].contains("<path")) lastPath = stringArray[string]
        }
        var startClass: MapElement? = null
        var endClass: MapElement? = null
        for (aClass in map.getClasses()) {
            if (aClass.getID().contains("3803")) startClass = aClass
            if (aClass.getID().contains("3805")) endClass = aClass
        }
        Assert.assertEquals(lastPath, Dijkstra(startClass!!, endClass!!, map.createPaths(map.generatePointsAcrossMap())))

        Assert.assertEquals(map.getSVGStringFromDirections(Pair(1111, 3805)), "")
        Assert.assertEquals(map.getSVGStringFromDirections(Pair(3803, 1111)), "")
        Assert.assertEquals(map.getSVGStringFromDirections(Pair(1111, 1111)), "")
    }

    @Test
    fun TestExtractAttr() {
        val testPath: String = "<path id=\"123\" d=\"Some path\" style=\"testStyle\" />"

        Assert.assertEquals(map.extractAttr("id", testPath), "123")
        Assert.assertEquals(map.extractAttr("d", testPath), "Some path")
        Assert.assertEquals(map.extractAttr("style", testPath), "testStyle")
        Assert.assertEquals(map.extractAttr("transform", testPath), "")

        val testRect: String = "<rect x=\"20\" y=\"20\" width=\"20\" height=\"20\" id=\"123\" style=\"testStyle\" />"

        Assert.assertEquals(map.extractAttr("id", testRect), "123")
        Assert.assertEquals(map.extractAttr("style", testRect), "testStyle")
        Assert.assertEquals(map.extractAttr("x", testRect), "20")
        Assert.assertEquals(map.extractAttr("y", testRect), "20")
        Assert.assertEquals(map.extractAttr("height", testRect), "20")
        Assert.assertEquals(map.extractAttr("width", testRect), "20")
    }

    @Test
    fun TestCreatePath() {
        val testPath: String = "<path id=\"123\" d=\"m 0,0 20,20 z\" style=\"testStyle\" />"
        val resultPath: Path = map.createPath(testPath)
        Assert.assertEquals(resultPath.getID(), "123")
        Assert.assertEquals(resultPath.vertices[0], Pair(0.0,0.0))
        Assert.assertEquals(resultPath.vertices[1], Pair(20.0,20.0))
        Assert.assertEquals(resultPath.isClosed, true)
        Assert.assertEquals(resultPath.style, "testStyle")
        Assert.assertEquals(resultPath.transform, "")
    }

    @Test
    fun TestCreateRect() {
        val testRect: String = "<rect x=\"20\" y=\"20\" width=\"20\" height=\"20\" id=\"123\" style=\"testStyle\" />"
        val resultRect: Rect = map.createRect(testRect)

        Assert.assertEquals(resultRect.id, "123")
        Assert.assertEquals(resultRect.style, "testStyle")
        Assert.assertEquals(resultRect.x, 20.0, 0.0001)
        Assert.assertEquals(resultRect.y, 20.0, 0.0001)
        Assert.assertEquals(resultRect.height, 20.0, 0.0001)
        Assert.assertEquals(resultRect.width, 20.0, 0.0001)
    }

    @Test
    fun TestReadSVGFromString() {
        val testSVG: String = " <rect\n" +
                "         y=\"220.21933\"\n" +
                "         x=\"33.57143\"\n" +
                "         height=\"625.71429\"\n" +
                "         width=\"682.14288\"\n" +
                "         id=\"rect3007\"\n" +
                "         style=\"fill:#f7d6d6;fill-opacity:1;stroke:#000000;stroke-width:1.36025514;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" />\n" +
                "      <rect\n" +
                "         y=\"220.21933\"\n" +
                "         x=\"33.57143\"\n" +
                "         height=\"69.821426\"\n" +
                "         width=\"95.178574\"\n" +
                "         id=\"rect3781\"\n" +
                "         style=\"fill:#da3636;fill-opacity:1;stroke:#000000;stroke-width:1.36025514;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" />\n" +
                "      <rect\n" +
                "         y=\"220.21933\"\n" +
                "         x=\"128.75\"\n" +
                "         height=\"92.142853\"\n" +
                "         width=\"60.714279\"\n" +
                "         id=\"rect3783\"\n" +
                "         style=\"fill:#da3636;fill-opacity:1;stroke:#000000;stroke-width:1.36025514;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" />\n" +
                "      <rect\n" +
                "         y=\"220.21933\"\n" +
                "         x=\"189.46428\"\n" +
                "         height=\"92.142853\"\n" +
                "         width=\"66.785721\"\n" +
                "         id=\"rect3785\"\n" +
                "         style=\"fill:#da3636;fill-opacity:1;stroke:#000000;stroke-width:1.36025514;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\" />\n" +
                "       <path\n" +
                "         style=\"fill:#da3636;fill-opacity:1;stroke:#000000;stroke-width:2;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\"\n" +
                "         d=\"m 291.09375,631.125 0,27.84375 0,16.28125 38.59375,0 1.3125,0 0,24 24.6875,0 0,-40.28125 -26,0 0,-27.84375 -38.59375,0 z\"\n" +
                "         transform=\"matrix(0.68012757,0,0,0.68012757,26.656197,183.82552)\"\n" +
                "         id=\"rect4717\"\n" +
                "         inkscape:connector-curvature=\"0\" />\n" +
                "      <path\n" +
                "         style=\"fill:#da3636;fill-opacity:1;stroke:#000000;stroke-width:2;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none\"\n" +
                "         d=\"m 595.75,549.75 0,95.3125 36.75,0 0,30.71875 60.84375,0 0,-30.71875 0,-95.3125 -97.59375,0 z\"\n" +
                "         transform=\"matrix(0.68012757,0,0,0.68012757,26.656197,183.82552)\"\n" +
                "         id=\"rect4726\"\n" +
                "         inkscape:connector-curvature=\"0\" />\n"

        val testProcessMap: ProcessMap = ProcessMap()
        testProcessMap.readSVGFromString(testSVG)

        Assert.assertEquals(testProcessMap.getClasses().size, 5)
        Assert.assertEquals(testProcessMap.firstElement?.getID(), "rect3007")

        val classes: List<MapElement> = testProcessMap.getClasses()
        Assert.assertEquals(classes[0].getID(), "rect3781")
        Assert.assertEquals(classes[1].getID(), "rect3783")
        Assert.assertEquals(classes[2].getID(), "rect3785")
        Assert.assertEquals(classes[3].getID(), "rect4717")
        Assert.assertEquals(classes[4].getID(), "rect4726")
    }
}
