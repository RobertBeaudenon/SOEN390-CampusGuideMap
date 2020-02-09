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
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import java.io.IOException
import java.util.Locale


//OnMapReadyCallback : interface ; extends AppCompatActivity() ;  GoogleMap.OnMarkerClickListener interface, which defines the onMarkerClick(), called when a marker is clicked or tapped:
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolygonClickListener {

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

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

        val calendarButton: View = findViewById(R.id.calendarButton)
        calendarButton.setOnClickListener {
            pingCalendar(this.applicationContext, this)
        }

        handleCampusSwitch()
        initPlacesSearch()
        initBottomSheetBehavior()

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

        //enable indoor level picker
        map.isIndoorEnabled = true
        map.getUiSettings().setIndoorLevelPickerEnabled(true)

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
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }

        drawBuildingPolygons()
        map.setOnPolygonClickListener(this)

        map.setOnMapClickListener {

            //Dismiss the bottom sheet when clicking anywhere on the map
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    //implements methods of interface GoogleMap.GoogleMap.OnPolygonClickListener
    override fun onPolygonClick(p: Polygon) {

        //Expand the bottom sheet when clicking on a polygon
        //TODO: Limt only to campus buildings as poylgons could highlight anything
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        //Populate the bottom sheet with building information
        val buildingNameText: TextView = findViewById(R.id.bottom_sheet_building_name)
        buildingNameText.text = p.tag.toString()
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

        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }

    //verifies that user has granted permission
    //checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        //updating map type we can choose between  4 types : MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng) // we are adding the marker on map
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage!!)
        }

        return addressText
    }

    //get real time updates of current location
    private fun startLocationUpdates() {
        //1 if the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2 If there is permission, request for location updates.
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
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
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun initPlacesSearch() {
        Places.initialize(this.applicationContext, getString(R.string.ApiKey), Locale.CANADA)
        Places.createClient(this)
        var fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)


        //Autocomplete search launches after hitting the button
        val searchButton: View = findViewById(R.id.fab_search)

        searchButton.setOnClickListener {
            var intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    // 1 Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { //Intent is nullable
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            locationUpdateState = true
            startLocationUpdates()
        }
        if (requestCode != AUTOCOMPLETE_REQUEST_CODE || resultCode != Activity.RESULT_OK) return

        if (data == null) return

        val place = Autocomplete.getPlaceFromIntent(data)
        Toast.makeText(this, place.address, Toast.LENGTH_LONG).show()

        if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status = Autocomplete.getStatusFromIntent(data)
            Log.i("Autocomplete: ", status.statusMessage!!)
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
    private fun handleCampusSwitch() {

        //TODO: refactor these coordinates into location
        val SGW_LAT = 45.495637
        val SGW_LNG = -73.578235

        val LOYOLA_LAT = 45.458159
        val LOYOLA_LNG = -73.640450

        var campusView: LatLng

        val campusToggle: ToggleButton = findViewById(R.id.toggle_Campus)

        //Setting toggle button text
        campusToggle.textOn = getString(R.string.SGW_Campus_Name)
        campusToggle.textOff = getString(R.string.Loyola_Campus_Name)

        //Setting Toggle button listener
        campusToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                campusView = LatLng(SGW_LAT, SGW_LNG)
                // map.addMarker(MarkerOptions().position(campusView).title(getString( R.string.SGW_Campus_Name )))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(campusView, 16.0f))

            } else {
                campusView = LatLng(LOYOLA_LAT, LOYOLA_LNG)
                map.addMarker(MarkerOptions().position(campusView).title(getString(R.string.Loyola_Campus_Name)))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(campusView, 16.0f))
            }
        }
    }


    private fun drawBuildingPolygons() {

        // SGW CAMPUS

        //EV Building
        val ev_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.495594, -73.578761),
                LatLng(45.495175, -73.577855),
                LatLng(45.495826, -73.577243),
                LatLng(45.496046, -73.577709),
                LatLng(45.495673, -73.578080),
                LatLng(45.495910, -73.578475)
            )
        val ev_Polygon: Polygon = map.addPolygon(ev_PolygonOptions)
        ev_Polygon.tag = getString(R.string.EV_Building_Name)

        val gm_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.495782, -73.579159),
                LatLng(45.495765, -73.579118),
                LatLng(45.495781, -73.579099),
                LatLng(45.495615, -73.578746),
                LatLng(45.495946, -73.578436),
                LatLng(45.496132, -73.578816)
            )
        val gm_Polygon: Polygon = map.addPolygon(gm_PolygonOptions)
        gm_Polygon.tag = getString(R.string.GM_Building_Name)

        // Hall Building
        val hall_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.497164, -73.579544),
                LatLng(45.497710, -73.579034),
                LatLng(45.497373, -73.578338),
                LatLng(45.496828, -73.578850)
            )
        val hall_Polygon: Polygon = map.addPolygon(hall_PolygonOptions)
        hall_Polygon.tag = getString(R.string.Hall_Building_Name)

        //JMSB Building
        val jmsb_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.495362, -73.579385),
                LatLng(45.495224, -73.579121),
                LatLng(45.495165, -73.579180),
                LatLng(45.495002, -73.578821),
                LatLng(45.495036, -73.578787),
                LatLng(45.495001, -73.578728),
                LatLng(45.495195, -73.578507),
                LatLng(45.495529, -73.579209)
            )
        val jmsb_Polygon: Polygon = map.addPolygon(jmsb_PolygonOptions)
        jmsb_Polygon.tag = getString(R.string.JMSB_Building_Name)

        //Library
        val lib_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.497283, -73.578079),
                LatLng(45.496682, -73.578637),
                LatLng(45.496249, -73.577675),
                LatLng(45.496487, -73.577457),
                LatLng(45.496582, -73.577651),
                LatLng(45.496634, -73.577604),
                LatLng(45.496615, -73.577560),
                LatLng(45.496896, -73.577279)
            )
        val lib_Polygon: Polygon = map.addPolygon(lib_PolygonOptions)
        lib_Polygon.tag = getString(R.string.WebsterLibrary_Building_Name)

        //FG Building
        val fg_PolygonOptions = PolygonOptions()
            .clickable(true)
            .add(
                LatLng(45.493822, -73.579069),
                LatLng(45.493619, -73.578735),
                LatLng(45.494457, -73.577627),
                LatLng(45.494687, -73.578045),
                LatLng(45.494363, -73.578439)
            )
        val fg_Polygon: Polygon = map.addPolygon(fg_PolygonOptions)
        fg_Polygon.tag = getString(R.string.FG_Building_Name)

    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change
                /* The following code can be used if we want to do certain actions related
                *  to the change of state of the bottom sheet
                * */

//                when (newState) {
//                    BottomSheetBehavior.STATE_HIDDEN -> {
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                    }
//                    BottomSheetBehavior.STATE_SETTLING -> {
//                    }
//                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
//                    }
//                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                // Adjusting the google zoom buttons to stay on top of the bottom sheet
                //Multiply the bottom sheet height by the offset to get the effect of them being anchored to the top of the sheet
                map.setPadding(0, 0, 0, (slideOffset * bottom_sheet.height).toInt())
            }
        })

    }


    //


}
