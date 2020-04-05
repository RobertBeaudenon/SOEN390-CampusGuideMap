package com.droidhats.campuscompass.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * This class allows us to query our Database
 */
@Dao
interface ExplorePlaceDAO {

    /**
     * Insert the timing and the day of a scheduled loyola shuttle bus
     * in the ExplorePlaceEntity table
     * @param place
     */
    @Insert
    fun saveExplorePlace(place: ExplorePlaceEntity)

    /**
     * Returns a list of all the places
     * @return: List of ExplorePlaceEntity
     */
    @Query("select * from ExplorePlaceEntity")
    fun getAllPlaces() : LiveData<List<ExplorePlaceEntity>>

    /**
     * Returns the number of rows ExplorePlace table
     * @return: count
     */
    @Query("select count(*) from ExplorePlaceEntity")
    fun getPlacesCount() : Int


}