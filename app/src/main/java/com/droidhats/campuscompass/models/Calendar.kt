package com.droidhats.campuscompass.models

import android.provider.CalendarContract

/*
* A Calendar is uniquely identified by the color of its events
* In reality, a calendar is uniquely identified by it's ID on the user's device (CalendarContract.Events.CALENDAR_ID)
* as a user can have multiple calendar apps installed.
* However, for the purposes of this app, we are only supporting Google Calendars events
* */

class Calendar(
    var id: String?,
    var accountName: String?,
    var accountType: String?,
    var displayName: String?,
    var ownerName: String?,
    var color: String?,
    var isDefaultColor: Boolean,
    var events: ArrayList<CalendarEvent> = arrayListOf()
) {

    companion object {
        var event_projection =
            mapOf(
                CalendarContract.Events.CALENDAR_ID to 0,  //The Calendars#_ID of the calendar the event belongs to.
                CalendarContract.Events._ID to 1,
                CalendarContract.Events.ACCOUNT_NAME to 2,
                CalendarContract.Events.ACCOUNT_TYPE to 3,
                CalendarContract.Events.OWNER_ACCOUNT to 4,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME to 5,
                CalendarContract.Events.TITLE to 6,
                CalendarContract.Events.EVENT_LOCATION to 7,
                CalendarContract.Events.DTSTART to 8,  //The time the event starts in UTC millis since epoch.
                CalendarContract.Events.DTEND to 9,   // The time the event ends in UTC millis since epoch.
                CalendarContract.Events.DISPLAY_COLOR to 10,
                CalendarContract.Events.DESCRIPTION to 11
            )
            private set


        var instances_projection =
            mapOf(
                CalendarContract.Instances.EVENT_ID to 0,
                CalendarContract.Instances.TITLE to 1,
                CalendarContract.Instances.EVENT_LOCATION to 2,
                CalendarContract.Instances.BEGIN to 3,
                CalendarContract.Instances.END to 4,
                CalendarContract.Instances.DISPLAY_COLOR to 5,
                CalendarContract.Instances.DESCRIPTION to 6
            )
            private set
    }

    fun printEvents() {

        for (i in events.orEmpty()) {
            println(i.toString())
        }
    }

    override fun toString(): String {

        return "Calendar ID: $id" +
                "\nAccount Name: $accountName" +
                "\nAccount Type: $accountType" +
                "\nOwner Name: $ownerName" +
                "\nDisplay Name: $displayName" +
                "\nColor: $color" +
                "\nIs Default Color: $isDefaultColor"
    }

}


