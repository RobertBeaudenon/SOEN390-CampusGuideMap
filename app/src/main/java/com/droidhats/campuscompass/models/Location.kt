package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions

/*
* Model for location classes
* Data relating to locations should be stored in this file
* */

abstract class Location(coordinate: LatLng) {
    private var coordinate: LatLng = coordinate
}

// Model for Campus class
class Campus(
    private val coordinate: LatLng,
    private val name: String,
    private val buildingsList: List<Building>
) : Location(coordinate) {

    fun getName(): String = name
    fun getCoordinate(): LatLng = coordinate
    fun getBuildings(): List<Building> = buildingsList
}

// Model for building class, data relating to buildings should be stored here
class Building(
    private val coordinate: LatLng,
    private val name: String,
    private val polygonCoordinatesList: List<LatLng>
) : Location(coordinate) {
    private val polygonColor = 4289544510.toInt()

    fun getName(): String = name
    fun getLocation(): LatLng = coordinate

    fun getPolygonOptions(): PolygonOptions {
        var polygonOptions = PolygonOptions()
            .fillColor(polygonColor)
            .strokeWidth(2F)
            .clickable(true)
        for (polygonCoordinate in polygonCoordinatesList) {
            polygonOptions.add(polygonCoordinate)
        }
        return polygonOptions
    }
}
