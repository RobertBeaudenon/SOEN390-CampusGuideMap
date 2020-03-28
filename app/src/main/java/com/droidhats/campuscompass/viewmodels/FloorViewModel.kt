package com.droidhats.campuscompass.viewmodels

import androidx.lifecycle.ViewModel
import com.droidhats.campuscompass.repositories.IndoorLocationRepository
import com.droidhats.campuscompass.repositories.IndoorNavigationRepository

class FloorViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val indoorLocationRepository: IndoorNavigationRepository = IndoorNavigationRepository.getInstance()

    fun getDirections(): Pair<Float, Float>? {
        return indoorLocationRepository.getStartAndEnd()
    }
}
