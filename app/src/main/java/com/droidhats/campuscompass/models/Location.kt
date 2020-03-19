package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.MarkerOptions

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
    private val centerLocation: LatLng,
    private val name: String,
    private val polygonCoordinatesList: List<LatLng>,
    private val address: String,
    private val openHours: String,
    private val departments: String,
    private val services: String
) : Location(coordinate) {
    private val polygonColor = 4289544510.toInt()
    private lateinit var polygon: Polygon
    private lateinit var marker: Marker

    fun getName(): String = name
    fun getLocation(): LatLng = coordinate
    fun getAddress(): String = address
    fun getDepartments(): String = departments
    fun getServices(): String = services
    fun getOpenHours(): String = openHours
    fun getPolygon(): Polygon = polygon
    fun getMarker(): Marker = marker

    fun setPolygon(polygon: Polygon){
        this.polygon = polygon
        this.polygon.tag = name
    }

    fun setMarker(marker: Marker){
        this.marker = marker
    }

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

    fun getMarkerOptions(): MarkerOptions{
        return MarkerOptions()
            .position(centerLocation).anchor(0.5f, 0.5f)
            .title(name)
    }

    /*
    A building does not have center location if in the buildings.json it is set to [0,0].
    This center location is used to place the Marker (initials) of specific buildings.
     */
    fun hasCenterLocation(): Boolean{
        return centerLocation != LatLng(0.0,0.0)
    }
}
