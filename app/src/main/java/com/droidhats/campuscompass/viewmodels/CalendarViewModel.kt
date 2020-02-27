package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.repositories.CalendarRepository
import com.droidhats.campuscompass.views.CalendarFragment

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
    var info: MutableLiveData<String> = _info

    //The colors available on the google calendar app with their corresponding int value
    val googleCalendarColorMap : Map<String, String> = mapOf(
        "Default" to "-6299161",
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
    var selectedColors = BooleanArray(googleCalendarColorMap.size)

   fun init() {
      userCalendars = CalendarRepository.getInstance().getCalendars(context)
   }

    fun getCalendars() : MutableLiveData<ArrayList<Calendar>>
    {
       return selectedCalendars
    }

    fun selectCalendars() {

        val colorArray = googleCalendarColorMap.keys.toTypedArray()
        val filteredList = arrayListOf<Calendar>()
        //Get the Calendars of the selected colors
        for (i in selectedColors.indices) {
            if (selectedColors[i]) {
                val colorToAdd =
                    userCalendars.value!![googleCalendarColorMap[colorArray[i]]]
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

