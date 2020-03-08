package com.droidhats.campuscompass.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ShuttleBus_DAO {

    @Insert
    fun saveLoyolaShuttleTime(shuttleBus: ShuttleBus_Loyola_Entity)

    @Query("select * from ShuttleBus_Loyola_Entity")
    fun getLoyolaShuttleTime() : LiveData<List<ShuttleBus_Loyola_Entity>>

    @Insert
    fun saveSGWShuttleTime(shuttleBus: ShuttleBus_SGW_Entity)

    @Query("select * from ShuttleBus_SGW_Entity")
    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>>
}