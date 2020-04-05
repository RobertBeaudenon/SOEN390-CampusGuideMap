package com.droidhats.campuscompass.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.ExplorePlaceDAO
import com.droidhats.campuscompass.roomdb.ExplorePlaceDB
import com.droidhats.campuscompass.roomdb.ExplorePlaceEntity

/**
 * This class will create a connection with the SQLite DB in order to get the
 * Places
 * @param application
 */
class ExplorePlaceRepository (private val application: Application)  {

    private var explorePlaceDAO: ExplorePlaceDAO
    private var allPlaces: LiveData<List<ExplorePlaceEntity>>

    companion object {
        // Singleton instantiation
        private var instance: ExplorePlaceRepository? = null

        fun getInstance(application: Application) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: ExplorePlaceRepository(application).also { instance = it }
                }
    }

    init {
        val db = ExplorePlaceDB.getInstance(application)
        explorePlaceDAO = db.ExplorePlaceDAO()
        allPlaces = explorePlaceDAO.getAllPlaces()
    }

    /**
     * @return allPlaces
     */
    fun getAllPlaces(): LiveData<List<ExplorePlaceEntity>> {
        return allPlaces
    }
}