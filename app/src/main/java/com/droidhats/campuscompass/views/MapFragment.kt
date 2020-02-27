package com.droidhats.campuscompass.views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.CalendarEvent
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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import android.widget.Toast
import com.android.volley.Response
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolygonClickListener, CalendarFragment.OnCalendarEventClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mapFragment = inflater.inflate(R.layout.map_fragment, container, false)
        return mapFragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)

        if(activity != null)
            {
                val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)

        //update lastLocation with the new location and update the map with the new location coordinates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
            }
        }

        //Use this Fragment's implemented calendar event click callback
        CalendarFragment.onCalendarEventClickListener = this

        createLocationRequest()
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

        //updating map type we can choose between  4 types : MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        //initializing vars for get last current location
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        //enable the zoom controls on the map and declare MainActivity as the callback triggered when the user clicks a marker on this map
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        //enable indoor level picker
        map.isIndoorEnabled = true
        map.getUiSettings().setIndoorLevelPickerEnabled(true)

        //Enables the my-location layer which draws a light blue dot on the user’s location.
        // It also adds a button to the map that, when tapped, centers the map on the user’s location.
        map.isMyLocationEnabled = true

        //Gives you the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(activity as Activity) { location ->
            // Got last known location. In some rare situations this can be null.
            // If  able to retrieve the the most recent location, then move the camera to the user’s current location.
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
        val client = LocationServices.getSettingsClient(activity as Activity)
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
                        activity as Activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    //get real time updates of current location
    private fun startLocationUpdates() {
        //If the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        if (ActivityCompat.checkSelfPermission(
                activity as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //If there is permission, request for location updates.
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null //* Looper *//*
        )
    }


    // 1 Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            locationUpdateState = true
            startLocationUpdates()
        }
        if (requestCode != AUTOCOMPLETE_REQUEST_CODE || resultCode != Activity.RESULT_OK) return

        if (data == null) return

        val place = Autocomplete.getPlaceFromIntent(data)
        Toast.makeText(activity as Activity, place.address, Toast.LENGTH_LONG).show()

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
    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    //implements methods of interface GoogleMap.GoogleMap.OnPolygonClickListener
    override fun onPolygonClick(p: Polygon) {


        //Expand the bottom sheet when clicking on a polygon
        //TODO: Limt only to campus buildings as poylgons could highlight anything
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        //Checking which transportation mode is selected, default is walking.
        var transportationMode: String = "driving"
        var radioSelectedId = radioTransportGroup.checkedRadioButtonId
        when (radioSelectedId) {
            R.id.drivingId -> {
                transportationMode = "driving"
            }
            R.id.walkingId -> {
                transportationMode = "walking"
            }
            R.id.bicyclingId -> {
                transportationMode = "bicycling"
            }
            R.id.shuttleId -> {
                transportationMode = "shuttle"
            }
        }

        //In case the transportation mode is changed, this will capture it.
        radioTransportGroup.setOnCheckedChangeListener { _, optionId ->
            when (optionId) {
                R.id.drivingId -> {
                    transportationMode = "driving"
                }
                R.id.walkingId -> {
                    transportationMode = "walking"
                }
                R.id.bicyclingId -> {
                    transportationMode = "bicycling"
                }
                R.id.shuttleId -> {
                    transportationMode = "shuttle"
                }
            }
        }

        //Populate the bottom sheet with building information
        val buildingNameText: TextView = activity!!.findViewById(R.id.bottom_sheet_building_name)
        buildingNameText.text = p.tag.toString()

        val directionsButton: Button = activity!!.findViewById(R.id.bottom_sheet_directions_button)
        directionsButton.setOnClickListener(View.OnClickListener {

            //Calculating the center of the polygon to use for it's location.
            // This won't be necessary once we hold the Buildings in a common class
            var centerLat: Double = 0.0
            var centerLong: Double = 0.0
            for (i in 0 until p.points.size) {
                centerLat += p.points[i].latitude
                centerLong += p.points[i].longitude
            }
            centerLat /= p.points.size
            centerLong /= p.points.size

            val buildingLocation: Location = lastLocation
            buildingLocation.latitude = centerLat
            buildingLocation.longitude = centerLong

            //TODO: This full clear and redraw should probably be removed when the directions system is implemented.
            // It was added to show only one route at a time
            map.clear()
            drawBuildingPolygons()
            placeMarkerOnMap(LatLng(centerLat, centerLong))

            //Generate directions from current location to the selected building
            fusedLocationClient.lastLocation.addOnSuccessListener(activity as Activity) { location ->
                if (location != null) {
                    generateDirections(location, buildingLocation, transportationMode)
                }
                //Move the camera to the starting location
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 16.0f
                    )
                )
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        })
    }

    //implements methods of interface   GoogleMap.OnMarkerClickListener
    override fun onMarkerClick(p0: Marker?) = false

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
        val geocoder = Geocoder(activity as Activity)
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
            Log.e("MapFragment", e.localizedMessage!!)
        }

        return addressText
    }

    private fun initPlacesSearch() {
        Places.initialize(activity as Activity, getString(R.string.ApiKey), Locale.CANADA)
        Places.createClient(activity as Activity)
        var fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)


        //Autocomplete search launches after hitting the button
        val searchButton: View = activity!!.findViewById(R.id.fab_search)

        searchButton.setOnClickListener {
            var intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(activity as Activity)
            startActivityForResult(intent,
                AUTOCOMPLETE_REQUEST_CODE
            )
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

        val drawer : DrawerLayout = activity!!.findViewById(R.id.drawer_layout)
        val side_nav : NavigationView = activity!!.findViewById(R.id.nav_view)
        val drawer_content : LinearLayout = side_nav.menu.findItem(R.id.nav_drawer_main_content_item).actionView as LinearLayout
        val campusToggle : Switch = drawer_content.findViewById(R.id.toggle_Campus)

        //Setting Toggle button listener
        campusToggle.setOnCheckedChangeListener { _, isChecked ->
            drawer.closeDrawers()
            if (isChecked) {
                campusView = LatLng(LOYOLA_LAT, LOYOLA_LNG)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(campusView, 16.0f))

            } else {
                campusView = LatLng(SGW_LAT, SGW_LNG)
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
                // The following code can be used if we want to do certain actions related
                 // to the change of state of the bottom sheet
                 //

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

    private fun generateDirections(origin: Location, destination: Location, mode: String) {

        //Directions URL to be sent
        val directionsURL = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude.toString() + "," + origin.longitude.toString() +
                "&destination=" + destination.latitude.toString() + "," + destination.longitude.toString() +
                "&mode=" + mode +
                "&key=" + getString(R.string.ApiKey)

        //Creating the HTTP request with the directions URL
        val directionsRequest = object : StringRequest(
            Method.GET,
            directionsURL,
            Response.Listener<String> { response ->

                //Retrieve response (a JSON object)
                val jsonResponse = JSONObject(response)

                // Get route information from json response
                val routesArray = jsonResponse.getJSONArray("routes")
                val routes = routesArray.getJSONObject(0)
                val legsArray: JSONArray = routes.getJSONArray("legs")
                val legs = legsArray.getJSONObject(0)
                val steps = legsArray.getJSONObject(0).getJSONArray("steps")
                val totalKm:JSONObject = legs.getJSONObject("distance")
                val travelTime:JSONObject = legs.getJSONObject("duration")

                //Debug
                for (x in 1..3) {
                    Toast.makeText(activity, "The selected Transportation mode is: $mode. Total distance is: ${totalKm.getString("text")}. Total travel time is: ${travelTime.getString("text")}", Toast.LENGTH_LONG).show()
                }

                val path: MutableList<List<LatLng>> = ArrayList()

                //Build the path polyline
                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    val instructions = steps.getJSONObject(i)
                        .getString("html_instructions")  //Getting the route instructions
                    path.add(PolyUtil.decode(points))
                }
                //Draw the path polyline
                for (i in 0 until path.size) {
                    this.map.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            },
            Response.ErrorListener {
                Log.e("Volley Error:", "HTTP response error")
            }) {}

        //Confirm and add the request with Volley
        val requestQueue = Volley.newRequestQueue(activity as Activity)
        requestQueue.add(directionsRequest)
    }

    override fun onCalendarEventClick(item: CalendarEvent?) {
        findNavController().navigateUp()
        Toast.makeText(context, "Start Navigation for ${item!!.title}", Toast.LENGTH_LONG).show()
    }

}
