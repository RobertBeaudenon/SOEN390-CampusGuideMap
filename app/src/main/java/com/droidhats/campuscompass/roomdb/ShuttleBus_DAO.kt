package com.droidhats.campuscompass.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * This class allows us to query our Database
 */
@Dao
interface ShuttleBus_DAO {

    /**
     * Insert the timing and the day of a scheduled loyola shuttle bus
     * in the ShuttleBus_loyola_Entity table
     * @param shuttleBus
     */
    @Insert
    fun saveLoyolaShuttleTime(shuttleBus: ShuttleBus_Loyola_Entity)

    /**
     * Returns a list of the timing and the day of all scheduled loyola shuttle bus
     * @return: List of shuttleBus_Loyola_Entity
     */
    @Query("select * from ShuttleBus_Loyola_Entity")
    fun getLoyolaShuttleTime() : LiveData<List<ShuttleBus_Loyola_Entity>>

    /**
     * Returns the number of rows in loyola shuttle bus table
     * @return: count
     */
    @Query("select count(*) from ShuttleBus_Loyola_Entity")
    fun getLoyolaShuttleTimeCount() : Int

    /**
     * Insert the timing and the day of a scheduled sgw shuttle bus
     * in the ShuttleBus_SGW_Entity table
     * @param shuttleBus
     */
    @Insert
    fun saveSGWShuttleTime(shuttleBus: ShuttleBus_SGW_Entity)

    /**
     * Returns a list of the timing and the day of all scheduled sgw shuttle bus
     * @return: List of shuttleBus_SGW_Entity
     */
    @Query("select * from ShuttleBus_SGW_Entity")
    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>>

    /**
     * Returns the number of rows in sgw shuttle bus table
     * @return: count
     */
    @Query("select count(*) from ShuttleBus_SGW_Entity")
    fun getSGWShuttleTimeCount() : Int
}