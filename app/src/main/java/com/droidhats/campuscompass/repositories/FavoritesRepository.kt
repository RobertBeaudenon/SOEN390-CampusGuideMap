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

    fun removePlace(placeID: String) {
        repository.remove(placeID)
    }

    fun savePlace(location: GooglePlace) {
        repository[location.placeID] = createFavoritePlace(location)
    }

    fun findPlaceById(placeID: String) : FavoritePlace? {
        if (repository.containsKey(placeID)) {
            return repository[placeID]
        } else {
            return null
        }
    }

    fun listAllPlaces() : List<FavoritePlace> = repository.values.toList()

    fun createFavoritePlace(googlePlace: GooglePlace) : FavoritePlace {
        val name : String = googlePlace.name
        val latitude : Double = googlePlace.coordinate.latitude
        val longitude : Double =  googlePlace.coordinate.longitude
        val address : String? = googlePlace.place?.address
        val placeId : String = googlePlace.placeID

        return FavoritePlace(placeId, name, latitude, longitude, address)
    }
}