package com.droidhats.campuscompass.viewmodels


import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.repositories.IndoorNavigationRepository

class FloorViewModel {
    val indoorLocationRepository: IndoorNavigationRepository = IndoorNavigationRepository.getInstance()

    fun getDirections(): Pair<IndoorLocation, IndoorLocation>? {
        return indoorLocationRepository.getStartAndEnd()
    }
}
