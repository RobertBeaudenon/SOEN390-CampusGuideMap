package com.droidhats.campuscompass.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.model.Place

/*
* Model for location classes
* Data relating to locations should be stored in this file
* */

abstract class Location {
    abstract val name: String
    abstract val coordinate: LatLng
}

// Model for Campus class
class Campus(
    override val coordinate: LatLng,
    override val name: String,
    private val buildingsList: List<Building>
) : Location() {

    fun getLocation(): LatLng = coordinate
    fun getBuildings(): List<Building> = buildingsList
}

// Model for building class, data relating to buildings should be stored here
class Building(
    override val coordinate: LatLng,
    override val name: String,
    private val polygonCoordinatesList: List<LatLng>,
    private val address: String,
    private val openHours: String,
    private val departments: String,
    private val services: String
) : Location(){
    private val polygonColor = 4289544510.toInt()
    private lateinit var polygon: Polygon

    fun getLocation(): LatLng = coordinate
    fun getAddress(): String = address
    fun getDepartments(): String = departments
    fun getServices(): String = services
    fun getOpenHours(): String = openHours
    fun getPolygon(): Polygon = polygon

    fun setPolygon(polygon: Polygon){
        this.polygon = polygon
        this.polygon.tag = name
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
}

class GooglePlace(
    val placeID : String,
    override val name: String,
    override var coordinate: LatLng
) : Location()
{
    var place : Place? = null

}
