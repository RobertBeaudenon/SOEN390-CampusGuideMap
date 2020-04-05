package com.droidhats.campuscompass.NavHandler

import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.models.NavigationRoute
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.google.android.gms.maps.model.LatLng

class OutdoorNavStep(override val location: Location, private val selectedTransportationMode: String) : NavHandler() {

    override fun getNavigationRoute() {
        if (next?.location != null)
            NavigationRepository.getInstance()?.generateDirections(location, next!!.location, selectedTransportationMode)
    }

}