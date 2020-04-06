package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng

class NavigationRoute(
    var origin: Location?,
    var destination: Location?,
    var transportationMode: String?,
    var polyLinePath: MutableList<List<LatLng>>,
    var instructions: ArrayList<String>,
    var instructionsCoordinates: ArrayList<LatLng>
) {
    var routeTime: String? = null
    enum class TransportationMethods(val string: String) {
        WALKING("walking"),
        DRIVING("driving"),
        TRANSIT("transit"),
        BICYCLE("bicycling"),
        SHUTTLE("shuttle")
    }
}