package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng

open class NavigationRoute(
    open var origin: Location?,
    open var destination: Location?
)

class OutdoorNavigationRoute(
    override var origin: Location?,
    override var destination: Location?,
    var transportationMode: String?,
    var polyLinePath: MutableList<List<LatLng>>,
    var instructions: ArrayList<String>,
    var instructionsCoordinates: ArrayList<LatLng>
) : NavigationRoute(origin, destination) {
    var routeTime: String? = null
    enum class TransportationMethods(val string: String) {
        WALKING("walking"),
        DRIVING("driving"),
        TRANSIT("transit"),
        BICYCLE("bicycling"),
        SHUTTLE("shuttle")
    }
}