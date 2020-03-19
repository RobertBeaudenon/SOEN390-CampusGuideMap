package com.droidhats.campuscompass.models

class NavigationRoute (
    var origin: Location?,
    var destination: Location?,
    var routeTime: String?
){
        enum class TransportationMethods(val string : String){
            WALKING("walking"),
            DRIVING("driving"),
            TRANSIT("transit"),
            BICYCLE("bicycling"),
            SHUTTLE("shuttle")

    }



}