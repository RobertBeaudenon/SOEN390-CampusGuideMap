package com.droidhats.campuscompass

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.lifecycle.MutableLiveData

class CalendarRepository {

    private lateinit var userCalendars : MutableMap<String,Calendar>// The user's retrieved calendars

    companion object {
   // Singleton instantiation
        private var instance: CalendarRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: CalendarRepository().also { instance = it }
            }
    }

    fun getCalendars(context: Context) : MutableLiveData<MutableMap<String, Calendar>>
    {
        pingCalendars(context)
        return MutableLiveData<MutableMap<String, Calendar>>().apply {
            value = userCalendars
        }
    }

    private fun pingCalendars(context: Context) {

        userCalendars = mutableMapOf()

        // Run query
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        try {
            val resolver = context.contentResolver

            // In this query's WHERE clause I specify that I only want the user's personal google events (ordered by event start time)
            val cur: Cursor = resolver.query(uri, Calendar.event_projection.keys.toTypedArray(),
                                            "${CalendarContract.Events.ACCOUNT_TYPE} = 'com.google' AND" +
                                                    " ${CalendarContract.Events.ACCOUNT_NAME} == ${CalendarContract.Events.OWNER_ACCOUNT}",
                                            null,
                                            CalendarContract.Events.DTSTART)!!

            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(Calendar.event_projection.getValue(CalendarContract.Events.CALENDAR_ID))
                val accountName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.ACCOUNT_NAME))
                val accountType: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.ACCOUNT_TYPE))
                val ownerName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.OWNER_ACCOUNT))
                val displayName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.CALENDAR_DISPLAY_NAME))
                val title: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.TITLE))
                val location: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.EVENT_LOCATION))
                val dtstart: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DTSTART))
                val dtend: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DTEND))
                val color: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DISPLAY_COLOR))
                val description: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DESCRIPTION))

                //Construct the calendar and add it to the collection of user calendars
                if ( userCalendars.containsKey(color) ) {
                    userCalendars[color]!!.events.add(CalendarEvent(title, location, dtstart, dtend, description, color))
                }
                else {
                    val calendar = Calendar(calID.toString(), accountName, accountType, displayName, ownerName, color)
                    calendar.events.add(CalendarEvent(title, location, dtstart, dtend, description, color))
                    userCalendars[color] = calendar
                }
            }
            cur.close()
        } catch (e: SecurityException) {
            println("Caught Security Exception when attempting to access calendar")
            println("The user didn't accept to access the calendar")
            println("Here is the error stack: ${e.printStackTrace()}")
        }
    }
}