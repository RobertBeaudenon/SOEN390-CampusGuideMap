package com.droidhats.campuscompass.repositories

import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.models.GooglePlace

class FavoritesRepository {
    private lateinit var repository : HashMap<String, FavoritePlace>

    companion object {
        // Singleton instantiation
        private var instance: FavoritesRepository? = null

        fun getInstance() =
            instance
                ?: synchronized(this) {
                    instance
                        ?: FavoritesRepository().also { instance = it }
                }
    }

    // save place
    fun savePlace(placeToSave : FavoritePlace) {

    }


    // delete place
    fun removePlace(placeToRemove: FavoritePlace) {

    }

    // find place
    fun findPlace(placeId: String) {

    }

    fun removeById(placeID: String) {

    }

    fun save(location: GooglePlace) {

    }

    fun findById(placeID: String) {

    }
    // find all places

}