package com.droidhats.campuscompass.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class CalendarTest {

    // initializing test variable for the class
    private val testCal: Calendar = Calendar(
        "123",
        "name",
        "type",
        "display",
        "owner",
        "red",
        false, arrayListOf()
    )

    // Testing the constructor
    @Test
    fun testConstructor() {
        Assert.assertEquals(testCal.id, "123")
        Assert.assertEquals(testCal.accountName, "name")
        Assert.assertEquals(testCal.accountType, "type")
        Assert.assertEquals(testCal.displayName, "display")
        Assert.assertEquals(testCal.ownerName, "owner")
        Assert.assertEquals(testCal.color, "red")
        Assert.assertEquals(testCal.isDefaultColor, false)
        Assert.assertEquals(testCal.events, arrayListOf<CalendarEvent>())
    }

    // testing the toString method
    @Test
    fun testToString() {
        Assert.assertEquals(
            testCal.toString(),
            "Calendar ID: 123\n" +
                    "Account Name: name\n" +
                    "Account Type: type\n" +
                    "Owner Name: owner\n" +
                    "Display Name: display\n" +
                    "Color: red\n" +
                    "Is Default Color: false"
        )
    }

    // testing the values of the projections in the companion object
    @Test
    fun testProjections() {
        var i = 0
        for(event in Calendar.event_projection) {
            Assert.assertEquals(event.value, i)
            i++
        }

        var x = 0
        for(instance in Calendar.instances_projection) {
            Assert.assertEquals(instance.value, x)
            x++
        }
    }

    // testing class setters
    @Test
    fun testSetters() {
        testCal.id = ""
        testCal.accountName = ""
        testCal.accountType = ""
        testCal.displayName = ""
        testCal.ownerName = ""
        testCal.isDefaultColor = true
        testCal.color = ""

        Assert.assertEquals(testCal.id, "")
        Assert.assertEquals(testCal.accountName, "")
        Assert.assertEquals(testCal.accountType, "")
        Assert.assertEquals(testCal.displayName, "")
        Assert.assertEquals(testCal.ownerName, "")
        Assert.assertEquals(testCal.color, "")
        Assert.assertEquals(testCal.isDefaultColor, true)
    }
}
