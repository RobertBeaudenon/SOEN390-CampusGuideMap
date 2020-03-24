package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.droidhats.campuscompass.repositories.IndoorLocationRepository
import com.droidhats.campuscompass.roomdb.IndoorLocationDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    //set as false to identify instantiations are not complete
    private val mutableLiveData = MutableLiveData<Boolean>().apply {
        value = false
    }
    val initCode: LiveData<Boolean> = mutableLiveData

    fun init() {
        mutableLiveData.value = true //hard coded to show successful API connection
        val context = getApplication<Application>().applicationContext
        val indoorInitializer = IndoorLocationRepository.getInstance(
            IndoorLocationDatabase.getInstance(context).indoorLocationDao())
        GlobalScope.launch {
            indoorInitializer.initializeIndoorLocations(context)
        }

    }

}