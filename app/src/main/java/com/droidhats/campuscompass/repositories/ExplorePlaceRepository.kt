package com.droidhats.campuscompass.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.R
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

    /**
     * Returns the explore place image from drawable resources
     * @param placeName: Used to map the place name to the place image.
     */
    private fun getPlaceImageResourceID(placeName: String): Int {

        // The id for the place image resource is of Int type
        // Return the place image resource id that corresponds to the place name
        return when (placeName) {
            "Restaurant Maison Prathet Thai" -> R.drawable.resto_RestaurantMaisonPrathetThai
            "Les Saisons de Corée" -> R.drawable.resto_LesSaisonsdeCorée
            "Bar-B-Barn" -> R.drawable.resto_Bar_B_Barn
            "Kazu" -> R.drawable.resto_Kazu
            "Bacaro Pizzeria" -> R.drawable.resto_BacaroPizzeria
            "Garage Beirut" -> R.drawable.resto_GarageBeirut
            "Da Vinci Ristorante" -> R.drawable.resto_DaVinciRistorante
            else -> Log.v("ImageError", "couldn't load image")
        }
    }
}