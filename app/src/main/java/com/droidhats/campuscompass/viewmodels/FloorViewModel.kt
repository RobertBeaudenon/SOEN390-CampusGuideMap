package com.droidhats.campuscompass.viewmodels

import androidx.lifecycle.ViewModel
import com.droidhats.campuscompass.repositories.IndoorLocationRepository
import com.droidhats.campuscompass.repositories.IndoorNavigationRepository

class FloorViewModel : NavHandler() {
    // TODO: Implement the ViewModel
    val indoorLocationRepository: IndoorNavigationRepository = IndoorNavigationRepository.getInstance()

    fun getDirections(): Pair<String, String>? {
        return indoorLocationRepository.getStartAndEnd()
    }

    override fun displayNav() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
