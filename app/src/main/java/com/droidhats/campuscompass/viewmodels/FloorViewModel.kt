package com.droidhats.campuscompass.viewmodels


import androidx.lifecycle.ViewModel
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.repositories.NavigationRepository

class FloorViewModel: ViewModel() {

    val navigationRepository: NavigationRepository? = NavigationRepository.getInstance()

    fun getDirections(): Pair<IndoorLocation, IndoorLocation>? {
        val navRoute = NavigationRepository.getInstance()?.getNavigationRoute()?.value
        if (navRoute?.origin is IndoorLocation && navRoute.destination is IndoorLocation) {
            return Pair(navRoute.origin as IndoorLocation, navRoute.destination as IndoorLocation)
        }
        else {
            return null
        }
    }

    fun consumeNavHandler() {
        navigationRepository?.consumeNavigationHandler()
    }
}
