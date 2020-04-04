package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.roomdb.ShuttleBusLoyolaEntity
import com.droidhats.campuscompass.roomdb.ShuttleBusSGWEntity

/**
 * This class will provide loyola and sgw shuttle times to the fragment class
 * And will interact with the NavigationRepository to fetch the data
 * @param application
 */
class NavigationViewModel(application: Application) : NavHandler(application) {

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

    override fun displayNav() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}