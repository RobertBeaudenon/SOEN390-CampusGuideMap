package com.droidhats.campuscompass

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/*
  This class must extend AndroidViewModel instead of just ViewModel because
  ContentResolver requires the application context to be able to query calendar info
*/
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var userCalendars = MutableLiveData<MutableMap<String, Calendar>>()

    private val _text = MutableLiveData<String>().apply {
        value = "Calendar"
    }
    val text: LiveData<String> = _text

   fun init()
    {
       userCalendars = CalendarRepository.getInstance().getCalendars(context)
    }

    fun getUserCalendars() : MutableLiveData<MutableMap<String, Calendar>>
    {
       return userCalendars
    }
}
