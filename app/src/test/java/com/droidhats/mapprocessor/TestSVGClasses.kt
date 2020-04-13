package com.droidhats.mapprocessor

import org.junit.Test
import org.junit.Assert

class TestSVGClasses {

    @Test
    fun testRect() {
        val testRect = Rect("id", 0.0, 0.0, 20.0, 20.0, "style")

        Assert.assertEquals("id", testRect.getID())

        Assert.assertTrue(testRect.isWithin(-4.0, -4.0))
        Assert.assertTrue(testRect.isWithin(24.0, 24.0))
        Assert.assertFalse(testRect.isWithin(30.0, 15.0))
        Assert.assertFalse(testRect.isWithin(15.0, 30.0))
        Assert.assertFalse(testRect.isWithin(30.0, 30.0))

        Assert.assertEquals(testRect.getWidth().first, 0.0, 0.1)
        Assert.assertEquals(testRect.getWidth().second, 20.0, 0.1)
        Assert.assertEquals(testRect.getHeight().first, 0.0, 0.1)
        Assert.assertEquals(testRect.getHeight().second, 20.0, 0.1)

        Assert.assertEquals(testRect.getCenter().first, 10.0, 0.1)
        Assert.assertEquals(testRect.getCenter().second, 10.0, 0.1)
    }

    @Test
    fun testPath() {
        val testPath = Path("id", "m 0,0 20,0 0,20 -20,0 Z", "", "style", true)

        Assert.assertEquals(testPath.vertices[0], Pair(0.0, 0.0))
        Assert.assertEquals(testPath.vertices[1], Pair(20.0, 0.0))
        Assert.assertEquals(testPath.vertices[2], Pair(20.0, 20.0))
        Assert.assertEquals(testPath.vertices[3], Pair(0.0, 20.0))

        Assert.assertEquals(20.0, testPath.xMax, 0.1)
        Assert.assertEquals(20.0, testPath.yMax, 0.1)
        Assert.assertEquals(0.0, testPath.yMin, 0.1)
        Assert.assertEquals(0.0, testPath.xMin, 0.1)

        val testNonClosedPath = Path ("id", "0,0 0,0", "", "style", false)

        Assert.assertEquals(-1.0, testNonClosedPath.xMax, 0.1)
        Assert.assertEquals(-1.0, testNonClosedPath.yMax, 0.1)
        Assert.assertEquals(-1.0, testNonClosedPath.yMin, 0.1)
        Assert.assertEquals(-1.0, testNonClosedPath.xMin, 0.1)

        Assert.assertEquals(testPath.getCenter(), Pair(10.0, 10.0))

        testPath.transform = "sevenc2.0,2.0,2.0,2.0,2.0,2.0"
        testPath.transformVertices()

        Assert.assertEquals(testPath.vertices[0], Pair(2.0, 2.0))
        Assert.assertEquals(testPath.vertices[1], Pair(2.0, 42.0))
        Assert.assertEquals(testPath.vertices[2], Pair(42.0, 82.0))
        Assert.assertEquals(testPath.vertices[3], Pair(42.0, 42.0))

        Assert.assertEquals(testPath.toString(),
                "<path id=\"id\" d=\"m 0,0 20,0 0,20 -20,0 Z\" style=\"style\" " +
                        "transform=\"sevenc2.0,2.0,2.0,2.0,2.0,2.0\"/>")
        Assert.assertFalse(testPath.isWithin(-4.0, -4.0))
        Assert.assertFalse(testPath.isWithin(24.0, 24.0))
        Assert.assertFalse(testPath.isWithin(30.0, 15.0))
        Assert.assertTrue(testPath.isWithin(15.0, 30.0))
        Assert.assertFalse(testPath.isWithin(30.0, 30.0))

        Assert.assertEquals(testPath.getWidth(), Pair(0.0, 20.0))
        Assert.assertEquals(testPath.getHeight(), Pair(0.0, 20.0))

        val createdPath = Path.createPath(Pair(0.0, 0.0), Pair(20.0, 20.0))
        Assert.assertEquals(createdPath.id, "")
        Assert.assertEquals(createdPath.d, "m 0.0,0.0 20.0,20.0")
        Assert.assertEquals(createdPath.transform, "")
        Assert.assertEquals(createdPath.style, "stroke:#000000;stroke-width:2.01184581;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1")
        Assert.assertFalse(createdPath.isClosed)
    }

    @Test
    fun testCircle() {
        val testCircle = Circle(0.0, 0.0, 5.0)

        Assert.assertNotNull(testCircle.getID())
        Assert.assertEquals(testCircle.toString(), "<circle cx=\"0.0\" cy=\"0.0\" r=\"5.0\" />")

        val testGetPoint = Circle.getPoint(5.0, 5.0)
        Assert.assertEquals(testGetPoint.cx, 5.0, 0.1)
        Assert.assertEquals(testGetPoint.cy, 5.0, 0.1)
        Assert.assertEquals(testGetPoint.r, 5.0, 0.1)

        Assert.assertEquals(testGetPoint.getCenter(), Pair(5.0, 5.0))
        Assert.assertFalse(testCircle.equals(testGetPoint))
        Assert.assertTrue(testCircle.equals(Circle(0.0, 0.0, 0.0)))
    }
}