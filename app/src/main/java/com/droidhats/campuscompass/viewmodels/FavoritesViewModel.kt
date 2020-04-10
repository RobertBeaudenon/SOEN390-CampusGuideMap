package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.roomdb.FavoritesDatabase
import com.google.android.gms.maps.model.LatLng
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.asin
import kotlin.math.pow

/*
  This class must extend AndroidViewModel instead of just ViewModel because
  ContentResolver requires the application context to be able to query calendar info
*/
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val favoritesDb: FavoritesDatabase
    var currentLocation: LatLng? = null

    init {
        favoritesDb = FavoritesDatabase.getInstance(context)
    }

    fun getFavorites() : List<FavoritePlace> {
        return favoritesDb.favoritePlacesDao().listAllPlaces()
    }

    fun haversineDist(destination: LatLng) : Double? {
        if (currentLocation == null) return null
        val diffLat = Math.toRadians(destination.latitude - currentLocation!!.latitude)
        val diffLong = Math.toRadians(destination.longitude- currentLocation!!.longitude)

        val lat = Math.toRadians(currentLocation!!.latitude)
        val lat2 = Math.toRadians(destination.latitude)

        val rad = 6371.0
        val a = sin(diffLat / 2).pow(2.0) +
                sin(diffLong / 2).pow(2.0) *
                cos(lat) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return rad * c
    }
}

