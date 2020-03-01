package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.repositories.CalendarRepository

/*
  This class must extend AndroidViewModel instead of just ViewModel because
  ContentResolver requires the application context to be able to query calendar info
*/
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    internal var userCalendars = MutableLiveData<MutableMap<String, Calendar>>()
    internal var selectedCalendars = MutableLiveData<ArrayList<Calendar>>()

    private val _info = MutableLiveData<String>().apply {
        value = ""
    }
    var info: LiveData<String> = _info

    //The colors available on the google calendar app with their corresponding int value
    companion object {

        val GOOGLE_CALENDAR_COLOR_MAP: Map<String, String> = mapOf(
            "Default" to "Any",
            "Tomato" to "-2350809",
            "Tangerine" to "-18312",
            "Banana" to "-272549",
            "Basil" to "-11421879",
            "Sage" to "-8722497",
            "Peacock" to "-12134693",
            "Blueberry" to "-11238163",
            "Lavender" to "-5980676",
            "Grape" to "-2380289",
            "Flamingo" to "-30596",
            "Graphite" to "-1973791"
        )
    }
    //array initialized to false to keep track of colors that we want to set for the calendar event
    var selectedColors = BooleanArray(GOOGLE_CALENDAR_COLOR_MAP.size)

   fun init() {
       //we fetch all the different calendars within the same calendar based on the colors that the user has preset.
      userCalendars = CalendarRepository.getInstance().getCalendars(context)
   }

    fun getCalendars() : MutableLiveData<ArrayList<Calendar>> = selectedCalendars

    fun selectCalendars() {
        //constant map of the colors, convert map to array
        val colorArray = GOOGLE_CALENDAR_COLOR_MAP.keys.toTypedArray()
        //calendar is the color that we pick,
        val filteredList = arrayListOf<Calendar>()

        var selectedButNoCalendarsFound = 0

        //Get the Calendars of the selected colors
        for (i in selectedColors.indices) {
            if (selectedColors[i]) {
                //Storing the calendar object specific to the color we selected, fetched from repo
                val colorToAdd =
                    userCalendars.value!![GOOGLE_CALENDAR_COLOR_MAP[colorArray[i]]]

                //if the calendar specific to the color selected actually exist then set it
                if (colorToAdd != null)
                    filteredList.add(colorToAdd)
                else
                    selectedButNoCalendarsFound++
            }
        }
        //display text on screen
        updateInfo(filteredList, selectedButNoCalendarsFound)
        //pass the calendars to view
        selectedCalendars.value = filteredList
    }

    private fun updateInfo(list : ArrayList<Calendar>, selectedButNotFound : Int) {
        if (list.size == 0 && selectedButNotFound == 0)
            _info.value = "No Calendars Selected"
        else if (selectedButNotFound > 0 && list.size ==0)
            _info.value= "No Calendars Found\nfor Current Selection(s)"
        else
            _info.value = ""

        if (userCalendars.value.isNullOrEmpty())
            _info.value = "Could Not Find Calendars"
    }
}

