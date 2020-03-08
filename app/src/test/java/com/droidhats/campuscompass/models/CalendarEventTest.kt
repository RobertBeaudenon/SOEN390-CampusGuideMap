package com.droidhats.campuscompass.models

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CalendarEventTest {
    
    // initializing test variable for testing
    private val calEvent: CalendarEvent = CalendarEvent(
        "title",
        "id",
        "location",
        "start time",
        "end time",
        "desc",
        "color"
        )

    // Test constructor
    @Test
    fun testConstructor() {
        Assert.assertEquals(calEvent.title, "title")
        Assert.assertEquals(calEvent.id, "id")
        Assert.assertEquals(calEvent.location, "location")
        Assert.assertEquals(calEvent.startTime, "start time")
        Assert.assertEquals(calEvent.endTime, "end time")
        Assert.assertEquals(calEvent.description, "desc")
        Assert.assertEquals(calEvent.color, "color")
    }

    // Test toString method
    @Test
    fun testToString() {
        Assert.assertEquals(
            calEvent.toString(),
            "Event: title\n" +
                    "ID: id\n" +
                    "Location: location\n" +
                    "Start Time: start time\n" +
                    "End Time: end time\n" +
                    "Description: desc\n" +
                    "Color: color"
        )
    }

    // Test setters
    @Test
    fun testSetters() {
        calEvent.title = ""
        calEvent.id = ""
        calEvent.location = ""
        calEvent.startTime = ""
        calEvent.endTime = ""
        calEvent.description = ""
        calEvent.color = ""

        Assert.assertEquals(calEvent.title, "")
        Assert.assertEquals(calEvent.id, "")
        Assert.assertEquals(calEvent.location, "")
        Assert.assertEquals(calEvent.startTime, "")
        Assert.assertEquals(calEvent.endTime, "")
        Assert.assertEquals(calEvent.description, "")
        Assert.assertEquals(calEvent.color, "")
    }
}