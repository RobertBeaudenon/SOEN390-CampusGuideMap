package com.droidhats.campuscompass

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.lifecycle.MutableLiveData


class CalendarRepository {

    private lateinit var userCalendars : ArrayList<Calendar>// The user's retrieved calendars

    companion object {
   // Singleton instantiation
        private var instance: CalendarRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: CalendarRepository().also { instance = it }
            }
    }

    fun getCalendars(context: Context) : MutableLiveData<ArrayList<Calendar>>
    {
        pingCalendars(context)
        return MutableLiveData<ArrayList<Calendar>>().apply {
            value = userCalendars
        }
    }

    private fun pingCalendars(context: Context) {

        userCalendars = arrayListOf()

        // Run query
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        try {
            val resolver = context.contentResolver
            val cur: Cursor = resolver.query(uri, Calendar.calendar_projection.keys.toTypedArray(), null, null, null)!!

            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(Calendar.calendar_projection.getValue(CalendarContract.Calendars._ID))
                val displayName: String = cur.getString(Calendar.calendar_projection.getValue(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                val accountName: String = cur.getString(Calendar.calendar_projection.getValue(CalendarContract.Calendars.ACCOUNT_NAME))
                val ownerName: String = cur.getString(Calendar.calendar_projection.getValue(CalendarContract.Calendars.OWNER_ACCOUNT))

               //Construct the calendar and add it to the list of user calendars
                val calendar = Calendar(calID.toString(), accountName, displayName, ownerName)
                println(calendar.toString())
                userCalendars.add(calendar)
            }
            cur.close()
        } catch (e: SecurityException) {
            println("Caught Security Exception when attempting to access calendar")
            println("The user didn't accept to access the calendar")
            println("Here is the error stack: ${e.printStackTrace()}")
        }
    }
}