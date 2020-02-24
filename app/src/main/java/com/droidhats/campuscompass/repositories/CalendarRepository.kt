package com.droidhats.campuscompass.repositories

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.models.CalendarEvent
import java.util.*
import kotlin.collections.ArrayList

class CalendarRepository {

    private lateinit var userCalendars : MutableMap<String, Calendar>// The user's retrieved calendars

    companion object {
   // Singleton instantiation
        private var instance: CalendarRepository? = null

        fun getInstance() =
            instance
                ?: synchronized(this) {
                instance
                    ?: CalendarRepository().also { instance = it }
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

            // In this query's WHERE clause I specify that I only want the user's personal google events
            val cur: Cursor = resolver.query(uri, Calendar.event_projection.keys.toTypedArray(),
                                            "${CalendarContract.Events.ACCOUNT_TYPE} = 'com.google' AND" +    //Only google events
                                                    " ${CalendarContract.Events.ACCOUNT_NAME} = ${CalendarContract.Events.OWNER_ACCOUNT} AND " + //Only primary account
                                                    " ${CalendarContract.Events.DELETED} != '1' AND " +
                                                    " ${CalendarContract.Events.STATUS} != '${CalendarContract.Events.STATUS_CANCELED}'",
                                            null,
                                            CalendarContract.Events.DTSTART)!! //Sort by closest event
            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(Calendar.event_projection.getValue(CalendarContract.Events.CALENDAR_ID))
                val eventID: Long = cur.getLong(Calendar.event_projection.getValue(CalendarContract.Events._ID))
                val accountName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.ACCOUNT_NAME))
                val accountType: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.ACCOUNT_TYPE))
                val ownerName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.OWNER_ACCOUNT))
                val displayName: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.CALENDAR_DISPLAY_NAME))
                val title: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.TITLE))
                val location: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.EVENT_LOCATION))
                val dtstart: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DTSTART))
                val dtend: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DTEND))
                val color: String = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DISPLAY_COLOR))
                val description: String? = cur.getString(Calendar.event_projection.getValue(CalendarContract.Events.DESCRIPTION))

                val calendar = Calendar(calID.toString(), accountName, accountType, displayName, ownerName, color)
                if (dtend.isNullOrEmpty()) { // We have a recurring event
                    //Query the recurring event instances
                    val recurringEvents = pingReccuringEvents(context, eventID)

                    for (i in recurringEvents) {
                        constructCalendar(calendar, i)
                    }

                } else {
                    constructCalendar(calendar, CalendarEvent(title, eventID.toString(), location, dtstart, dtend, description, color))
                }

            }
            cur.close()
        } catch (e: SecurityException) {
            println("Security Exception when attempting to access calendar: ${e.printStackTrace()}")
        }
    }

    private fun constructCalendar(calendar: Calendar, event: CalendarEvent) {

        // Adding only future events. I am not adding this to the query's where clause because of pesky recurring events.
        //CalendarContract.Events.CONTENT_URI only fetches the root (or first) recurring event which could have happened in he past
        //Therefore, if I include this in the where clause all recurring events which had their root in the past would be skipped
        if (event.endTime!!.toLong() < java.util.Calendar.getInstance(Locale.CANADA).timeInMillis )
            return

        if ( userCalendars.containsKey(event.color) ) {
            userCalendars[event.color]!!.events.add(event)
        }
        else {
            calendar.events.add(event)
            userCalendars[event.color!!] = calendar
        }
    }

    private fun pingReccuringEvents(context: Context, eID: Long) : ArrayList<CalendarEvent> {

        val recurringEvents = arrayListOf<CalendarEvent>()

        val eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE)
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE)
        val eventsUri = eventsUriBuilder.build()
        try {
            val resolver = context.contentResolver
            // In this query's WHERE clause I specify that I only want the recurring events that match the passed eventID
            val cur: Cursor = resolver.query(eventsUri, Calendar.instances_projection.keys.toTypedArray(),
                "${CalendarContract.Instances.EVENT_ID} = '${eID}' AND " +
                "${CalendarContract.Instances.END} >= '${java.util.Calendar.getInstance(Locale.CANADA).timeInMillis}' AND " +
                 "${CalendarContract.Instances.STATUS} != ${CalendarContract.Instances.STATUS_CANCELED}",
                null,
                CalendarContract.Instances.BEGIN)!!

            while (cur.moveToNext()) {
                val eventID: Long = cur.getLong(Calendar.instances_projection.getValue(CalendarContract.Instances.EVENT_ID))
                val title: String? = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.TITLE))
                val location: String? = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.EVENT_LOCATION))
                val dtstart: String? = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.BEGIN))
                val dtend: String? = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.END))
                val color: String = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.DISPLAY_COLOR))
                val description: String? = cur.getString(Calendar.instances_projection.getValue(CalendarContract.Instances.DESCRIPTION))

                val event = CalendarEvent(title, eventID.toString(), location, dtstart, dtend, description, color)
                recurringEvents.add(event)
            }
            cur.close()
        } catch (e: SecurityException) {
            println("Security Exception when attempting to access calendar: ${e.printStackTrace()}")
        }
        return recurringEvents
    }
}