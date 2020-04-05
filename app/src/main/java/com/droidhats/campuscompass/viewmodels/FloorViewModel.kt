package com.droidhats.campuscompass.viewmodels


import androidx.lifecycle.ViewModel
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.repositories.IndoorNavigationRepository

class FloorViewModel: ViewModel() {
    val indoorLocationRepository: IndoorNavigationRepository = IndoorNavigationRepository.getInstance()

    fun getDirections(): Pair<IndoorLocation, IndoorLocation>? {
        return indoorLocationRepository.getStartAndEnd()
    }
}
