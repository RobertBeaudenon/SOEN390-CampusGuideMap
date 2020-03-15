package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.roomdb.ShuttleBus_Loyola_Entity
import com.droidhats.campuscompass.roomdb.ShuttleBus_SGW_Entity

class NavigationViewModel: AndroidViewModel {

    private  var navigationRepository: NavigationRepository
    public  var loyolaShuttleTimes: LiveData<List<ShuttleBus_Loyola_Entity>>
    private  var sgwShuttleTimes: LiveData<List<ShuttleBus_SGW_Entity>>

    constructor(application: Application) : super(application) {
        navigationRepository = NavigationRepository(application)
        loyolaShuttleTimes  =  navigationRepository.getLoyolaShuttleTime()
        sgwShuttleTimes = navigationRepository.getSGWShuttleTime()
    }

    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBus_Loyola_Entity>> {
        return loyolaShuttleTimes
    }

    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>> {
        return sgwShuttleTimes
    }
}