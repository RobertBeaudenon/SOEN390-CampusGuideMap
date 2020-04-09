package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.roomdb.FavoritesDatabase

/*
  This class must extend AndroidViewModel instead of just ViewModel because
  ContentResolver requires the application context to be able to query calendar info
*/
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val favoritesDb: FavoritesDatabase

    init {
        favoritesDb = FavoritesDatabase.getInstance(context)
    }

    fun getFavorites() : List<FavoritePlace> {
        return favoritesDb.favoritePlacesDao().listAllPlaces()
    }

    // TODO: To be implemented
    fun getThumbnail() {
        return;
    }
}

