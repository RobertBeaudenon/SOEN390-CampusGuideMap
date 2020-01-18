package com.droidhats.campuscompass

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

//OnMapReadyCallback : interface ; extends AppCompatActivity() ;  GoogleMap.OnMarkerClickListener interface, which defines the onMarkerClick(), called when a marker is clicked or tapped:
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


         //update lastLocation with the new location and update the map with the new location coordinates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }


        createLocationRequest()
        handleCampusSwitch()
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

        //initializing vars for get last current location
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        //enable the zoom controls on the map and declare MapsActivity as the callback triggered when the user clicks a marker on this map
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        //will asks for users permission through a popup
        setUpMap()

        //1 enables the my-location layer which draws a light blue dot on the user’s location. It also adds a button to the map that, when tapped, centers the map on the user’s location.
        map.isMyLocationEnabled = true

        //2 gives you the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3 If  able to retrieve the the most recent location, then move the camera to the user’s current location.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    //implements methods of interface   GoogleMap.OnMarkerClickListener
     override fun onMarkerClick(p0: Marker?) = false

    // 1
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    //ask permision of user when accessing location
    companion object {
         private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        // 3 REQUEST_CHECK_SETTINGS is used as the request code passed to onActivityResult.
        private const val REQUEST_CHECK_SETTINGS = 2
    }

   //verifies that user has granted permission
    //checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
    private fun setUpMap() {
       if (ActivityCompat.checkSelfPermission(this,
               android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this,
               arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
           return
       }

       map.isMyLocationEnabled = true

       //updating map type we can choose between  4 types : MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
       map.mapType = GoogleMap.MAP_TYPE_TERRAIN

       fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
           // Got last known location. In some rare situations this can be null.
           if (location != null) {
               lastLocation = location
               val currentLatLng = LatLng(location.latitude, location.longitude)
               placeMarkerOnMap(currentLatLng) // we are adding the marker on map
               map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
           }
       }
    }

   //the Android Maps API lets you use a marker object, which is an icon that can be placed at a particular point on the map’s surface.
    private fun placeMarkerOnMap(location: LatLng) {
        // 1 Create a MarkerOptions object and sets the user’s current location as the position for the marker
        val markerOptions = MarkerOptions().position(location)

       //added a call to getAddress() and added this address as the marker title.
       val titleStr = getAddress(location)
       markerOptions.title(titleStr)


        // 2 Add the marker to the map
        map.addMarker(markerOptions)
    }

    //This method get address from coordinates
    private fun getAddress(latLng: LatLng): String {
        // 1 Creates a Geocoder object to turn a latitude and longitude coordinate into an address and vice versa
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2 Asks the geocoder to get the address from the location passed to the method
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3 If the response contains any address, then append it to a string and return
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    //get real time updates of current location
    private fun startLocationUpdates() {
        //1 if the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2 If there is permission, request for location updates.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    private fun createLocationRequest() {
        // 1  create an instance of LocationRequest, add it to an instance of LocationSettingsRequest.Builder and retrieve and handle any changes to be made based on the current state of the user’s location settings.
        locationRequest = LocationRequest()
        // 2   specifies the rate at which your app will like to receive updates.
        locationRequest.interval = 10000
        // 3 specifies the fastest rate at which the app can handle updates. Setting the fastestInterval rate places a limit on how fast updates will be sent to your app.
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4 check location settings before asking for location updates
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5 A task success means all is well and you can go ahead and initiate a location request
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6  A task failure means the location settings have some issues which can be fixed. This could be as a result of the user’s location settings turned off
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    // 1 Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Intent isnullable
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2 Override onPause() to stop location update request
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3 Override onResume() to restart the location update request.
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    //Handle the switching views between the two campuses. Should probably move from here later
    private fun handleCampusSwitch()
    {

  //TODO: refactor these coordinates into location
       val SGW_LAT  = 45.495637
       val SGW_LNG = -73.578235

       val LOYOLA_LAT = 45.458159
       val LOYOLA_LNG =  -73.640450

       var campusView : LatLng

       val campusToggle: ToggleButton = findViewById(R.id.toggle_Campus)

        //Setting toggle button text
        campusToggle.textOn = getString( R.string.SGW_Campus_Name )
        campusToggle.textOff = getString( R.string.Loyola_Campus_Name )

        //Setting Toggle button listener
        campusToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                campusView = LatLng(SGW_LAT, SGW_LNG)
                map.addMarker(MarkerOptions().position(campusView).title(getString( R.string.SGW_Campus_Name )))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(campusView, 15.0f))

            } else {
                campusView = LatLng(LOYOLA_LAT, LOYOLA_LNG)
                map.addMarker(MarkerOptions().position(campusView).title( getString( R.string.Loyola_Campus_Name ) ))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(campusView, 15.0f))
            }
        }
    }


}
