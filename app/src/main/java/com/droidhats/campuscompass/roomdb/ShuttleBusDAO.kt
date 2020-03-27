package com.droidhats.campuscompass.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * This class allows us to query our Database
 */
@Dao
interface ShuttleBusDAO {

    /**
     * Insert the timing and the day of a scheduled loyola shuttle bus
     * in the ShuttleBusloyolaEntity table
     * @param shuttleBus
     */
    @Insert
    fun saveLoyolaShuttleTime(shuttleBus: ShuttleBusLoyolaEntity)

    /**
     * Returns a list of the timing and the day of all scheduled loyola shuttle bus
     * @return: List of shuttleBusLoyolaEntity
     */
    @Query("select * from ShuttleBusLoyolaEntity")
    fun getLoyolaShuttleTime() : LiveData<List<ShuttleBusLoyolaEntity>>

    /**
     * Returns the number of rows in loyola shuttle bus table
     * @return: count
     */
    @Query("select count(*) from ShuttleBusLoyolaEntity")
    fun getLoyolaShuttleTimeCount() : Int

    /**
     * Insert the timing and the day of a scheduled sgw shuttle bus
     * in the ShuttleBusSGWEntity table
     * @param shuttleBus
     */
    @Insert
    fun saveSGWShuttleTime(shuttleBus: ShuttleBusSGWEntity)

    /**
     * Returns a list of the timing and the day of all scheduled sgw shuttle bus
     * @return: List of shuttleBusSGWEntity
     */
    @Query("select * from ShuttleBusSGWEntity")
    fun getSGWShuttleTime(): LiveData<List<ShuttleBusSGWEntity>>

    /**
     * Returns the number of rows in sgw shuttle bus table
     * @return: count
     */
    @Query("select count(*) from ShuttleBusSGWEntity")
    fun getSGWShuttleTimeCount() : Int
}