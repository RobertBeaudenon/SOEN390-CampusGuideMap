package com.droidhats.campuscompass

import android.provider.CalendarContract

class Calendar(cal_id: String, cal_account_name: String, cal_display_name: String, cal_owner_name: String) {

        var id : String? = cal_id
        var  account_name : String? = cal_account_name
        var display_name : String? = cal_display_name
        var owner_name : String? = cal_owner_name

    companion object
    {
        var calendar_projection =
            mapOf<String, Int>(CalendarContract.Calendars._ID to 0,
                CalendarContract.Calendars.ACCOUNT_NAME to 1,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME to 2,
                CalendarContract .Calendars.OWNER_ACCOUNT to 3
            )
            private set
    }

    override fun toString(): String {

        return "Calendar ID: $id" +
                "\nAccount Name: $account_name" +
                "\nDisplay Name: $display_name" +
                "\nOwner Name: $owner_name"
    }

}



