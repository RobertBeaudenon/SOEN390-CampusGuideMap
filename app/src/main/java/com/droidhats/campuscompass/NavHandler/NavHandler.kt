package com.droidhats.campuscompass.NavHandler


import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.Location

abstract class NavHandler {
    var next: NavHandler? = null
    private var prev: NavHandler? = null
    abstract val location: Location

    companion object {
        fun initializeChain(origin: Location, destination: Location, selectedTransportationMode: String): NavHandler {
            val navigationHandler: NavHandler
            if (origin is IndoorLocation) {
                navigationHandler = IndoorNavStep(origin)
                if (destination is IndoorLocation) {
                    if (origin.floorMap == destination.floorMap) {
                        navigationHandler.setNext(IndoorNavStep(destination))
                    } else {
                        navigationHandler
                            .setNext(OutdoorNavStep(origin, selectedTransportationMode))
                            .setNext(OutdoorNavStep(destination, selectedTransportationMode))
                            .setNext(IndoorNavStep(destination))
                    }
                }
            } else {
                navigationHandler = OutdoorNavStep(origin, selectedTransportationMode)
                if (destination is IndoorLocation) {
                    navigationHandler
                        .setNext(OutdoorNavStep(destination, selectedTransportationMode))
                        .setNext(IndoorNavStep(destination))
                } else {
                    navigationHandler.setNext(OutdoorNavStep(destination, selectedTransportationMode))
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
