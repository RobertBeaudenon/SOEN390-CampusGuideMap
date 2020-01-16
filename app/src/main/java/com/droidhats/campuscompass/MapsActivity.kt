package com.droidhats.campuscompass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

//OnMapReadyCallback : interface ; extends AppCompatActivity() ;  GoogleMap.OnMarkerClickListener interface, which defines the onMarkerClick(), called when a marker is clicked or tapped:
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

/*        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        val myPlace = LatLng(45.495637, -73.578235)  // this is concordia latitude and longitude coordinate
        map.addMarker(MarkerOptions().position(myPlace).title("Concordia"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15.0f)) //centers marker on the screen, and control zoom on map: 0 full zoom out 20 max zoom in

        //enable the zoom controls on the map and declare MapsActivity as the callback triggered when the user clicks a marker on this map
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
    }

//implements methods of interface   GoogleMap.OnMarkerClickListener
override fun onMarkerClick(p0: Marker?) = false
}
