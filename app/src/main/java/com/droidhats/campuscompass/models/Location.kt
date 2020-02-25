package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng

/*
* Model for location classes
* Data relating to locations should be stored in this file
* */

abstract class Location(coordinate: LatLng) {
    private var coordinate: LatLng = coordinate
}

//
class Campus(coordinate: LatLng, name: String) : Location (coordinate) {
    private lateinit var buildings: Array<Building>
    private val name: String = name
    fun getName(): String = name
}

// Model for building class, data relating to buildings should be stored here
class Building(coordinate: LatLng, name: String, desc: String) : Location (coordinate) {
    private val name: String = name
    private val desc: String = desc

    fun getName(): String = name
    fun getDescription(): String = desc
}
