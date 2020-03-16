package com.droidhats.campuscompass.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.*

/**
 * This class will create a connection with the SQLite DB in order to get the
 * SGW and Loyola shuttle times
 * @param application
 */
class NavigationRepository {

    private  var shuttleBusDAO: ShuttleBus_DAO
    private  var loyolaShuttleTimes: LiveData<List<ShuttleBus_Loyola_Entity>>
    private  var sgwShuttleTimes: LiveData<List<ShuttleBus_SGW_Entity>>

    constructor(application: Application){
        val db = ShuttleBusDB.getInstance(application)
        shuttleBusDAO = db!!.shuttleBusDAO()
        loyolaShuttleTimes = shuttleBusDAO.getLoyolaShuttleTime()
        sgwShuttleTimes = shuttleBusDAO.getSGWShuttleTime()
    }

    /**
    * @return loyolaShuttleTimes
    */
    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBus_Loyola_Entity>> {
        return loyolaShuttleTimes
    }

    /**
     * @return sgwShuttleTimes
     */
    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>> {
        return sgwShuttleTimes
    }
}