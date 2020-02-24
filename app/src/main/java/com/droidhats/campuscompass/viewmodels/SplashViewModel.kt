package com.droidhats.campuscompass.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    //set as null to identify instantiations are not complete
    private var mutableLiveData = MutableLiveData<Boolean>().apply {
        value = false
    }

    //example of a value that can be accessed from SplashFragment
    val hardCode: LiveData<Boolean> = mutableLiveData

    fun init() {
        mutableLiveData.value = true //hard coded to show successful API connection
        returnData() //dummy function - represents Map initialization methods
    }

    private fun returnData(): LiveData<Boolean> {
        return mutableLiveData //returns updated value
    }
}