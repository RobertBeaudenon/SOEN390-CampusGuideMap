package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.repositories.CalendarRepository

/*
  This class must extend AndroidViewModel instead of just ViewModel because
  ContentResolver requires the application context to be able to query calendar info
*/
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var userCalendars = MutableLiveData<MutableMap<String, Calendar>>()
    private var selectedCalendars = MutableLiveData<ArrayList<Calendar>>()

    private val _info = MutableLiveData<String>().apply {
        value = ""
    }
    // TODO: Change the following variable to be a LiveData variable
    var info: MutableLiveData<String> = _info

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
        var selectedColors = BooleanArray(GOOGLE_CALENDAR_COLOR_MAP.size)
    }

   fun init() {
      userCalendars = CalendarRepository.getInstance().getCalendars(context)
   }

    fun getCalendars() : MutableLiveData<ArrayList<Calendar>>
    {
       return selectedCalendars
    }

    fun selectCalendars() {

        val colorArray = GOOGLE_CALENDAR_COLOR_MAP.keys.toTypedArray()
        val filteredList = arrayListOf<Calendar>()
        //Get the Calendars of the selected colors
        for (i in selectedColors.indices) {
            if (selectedColors[i]) {
                println(GOOGLE_CALENDAR_COLOR_MAP[colorArray[i]])
                val colorToAdd =
                    userCalendars.value!![GOOGLE_CALENDAR_COLOR_MAP[colorArray[i]]]
                if (colorToAdd != null)
                    filteredList.add(colorToAdd)
            }
        }
        updateInfo(filteredList)
        selectedCalendars.value = filteredList
    }

    private fun updateInfo(list : ArrayList<Calendar>)
    {
        if (list.size == 0)
            info.value = "No Calendars Selected"
        else
            info.value= ""

        if (userCalendars.value.isNullOrEmpty())
            info.value = "Could Not Find Calendars"
        else
            info.value=""
    }


}

