package com.droidhats.campuscompass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Calendar Fragment"
    }
    val text: LiveData<String> = _text
}
