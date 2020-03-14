package com.droidhats.campuscompass.repositories

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.droidhats.campuscompass.models.IndoorLocation

class IndoorLocationRepository private constructor(private val indoorLocationDao: IndoorLocationDao) {

    companion object {
        // Singleton instantiation
        private var instance: IndoorLocationRepository? = null

        fun getInstance(indoorLocationDao : IndoorLocationDao ) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: IndoorLocationRepository(indoorLocationDao).also { instance = it }
                }
    }

    fun getIndoorLocations() : LiveData<List<IndoorLocation>> = indoorLocationDao.getAll()
    fun getClassrooms() : LiveData<List<IndoorLocation>> = indoorLocationDao.getAllClassrooms()
    fun getMatchedClassrooms(query : SimpleSQLiteQuery) : LiveData<List<String>> = indoorLocationDao.getMatchedClassrooms(query)
}

