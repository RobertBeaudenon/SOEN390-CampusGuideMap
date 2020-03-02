package com.droidhats.campuscompass.views

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.CalendarEvent
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
//import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import kotlinx.android.synthetic.main.map_fragment.searchBar
import kotlinx.android.synthetic.main.map_fragment.toggleButton
import org.json.JSONObject
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.listOf
import kotlinx.android.synthetic.main.bottom_sheet_layout.radioTransportGroup
import java.util.Locale
import com.android.volley.Response
import org.json.JSONArray

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolygonClickListener, CalendarFragment.OnCalendarEventClickListener {

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

        private const val MAP_PADDING_TOP = 150
        private const val MAP_PADDING_RIGHT = 15
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mapFragment = inflater.inflate(R.layout.map_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.init()
        return mapFragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        initPlacesSearch()
        initBottomSheetBehavior()
       // initSearchBar()
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

        //updating map type we can choose between  4 types : MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        //initializing vars for get last current location
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        //enable the zoom controls on the map and declare MainActivity as the callback triggered when the user clicks a marker on this map
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        //enable indoor level picker
        map.isIndoorEnabled = true
        map.uiSettings.isIndoorLevelPickerEnabled = true

        //Enables the my-location layer which draws a light blue dot on the user’s location.
        // It also adds a button to the map that, when tapped, centers the map on the user’s location.
        map.isMyLocationEnabled = true
        //Lower the button
        map.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, 0)

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
        setBuildingPolygons()

        map.setOnMapClickListener {

            //Dismiss the bottom sheet when clicking anywhere on the map
            dismissBottomSheet()
        }
    }

    private fun createLocationRequest() {
        // 1  create an instance of LocationRequest, add it to an instance of
        // LocationSettingsRequest.Builder and retrieve and handle any changes to be made based on
        // the current state of the user’s location settings.
        locationRequest = LocationRequest()
        // 2   specifies the rate at which your app will like to receive updates.
        locationRequest.interval = 10000
        // 3 specifies the fastest rate at which the app can handle updates. Setting the
        // fastestInterval rate places a limit on how fast updates will be sent to your app.
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4 check location settings before asking for location updates
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        // 5 A task success means all is well and you can go ahead and initiate a location request
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }

        task.addOnFailureListener { e ->
            // 6  A task failure means the location settings have some issues which can be fixed.
            // This could be as a result of the user’s location settings turned off
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        activity,
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


    // 1 Override AppCompatActivity’s onActivityResult() method and start the update request if it
    // has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                if (place != null) {
                    Log.i(TAG,"Place: " + place.name + ", " + place.id + ", " + place.latLng)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 16.0f))
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                var status = data?.let { Autocomplete.getStatusFromIntent(it) }
            } else if (resultCode == RESULT_CANCELED) {
                // TODO: Handle user cancelling the operation.
            }
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

    private fun setBuildingPolygons() {
        drawBuildingPolygons()
        map.setOnPolygonClickListener(this)
    }

    // implements methods of interface GoogleMap.GoogleMap.OnPolygonClickListener
    override fun onPolygonClick(p: Polygon) {
        // Expand the bottom sheet when clicking on a polygon
        // TODO: Limit only to campus buildings as polygons could highlight anything
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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

        // Populate the bottom sheet with building information
        populateAdditionalInfoBottomSheet(p)

        val directionsButton: Button = requireActivity().findViewById(R.id.bottom_sheet_directions_button)
        directionsButton.setOnClickListener(View.OnClickListener {

            // Calculating the center of the polygon to use for it's location.
            // This was here before building class in the model was made, so I think it's a good
            // idea to keep it here because there is no use in introducing a dependency to this
            // method and it's not slower than filtering through all the buildings for a match to
            // this polygon.
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

            //TODO: This full clear and redraw should probably be removed when the directions
            // system is implemented. It was added to show only one route at a time
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

    //the Android Maps API lets you use a marker object, which is an icon that can be placed at a
    // particular point on the map’s surface.
    private fun placeMarkerOnMap(location: LatLng) {
        // 1 Create a MarkerOptions object and sets the user’s current location as the
        // position for the marker
        val markerOptions = MarkerOptions().position(location)

        //added a call to getAddress() and added this address as the marker title.
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        // 2 Add the marker to the map
        map.addMarker(markerOptions)
    }

    //This method get address from coordinates
    private fun getAddress(latLng: LatLng): String {
        // 1 Creates a Geocoder object to turn a latitude and longitude coordinate into an address
        // and vice versa
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
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
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
        val searchButton: View = requireActivity().findViewById(R.id.fab_search)

        searchButton.setOnClickListener {
            var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(activity as Activity)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    //Handle the switching views between the two campuses. Should probably move from here later
    private fun handleCampusSwitch() {
        var campusView: LatLng

        //Setting Toggle button listener
        toggleButton.setOnCheckedChangeListener { _, onSwitch ->
            if (onSwitch) {
                campusView = LatLng(45.495637, -73.578235)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(campusView, 17.5f))
            } else {
                campusView = LatLng(45.458159, -73.640450)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(campusView, 17.5f))
            }
            dismissBottomSheet()
        }
}

    private fun drawBuildingPolygons() {
        //Highlight both SGW and Loyola Campuses
        for (campus in viewModel.getCampuses()) {
            for (building in campus.getBuildings()) {
                var polygon: Polygon = map.addPolygon(building.getPolygonOptions())
                building.setPolygon(polygon)
            }
        }
    }
/*
    private fun initSearchBar() {
        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{

            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode) {
                    //Open the Nav Bar
                    MaterialSearchBar.BUTTON_NAVIGATION -> requireActivity().
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                }
            }
            override fun onSearchStateChanged(enabled: Boolean) {
            }
            override fun onSearchConfirmed(text: CharSequence?) {
            }
        })
    }
*/
    private fun initBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change.
                // No functionality yet (but needs to override abstract class)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Adjusting the google zoom buttons to stay on top of the bottom sheet
                //Multiply the bottom sheet height by the offset to get the effect of them being anchored to the top of the sheet
                map.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, (slideOffset * bottom_sheet.height).toInt())
            }
        })
    }

    private fun dismissBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun populateAdditionalInfoBottomSheet(p: Polygon) {
        // Populate the bottom sheet with building information
        val buildingName: TextView = requireActivity().findViewById(R.id.bottom_sheet_building_name)
        val buildingAddress: TextView =
            requireActivity().findViewById(R.id.bottom_sheet_building_address)
        val buildingOpenHours: TextView = requireActivity().findViewById(R.id.bottom_sheet_open_hours)
        val buildingServices: TextView = requireActivity().findViewById(R.id.bottom_sheet_services)
        val buildingDepartments: TextView =
            requireActivity().findViewById(R.id.bottom_sheet_departments)
        val buildingImage: ImageView = requireActivity().findViewById(R.id.building_image)

        for (campus in viewModel.getCampuses()) {
            for (building in campus.getBuildings()) {
                if (building.getPolygon().tag == p.tag) {
                    buildingName.text = p.tag.toString()
                    buildingAddress.text = building.getAddress()
                    buildingOpenHours.text = building.getOpenHours()
                    buildingServices.text = building.getServices()
                    buildingDepartments.text = building.getDepartments()

                    when(building.getPolygon().tag){
                        "Henry F. Hall Building" -> buildingImage.setImageResource(R.drawable.building_hall)
                        "EV Building" -> buildingImage.setImageResource(R.drawable.building_ev)
                        "John Molson School of Business" -> buildingImage.setImageResource(R.drawable.building_jmsb)
                        "Faubourg Saint-Catherine Building" -> buildingImage.setImageResource(R.drawable.building_fg_sc)
                        "Guy-De Maisonneuve Building" -> buildingImage.setImageResource(R.drawable.building_gm)
                        "Faubourg Building" -> buildingImage.setImageResource(R.drawable.building_fg)
                        "Visual Arts Building" -> buildingImage.setImageResource(R.drawable.building_va)
                        "Pavillion J.W. McConnell Building" -> buildingImage.setImageResource(R.drawable.building_webster_library)
                        "Psychology Building" -> buildingImage.setImageResource(R.drawable.building_p)
                        "Richard J. Renaud Science Complex" -> buildingImage.setImageResource(R.drawable.building_rjrsc)
                        "Central Building" -> buildingImage.setImageResource(R.drawable.building_cb)
                        "Communication Studies and Journalism Building" -> buildingImage.setImageResource(R.drawable.building_csj)
                        "Administration Building" -> buildingImage.setImageResource(R.drawable.building_a)
                        "Loyola Jesuit and Conference Centre" -> buildingImage.setImageResource(R.drawable.building_ljacc)
                        else -> Log.v("Error loading images", "couldn't load image")
                    }
                    //TODO: Leaving events empty for now as the data is not loaded from json. Need to figure out in future how to implement
                }
            }
        }
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
        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(directionsRequest)
    }

    override fun onCalendarEventClick(item: CalendarEvent?) {
        findNavController().navigateUp()
        Toast.makeText(context, "Start Navigation for ${item!!.title}", Toast.LENGTH_LONG).show()
    }

}
