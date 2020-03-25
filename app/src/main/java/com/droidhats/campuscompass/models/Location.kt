package com.droidhats.campuscompass.models

import android.os.Parcelable
import com.droidhats.campuscompass.helpers.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.parcel.Parcelize

/**
* An abstract class for other location model classes
*/
abstract class Location {
    abstract val name: String
    abstract val coordinate: LatLng
}

/**
 * Model for Campus
 */
class Campus(
    override val coordinate: LatLng,
    override val name: String,
    private val buildingsList: List<Building>
) : Location() {

    fun getLocation(): LatLng = coordinate
    fun getBuildings(): List<Building> = buildingsList
}

/**
 * Model for building class, data relating to buildings should be stored here
 */
@Parcelize
class Building(
    override val coordinate: LatLng,
    override val name: String,
	private val centerLocation: LatLng,
    private val polygonCoordinatesList: List<LatLng>,
    private val address: String,
    private val placeId: String,
    private val openHours: String,
    private val departments: String,
    private val services: String,
    private val imageResId: Int,
    private val markerResId: Int
) : Location(), Parcelable, Observer {
    private lateinit var polygon: Polygon
    private lateinit var marker: Marker

    companion object {
        private const val POLYGON_COLOR = 4289544510.toInt()
        private const val MARKER_VISIBILITY_ZOOM_LEVEL = 16f
    }

    fun getLocation(): LatLng = coordinate
    fun getAddress(): String = address
    fun getPlaceId(): String = placeId
    fun getDepartments(): String = departments
    fun getServices(): String = services
    fun getOpenHours(): String = openHours
    fun getPolygon(): Polygon = polygon
    fun getImageResId(): Int = imageResId
    fun getMarkerResId(): Int = markerResId
    fun getMarker(): Marker = marker

    fun setPolygon(polygon: Polygon){
        this.polygon = polygon
        this.polygon.tag = name
    }

    fun setMarker(marker: Marker){
        this.marker = marker
    }

    fun getPolygonOptions(): PolygonOptions {
        val polygonOptions = PolygonOptions()
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

class GooglePlace(
    val placeID : String,
    override val name: String,
    val category : String,
    override var coordinate: LatLng
) : Location()
{
    var place : Place? = null
    var isCurrentLocation : Boolean = false
}
