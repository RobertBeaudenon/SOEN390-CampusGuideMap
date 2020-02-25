package com.droidhats.campuscompass.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    //set as false to identify instantiations are not complete
    private val mutableLiveData = MutableLiveData<Boolean>().apply {
        value = false
    }

    fun init() {
        mutableLiveData.value = true //hard coded to show successful API connection
    }

}