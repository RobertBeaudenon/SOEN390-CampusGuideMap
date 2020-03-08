package com.droidhats.campuscompass.roomdb

import android.os.AsyncTask

public class InitDBAsync(db:AppDB): AsyncTask<Void, Void, Void>() {

    //I will use that class to fetch the times from the csv file that i will insert in DB

    private val  shuttlebusDao: ShuttleBus_DAO
    init{
        shuttlebusDao =  db.shuttleBusDAO()
    }
    override fun doInBackground(vararg params: Void): Void? {
        //shuttleBus_Dao.deleteAll()

        var loyola = ShuttleBus_Loyola_Entity("F", "14:30")
        shuttlebusDao.saveLoyolaShuttleTime(loyola)

        var sgw = ShuttleBus_SGW_Entity("MTWT", "16:30")
        shuttlebusDao.saveSGWShuttleTime(sgw)
        return null
    }
}