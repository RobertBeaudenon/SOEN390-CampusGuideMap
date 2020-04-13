package com.droidhats.campuscompass.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.models.GooglePlace

@Dao
interface FavoritePlacesDao {

    @Delete
    fun removePlace(place : FavoritePlace)

    @Insert
    fun savePlace(place: FavoritePlace)

    @Query("SELECT * FROM favoriteplace WHERE placeId = :placeID")
    fun findPlaceById(placeID: String) : FavoritePlace?

    @Query("SELECT * FROM favoriteplace")
    fun listAllPlaces() : List<FavoritePlace>

    fun createFavoritePlace(googlePlace: GooglePlace) : FavoritePlace {
        val name : String = googlePlace.name
        val latitude : Double = googlePlace.coordinate.latitude
        val longitude : Double =  googlePlace.coordinate.longitude
        val address : String? = googlePlace.place?.address
        val placeId : String = googlePlace.placeID
        googlePlace.place

        return FavoritePlace(placeId, name, latitude, longitude, address)
    }
}