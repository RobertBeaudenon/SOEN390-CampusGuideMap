package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.roomdb.FavoritesDatabase
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.asin
import kotlin.math.pow

/*
  This class must extend AndroidViewModel instead of just ViewModel because
 "ContentResolver requires the application context to be able to query favorite place info"
*/
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val favoritesDb: FavoritesDatabase
    var currentLocation = MutableLiveData<LatLng>()

    init {
        favoritesDb = FavoritesDatabase.getInstance(context)
    }

    fun getFavorites() : List<FavoritePlace> {
        return favoritesDb.favoritePlacesDao().listAllPlaces()
    }

    fun haversineDist(destination: LatLng) : Double? {
        val diffLat = Math.toRadians(destination.latitude - currentLocation.value!!.latitude)
        val diffLong = Math.toRadians(destination.longitude- currentLocation.value!!.longitude)

        val lat = Math.toRadians(currentLocation.value!!.latitude)
        val lat2 = Math.toRadians(destination.latitude)

        val rad = 6371.0
        val a = sin(diffLat / 2).pow(2.0) +
                sin(diffLong / 2).pow(2.0) *
                cos(lat) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return rad * c
    }

    fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener{
           currentLocation.value = LatLng(it.latitude, it.longitude)
        }

    }
}

