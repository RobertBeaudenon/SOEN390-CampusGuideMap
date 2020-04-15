package com.droidhats.campuscompass.NavHandler


import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.Location

abstract class NavHandler {
    var next: NavHandler? = null
    var prev: NavHandler? = null
    abstract val location: Location

    companion object {
        fun initializeChain(origin: Location, destination: Location, selectedTransportationMode: String, wayPoints: String): NavHandler {
            val navigationHandler: NavHandler
            if (origin is IndoorLocation) {
                navigationHandler = IndoorNavStep(origin)
                if (destination is IndoorLocation) {
                    if (origin.buildingIndex == destination.buildingIndex) {
                        navigationHandler.setNext(IndoorNavStep(destination))
                    } else {
                        navigationHandler
                            .setNext(OutdoorNavStep(origin, selectedTransportationMode, wayPoints))
                            .setNext(IndoorNavStep(destination))
                    }
                } else {
                    navigationHandler
                        .setNext(OutdoorNavStep(origin, selectedTransportationMode, wayPoints))
                        .setNext(OutdoorNavStep(destination, selectedTransportationMode, wayPoints))
                }
            } else {
                navigationHandler = OutdoorNavStep(origin, selectedTransportationMode, wayPoints)
                if (destination is IndoorLocation) {
                    navigationHandler.setNext(IndoorNavStep(destination))
                } else {
                    navigationHandler.setNext(OutdoorNavStep(destination, selectedTransportationMode, wayPoints))
                }
            }
            return navigationHandler
        }
    }

    fun setNext(navHandler: NavHandler): NavHandler {
        next = navHandler
        navHandler.prev = this
        return next!!
    }

    abstract fun getNavigationRoute()
}
