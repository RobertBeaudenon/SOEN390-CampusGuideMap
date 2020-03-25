package com.droidhats.campuscompass.models

import androidx.fragment.app.FragmentActivity
import com.droidhats.campuscompass.MainActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

/**
 * A Model for the map.
 * This class has the objective of initializing the map, attaching listeners to it, and drawing the
 * polygons and markers for the buildings on the map. It follows a singelton design pattern because
 * we only need to initialize the map once.
 *
 * @constructor Creates an initialized GoogleMap object.
 * @param googleMap: A GoogleMap Object will be used to initialize the map.
 * @param mapFragmentOnMarkerClickListener: a listener for marker clicks that will be attached to the map.
 * @param mapFragmentOnPolygonClickListener: a listener for polygon clicks that will be attached to the map.
 * @param mapFragmentOnCameraIdleListener: a listener for when the camera is idle in the map.
 * @param activity: Used to check the location permission from the main activity.
 * @param buildings: A list of all concordia buildings that is used to draw map polygons and markers.
 */
class Map(
    var googleMap: GoogleMap,
    private var mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
    private var mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
    private var mapFragmentOnCameraIdleListener: GoogleMap.OnCameraIdleListener,
    private var activity: FragmentActivity,
    private var buildings: List<Building>
) {

    companion object {
        private const val MAP_PADDING_TOP = 200
        private const val MAP_PADDING_RIGHT = 15

        // Singleton instantiation
        private var instance: Map? = null

        fun getInstance(googleMap: GoogleMap,
                        mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
                        mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
                        mapFragmentOnCameraIdleListener: GoogleMap.OnCameraIdleListener,
                        activity: FragmentActivity,
                        buildings: List<Building>
        ) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: Map(googleMap,
                            mapFragmentOnMarkerClickListener,
                            mapFragmentOnPolygonClickListener,
                            mapFragmentOnCameraIdleListener,
                            activity,
                            buildings
                        ).also { instance = it }
                }
    }

    /**
     * Initializes the map, and attaches listeners to it.
     */
    init {
        //updating map type we can choose between  4 types : MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        //initializing vars for get last current location
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(mapFragmentOnMarkerClickListener)

        //enable the zoom controls on the map and declare MainActivity as the callback triggered when the user clicks a marker on this map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(mapFragmentOnMarkerClickListener)

        //enable indoor level picker
        googleMap.isIndoorEnabled = true
        googleMap.uiSettings.isIndoorLevelPickerEnabled = true

        //Current Location Icon has been adjusted to be at the bottom right sid eof the search bar.
        googleMap.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, 0)

        if ((activity as MainActivity).checkLocationPermission()) {
            //Enables the my-location layer which draws a light blue dot on the user’s location.
            // It also adds a button to the map that, when tapped, centers the map on the user’s location.
            googleMap.isMyLocationEnabled = true
        }

        googleMap.setOnPolygonClickListener(mapFragmentOnPolygonClickListener)
        googleMap.setOnCameraIdleListener(mapFragmentOnCameraIdleListener)

        // Draw the buildings polygons and markers
        for (building in buildings) {
            drawBuildingPolygon(building)
            setBuildingMarker(building)
        }

    }

    /**
     * Draws the polygon for a single building on the map
     */
    private fun drawBuildingPolygon(building: Building){
        googleMap.addPolygon(building.getPolygonOptions())?.tag = building.name
        val polygon = googleMap.addPolygon(building.getPolygonOptions())
        building.setPolygon(polygon!!)
    }

    /**
     * Draws the marker for a single building on the map
     */
    private fun setBuildingMarker(building: Building) {
        if(building.hasCenterLocation()) {
            val marker: Marker = googleMap.addMarker(building.getMarkerOptions())
            building.setMarker(marker)
            // Set the maker to become the new bitmap rather than the conventional map pin
            building.getMarker().setIcon(BitmapDescriptorFactory.fromResource(building.getMarkerResId()))
        }
    }


}


