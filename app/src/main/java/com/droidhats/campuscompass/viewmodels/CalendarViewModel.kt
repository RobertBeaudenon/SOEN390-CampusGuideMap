package com.droidhats.campuscompass.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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

    private val _text = MutableLiveData<String>().apply {
        value = "Calendar"
    }
    val text: LiveData<String> = _text

   fun init() {
      userCalendars = CalendarRepository.getInstance().getCalendars(context)
    }

    fun getCalendars() : MutableLiveData<ArrayList<Calendar>>
    {
       return selectedCalendars
    }

    fun selectCalendars(selectedColors: BooleanArray) {

        val colorArray = CalendarFragment.googleCalendarColorMap.keys.toTypedArray()
        val filteredList = arrayListOf<Calendar>()
        //Get the Calendars of the selected colors
        for (i in selectedColors.indices) {
            if (selectedColors[i]) {
                val colorToAdd =
                    userCalendars.value!![CalendarFragment.googleCalendarColorMap[colorArray[i]]]
                if (colorToAdd != null)
                    filteredList.add(colorToAdd)
            }
        }
        selectedCalendars.value = filteredList

    }

}

