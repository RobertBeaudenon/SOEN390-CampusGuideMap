package com.droidhats.mapprocessor

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

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
        val svg = map.getSVGStringFromDirections(Pair("rect3803", "rect3805"))
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

        Assert.assertEquals(map.getSVGStringFromDirections(Pair("1111", "3805")), "")
        Assert.assertEquals(map.getSVGStringFromDirections(Pair("3803", "1111")), "")
        Assert.assertEquals(map.getSVGStringFromDirections(Pair("1111", "1111")), "")
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
                "         inkscape:connector-curvature=\"0\" />\n" +
                "<svg width=\"35\" height=\"52\" viewBox=\"0 0 35 52\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\" id=\"escalator_down1\" x=\"340\" y=\"460\">\n" +
                "        <path\n" +
                "            d=\"M13.0369 22.7613C14.2897 23.9784 15.7774 24.587 17.5 24.587C19.2226 24.587 20.6711 23.9784 21.8456 22.7613C23.0984 21.4629 23.7248 19.9212 23.7248 18.136C23.7248 16.3508 23.0984 14.8496 21.8456 13.6324C20.6711 12.3341 19.2226 11.6849 17.5 11.6849C15.7774 11.6849 14.2897 12.3341 13.0369 13.6324C11.8624 14.8496 11.2752 16.3508 11.2752 18.136C11.2752 19.9212 11.8624 21.4629 13.0369 22.7613ZM5.05034 5.35559C8.49553 1.7852 12.6454 0 17.5 0C22.3546 0 26.4653 1.7852 29.8322 5.35559C33.2774 8.84484 35 13.105 35 18.136C35 20.6515 34.3736 23.5321 33.1208 26.778C31.9463 30.0238 30.4978 33.0667 28.7752 35.9068C27.0526 38.7469 25.33 41.4247 23.6074 43.9402C21.9631 46.3745 20.5537 48.322 19.3792 49.7826L17.5 51.8519C17.0302 51.2838 16.4038 50.5535 15.6208 49.6609C14.8378 48.6872 13.4284 46.8208 11.3926 44.0619C9.35682 41.2218 7.55593 38.5034 5.98993 35.9068C4.50224 33.229 3.13199 30.2266 1.87919 26.8997C0.626398 23.5727 0 20.6515 0 18.136C0 13.105 1.68344 8.84484 5.05034 5.35559Z\"\n" +
                "            fill=\"#666666\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M17.5 32.9412C24.4036 32.9412 30 26.6863 30 18.9706C30 11.2549 24.4036 5 17.5 5C10.5964 5 5 11.2549 5 18.9706C5 26.6863 10.5964 32.9412 17.5 32.9412Z\"\n" +
                "            fill=\"#F7D6D6\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M11.4009 21.2074C11.0799 21.1979 10.7691 21.3213 10.542 21.5484C10.3148 21.7756 10.1915 22.0863 10.2009 22.4074C10.2009 22.5731 10.3353 22.7074 10.5009 22.7074C10.6666 22.7074 10.8009 22.5731 10.8009 22.4074C10.7907 22.245 10.8508 22.0861 10.966 21.9712C11.0813 21.8563 11.2404 21.7966 11.4027 21.8074C11.5684 21.8069 11.7023 21.6722 11.7018 21.5065C11.7013 21.3408 11.5666 21.2069 11.4009 21.2074Z\"\n" +
                "            fill=\"#DA3636\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M24.6008 14.9074C24.7629 14.8969 24.9217 14.9567 25.0366 15.0716C25.1515 15.1865 25.2113 15.3453 25.2008 15.5074C25.2008 15.6731 25.3351 15.8074 25.5008 15.8074C25.6665 15.8074 25.8008 15.6731 25.8008 15.5074C25.8103 15.1863 25.6869 14.8756 25.4598 14.6484C25.2326 14.4213 24.9219 14.2979 24.6008 14.3074C24.4351 14.3074 24.3008 14.4417 24.3008 14.6074C24.3008 14.7731 24.4351 14.9074 24.6008 14.9074Z\"\n" +
                "            fill=\"#DA3636\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M18.6009 18.5074C18.4352 18.5074 18.3009 18.6417 18.3009 18.8074V19.4074H17.7009C17.5352 19.4074 17.4009 19.5417 17.4009 19.7074V20.3074H16.8009C16.6352 20.3074 16.5009 20.4417 16.5009 20.6074V21.2074H15.9009C15.7352 21.2074 15.6009 21.3417 15.6009 21.5074V22.1074H15.0009C14.8352 22.1074 14.7009 22.2417 14.7009 22.4074V23.3074C14.7009 23.4731 14.8352 23.6074 15.0009 23.6074C15.1666 23.6074 15.3009 23.4731 15.3009 23.3074V22.7074H15.9009C16.0666 22.7074 16.2009 22.5731 16.2009 22.4074V21.8074H16.8009C16.9666 21.8074 17.1009 21.6731 17.1009 21.5074V20.9074H17.7009C17.8666 20.9074 18.0009 20.7731 18.0009 20.6074V20.0074H18.6009C18.7666 20.0074 18.9009 19.8731 18.9009 19.7074V19.1074H19.5009C19.6666 19.1074 19.8009 18.9731 19.8009 18.8074V18.2074H20.4009C20.5666 18.2074 20.7009 18.0731 20.7009 17.9074V17.3074H21.3009C21.4666 17.3074 21.6009 17.1731 21.6009 17.0074V16.4074H22.2009C22.3666 16.4074 22.5009 16.2731 22.5009 16.1074C22.5009 15.9417 22.3666 15.8074 22.2009 15.8074H21.3009C21.1352 15.8074 21.0009 15.9417 21.0009 16.1074V16.7074H20.4009C20.2352 16.7074 20.1009 16.8417 20.1009 17.0074V17.6074H19.5009C19.3352 17.6074 19.2009 17.7417 19.2009 17.9074V18.5074H18.6009Z\"\n" +
                "            fill=\"#DA3636\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M13.2009 17.0074V20.0074H11.4009C10.1051 20.0067 9.04268 21.0348 9.00089 22.33C8.98328 22.9672 9.22718 23.5839 9.67589 24.0367C10.1515 24.5269 10.8046 24.8047 11.4876 24.8074H14.8224C15.3796 24.8089 15.9142 24.5874 16.3071 24.1924L22.5042 17.9953C22.5606 17.9393 22.6368 17.9077 22.7163 17.9074H24.5142C25.1972 17.9047 25.8502 17.6269 26.3259 17.1367C26.7745 16.6837 27.0184 16.067 27.0009 15.4297C26.9589 14.1347 25.8966 13.1067 24.6009 13.1074H21.4809C20.9237 13.1059 20.3891 13.3274 19.9962 13.7224L18.9009 14.8174V14.0074C18.9015 13.689 18.7748 13.3837 18.549 13.1593C18.5295 13.1398 18.5067 13.1254 18.486 13.1074C18.8649 12.7795 19.0002 12.2506 18.8252 11.781C18.6502 11.3114 18.2019 11 17.7007 11C17.1996 11 16.7512 11.3114 16.5763 11.781C16.4013 12.2506 16.5365 12.7795 16.9155 13.1074C16.6533 13.3331 16.502 13.6615 16.5009 14.0074V17.2174L15.6009 18.1174V17.0074C15.6016 16.6891 15.4748 16.3837 15.249 16.1593C15.2295 16.1398 15.2067 16.1254 15.186 16.1074C15.5649 15.7795 15.7002 15.2506 15.5252 14.781C15.3502 14.3114 14.9019 14 14.4007 14C13.8996 14 13.4512 14.3114 13.2763 14.781C13.1013 15.2506 13.2365 15.7795 13.6155 16.1074C13.3533 16.3331 13.202 16.6615 13.2009 17.0074ZM20.4189 14.1466C20.6999 13.8641 21.0824 13.7059 21.4809 13.7074H24.6009C25.5726 13.7069 26.3693 14.4777 26.4009 15.4489C26.4128 15.9235 26.2303 16.3824 25.8957 16.7191C25.5331 17.0932 25.0351 17.3053 24.5142 17.3074H22.7163C22.4775 17.3068 22.2483 17.4017 22.08 17.5711L15.8829 23.7682C15.6022 24.0503 15.2203 24.2085 14.8224 24.2074H11.4876C10.9672 24.2049 10.4698 23.9928 10.1076 23.6191C9.77241 23.2826 9.58936 22.8237 9.60089 22.3489C9.63247 21.3777 10.4292 20.6069 11.4009 20.6074H13.5855C13.8243 20.608 14.0534 20.5131 14.2218 20.3437L20.4189 14.1466ZM17.1009 12.2074C17.1009 11.876 17.3695 11.6074 17.7009 11.6074C18.0322 11.6074 18.3009 11.876 18.3009 12.2074C18.3009 12.5388 18.0322 12.8074 17.7009 12.8074C17.3695 12.8074 17.1009 12.5388 17.1009 12.2074ZM17.1009 14.0074C17.1009 13.676 17.3695 13.4074 17.7009 13.4074C18.0322 13.4074 18.3009 13.676 18.3009 14.0074V15.4174L17.1009 16.6174V14.0074ZM13.8009 15.2074C13.8009 14.876 14.0695 14.6074 14.4009 14.6074C14.7323 14.6074 15.0009 14.876 15.0009 15.2074C15.0009 15.5388 14.7323 15.8074 14.4009 15.8074C14.0695 15.8074 13.8009 15.5388 13.8009 15.2074ZM14.8248 16.5835C14.9378 16.6956 15.0013 16.8482 15.0009 17.0074V18.7174L13.8009 19.9174V17.0074C13.8012 16.765 13.9474 16.5465 14.1714 16.4538C14.3954 16.361 14.6532 16.4122 14.8248 16.5835Z\"\n" +
                "            fill=\"#DA3636\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "        <path\n" +
                "            d=\"M24.0811 21.5916C23.9639 21.4745 23.774 21.4745 23.6569 21.5916L21.3739 23.8746C21.2608 23.9876 21.2562 24.1694 21.3634 24.288L21.622 24.5727H19.6V22.3476L19.9363 22.7166C19.9914 22.7772 20.0689 22.8127 20.1508 22.8147C20.2333 22.8195 20.3137 22.7873 20.3701 22.7268L22.5811 20.5158C22.6591 20.4405 22.6904 20.3289 22.6629 20.224C22.6354 20.1192 22.5535 20.0372 22.4486 20.0098C22.3438 19.9823 22.2322 20.0136 22.1569 20.0916L20.1682 22.0803L20.0437 21.9435C19.877 21.7603 19.6148 21.6984 19.3838 21.7877C19.1527 21.877 19.0003 22.099 19 22.3467V24.5727C19 24.9041 19.2686 25.1727 19.6 25.1727H21.622C21.8597 25.1746 22.0755 25.0343 22.1701 24.8163C22.2683 24.5997 22.2276 24.3454 22.0666 24.1704L22 24.0969L24.0811 22.0158C24.1982 21.8986 24.1982 21.7087 24.0811 21.5916Z\"\n" +
                "            fill=\"#DA3636\"\n" +
                "            stroke-width=\"0\"/>\n" +
                "      </svg>"

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

        val transportationMethods: List<SVG> = testProcessMap.getIndoorTransportationMethods()
        Assert.assertEquals(transportationMethods.size, 1)
        Assert.assertEquals(transportationMethods[0].transportationType, "escalators")
    }

    @Test
    fun TestGetTimeInSeconds() {
        val processMap: ProcessMap = ProcessMap()
        val file = File("Hall-8.svg")
        val string = file.bufferedReader().use { it.readText() }
        processMap.readSVGFromString(string)

        val pairWidth = processMap.firstElement!!.getWidth()
        val width = pairWidth.second - pairWidth.first
        val pairHeight = processMap.firstElement!!.getHeight()
        val height = pairHeight.second - pairHeight.first
        val maxLength = sqrt(width.pow(2.0) + height.pow(2.0))

        val classes = processMap.getClasses()
        for (classRoomA in classes) {
            for (classRoomB in classes) {

                Assert.assertEquals(
                        (getDistance(classRoomA.getCenter(), classRoomB.getCenter()) * (150/maxLength)).toInt(),
                        processMap.getTimeInSeconds(classRoomA.getID(), classRoomB.getID())
                )

                Assert.assertTrue(processMap.getTimeInSeconds(classRoomA.getID(), classRoomB.getID()) < 150)
            }
        }
    }
}
