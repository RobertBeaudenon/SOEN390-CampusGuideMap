package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.repositories.MapRepository
import java.io.InputStream

class  MapViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var campuses: List<Campus>? = null

    fun getCampuses(): List<Campus> = campuses!!

    // The activity is required to access the assets to open our json file where the info
    // is stored
    fun init() {
        val inputStream: InputStream = context.assets.open("buildings.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        campuses = MapRepository.getInstance(json).getCampuses()
    }

}
