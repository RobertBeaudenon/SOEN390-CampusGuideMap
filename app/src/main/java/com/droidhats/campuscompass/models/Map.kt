package com.droidhats.campuscompass.models

import androidx.fragment.app.FragmentActivity
import com.droidhats.campuscompass.MainActivity
import com.google.android.gms.maps.GoogleMap

/**
 * A Model for the map.
 * This class has the objective of initializing the map and attaching listeners to it.
 *
 * @constructor Creates an initialized GoogleMap object.
 * @param googleMap: A GoogleMap Object will be used to initialize the map.
 * @param mapFragmentOnMarkerClickListener: a listener for marker clicks that will be attached to the map.
 * @param mapFragmentOnPolygonClickListener: a listener for polygon clicks that will be attached to the map.
 * @param activity: Used to check the location permission from the main activity.
 */
class Map(
    var googleMap: GoogleMap,
    var mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
    var mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
    var activity: FragmentActivity
) {

    companion object {
        private const val MAP_PADDING_TOP = 200
        private const val MAP_PADDING_RIGHT = 15
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
    }

    /**
     * @return the initialized GoogleMap.
     */
    fun getMap(): GoogleMap {
        return googleMap
    }
}


