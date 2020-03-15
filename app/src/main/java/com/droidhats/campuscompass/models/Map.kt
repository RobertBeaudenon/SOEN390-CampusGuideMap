package com.droidhats.campuscompass.models

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.droidhats.campuscompass.MainActivity
import com.google.android.gms.maps.GoogleMap


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

    fun getMap(): GoogleMap {
        return googleMap
    }
}


