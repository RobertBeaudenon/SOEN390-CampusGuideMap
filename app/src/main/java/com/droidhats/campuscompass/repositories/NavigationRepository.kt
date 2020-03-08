package com.droidhats.campuscompass.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.AppDB
import com.droidhats.campuscompass.roomdb.ShuttleBus_DAO
import com.droidhats.campuscompass.roomdb.ShuttleBus_Loyola_Entity
import com.droidhats.campuscompass.roomdb.ShuttleBus_SGW_Entity

class NavigationRepository {

    private  var shuttleBusDAO: ShuttleBus_DAO
    private  var loyolaShuttleTimes: LiveData<List<ShuttleBus_Loyola_Entity>>
    private  var sgwShuttleTimes: LiveData<List<ShuttleBus_SGW_Entity>>

    constructor(application: Application){
        val db = AppDB.getInstance(application)
        shuttleBusDAO = db!!.shuttleBusDAO()
        loyolaShuttleTimes = shuttleBusDAO.getLoyolaShuttleTime()
        sgwShuttleTimes = shuttleBusDAO.getSGWShuttleTime()
    }

    //to access times from View model
    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBus_Loyola_Entity>> {
        return loyolaShuttleTimes
    }

    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>> {
        return sgwShuttleTimes
    }
}