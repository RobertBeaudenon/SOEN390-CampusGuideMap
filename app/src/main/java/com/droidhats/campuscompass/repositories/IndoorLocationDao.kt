package com.droidhats.campuscompass.repositories

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.droidhats.campuscompass.models.IndoorLocation

/**
 * Data Access Object for the IndoorLocation class
 */

@Dao
interface IndoorLocationDao {
    /**
     * Returns ALL the rows in the IndoorLocation Table
     */
    @Query("SELECT * FROM IndoorLocation")
    fun getAll(): LiveData<List<IndoorLocation>>

    /**
     * Returns ALL the classrooms in the IndoorLocation Table
     */
    @Query("SELECT * FROM IndoorLocation WHERE location_type ='classroom'")
    fun getAllClassrooms(): LiveData<List<IndoorLocation>>

    /**
     * Runtime query used in search autocomplete
     */
    @RawQuery(observedEntities = [IndoorLocation::class])
    fun getMatchedClassrooms(query : SupportSQLiteQuery): LiveData<List<String>>

}