package com.droidhats.campuscompass

import android.provider.CalendarContract

/*
* A Calendar is uniquely identified by the color of its events
* In reality, a calendar is uniquely identified by it's ID on the user's device (CalendarContract.Events.CALENDAR_ID)
* as a user can have multiple calendar apps installed.
* However, for the purposes of this app, we are only supporting Google Calendars events
* */

class Calendar(
    cal_id: String,
    cal_account_name: String,
    cal_account_type: String,
    cal_display_name: String,
    cal_owner_name: String,
    cal_color: String
) {

    var id: String? = cal_id
    var accountName: String? = cal_account_name
    var accountType: String? = cal_account_type
    var displayName: String? = cal_display_name
    var ownerName: String? = cal_owner_name
    var color: String? = cal_color
    var events: ArrayList<CalendarEvent> = arrayListOf()

    companion object {
        var event_projection =
            mapOf(
                CalendarContract.Events.CALENDAR_ID to 0,  //The Calendars#_ID of the calendar the event belongs to.
                CalendarContract.Events.ACCOUNT_NAME to 1,
                CalendarContract.Events.ACCOUNT_TYPE to 2,
                CalendarContract.Events.OWNER_ACCOUNT to 3,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME to 4,
                CalendarContract.Events.TITLE to 5,
                CalendarContract.Events.EVENT_LOCATION to 6,
                CalendarContract.Events.DTSTART to 7,  //The time the event starts in UTC millis since epoch.
                CalendarContract.Events.DTEND to 8,   // The time the event ends in UTC millis since epoch.
                CalendarContract.Events.DISPLAY_COLOR to 9,
                CalendarContract.Events.DESCRIPTION to 10
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
                "\nColor: $color"
    }

}


