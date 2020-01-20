package com.droidhats.campuscompass

import android.Manifest
import android.app.Activity
import android.provider.CalendarContract
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import android.content.Context


fun pingCalendar(context: Context, activity: Activity) {

    // TODO: Make attributes for a future class to be implemented during feature #3
    val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT            // 3
    )

    // The indices for the projection array above.
    val PROJECTION_ID_INDEX: Int = 0
    val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3



    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
    ) {
        // Permission is not granted
        println("Permission denied")

        return
    }


    // Run query
    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    try {
        val resolver = activity!!.contentResolver
        val cur: Cursor = resolver.query(uri, EVENT_PROJECTION, null, null, null)!!

        println("Column Count: ${cur.columnCount} Count: ${cur.count}")

        while (cur.moveToNext()) {
            // Get the field values
            val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)

            println("CalID: $calID")
            println("Display Name: $displayName")
            println("accountName: $accountName")
            println("ownerName: $ownerName")
        }
    } catch (e: SecurityException) {
        println("Caught Security Exception when attempting to access calendar")
        println("The user didn't accept to access the calendar")
        println("Here is the error stack: ${e.printStackTrace()}")
    }

}

