package com.droidhats.campuscompass.models

import com.droidhats.campuscompass.helpers.Observer
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Model for Location, data pertaining to location are stored here
 */
abstract class Location(coordinate: LatLng) {
    private var coordinate: LatLng = coordinate
}

/**
 * Model for Campus
 */
class Campus(
    private val coordinate: LatLng,
    private val name: String,
    private val buildingsList: List<Building>
) : Location(coordinate) {

    fun getName(): String = name
    fun getCoordinate(): LatLng = coordinate
    fun getBuildings(): List<Building> = buildingsList
}

/**
 * Model for Building, data relating to buildings are stored here
 */
class Building(
    private val coordinate: LatLng,
    private val centerLocation: LatLng,
    private val name: String,
    private val polygonCoordinatesList: List<LatLng>,
    private val address: String,
    private val openHours: String,
    private val departments: String,
    private val services: String
) : Location(coordinate), Observer {

    private lateinit var polygon: Polygon
    private lateinit var marker: Marker

    companion object {
        private const val POLYGON_COLOR = 4289544510.toInt()
        private const val MARKER_VISIBILITY_ZOOM_LEVEL = 16f
    }

    fun getName(): String = name
    fun getLocation(): LatLng = coordinate
    fun getAddress(): String = address
    fun getDepartments(): String = departments
    fun getServices(): String = services
    fun getOpenHours(): String = openHours
    fun getCenterLocation(): LatLng = centerLocation
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
            .fillColor(POLYGON_COLOR)
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

    /**
     * A building has a center location if in the buildings.json it is set to a coordinate other than [0,0]
     * @return center location used to place the Marker holding the initials of the building
     */
    fun hasCenterLocation(): Boolean{
        return centerLocation != LatLng(0.0,0.0)
    }

    /**
     * A building's marker is visible only at its visibility zoom level
     */
    override fun update(mapZoomLevel: Float) {
        marker.isVisible = mapZoomLevel >= MARKER_VISIBILITY_ZOOM_LEVEL
    }
}
