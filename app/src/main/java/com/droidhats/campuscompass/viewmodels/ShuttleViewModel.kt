package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.roomdb.ShuttleBusLoyolaEntity
import com.droidhats.campuscompass.roomdb.ShuttleBusSGWEntity

class ShuttleViewModel(application: Application) : AndroidViewModel(application) {
    private  var navigationRepository: NavigationRepository = NavigationRepository.getInstance(getApplication())
    private  var loyolaShuttleTimes: LiveData<List<ShuttleBusLoyolaEntity>>
    private  var sgwShuttleTimes: LiveData<List<ShuttleBusSGWEntity>>

    init {
        loyolaShuttleTimes  =  navigationRepository.getLoyolaShuttleTime()
        sgwShuttleTimes = navigationRepository.getSGWShuttleTime()
    }

    /**
     * @return loyolaShuttleTimes
     */
    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBusLoyolaEntity>> {
        return loyolaShuttleTimes
    }

    /**
     *@return sgwShuttleTimes
     */
    fun getSGWShuttleTime(): LiveData<List<ShuttleBusSGWEntity>> {
        return sgwShuttleTimes
    }
}
