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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import kotlinx.android.synthetic.main.search_bar_layout.toggleButton
import org.json.JSONObject
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlinx.android.synthetic.main.search_bar_layout.radioTransportGroup
import java.util.Locale
import com.android.volley.Response
import com.droidhats.campuscompass.models.Building
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.search_bar_layout.searchBarMain
import kotlinx.android.synthetic.main.search_bar_layout.searchBarDestination
import org.json.JSONArray

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolygonClickListener, CalendarFragment.OnCalendarEventClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var placesClient : PlacesClient
    private lateinit var mapFragment: View
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val AUTOCOMPLETE_REQUEST_CODE = 3

        private const val MAP_PADDING_TOP = 370
        private const val MAP_PADDING_RIGHT = 15
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: MapViewModel

    private var checker = 0
    private var coordOne = ""
    private var coordTwo = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         mapFragment = inflater.inflate(R.layout.map_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
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
        initSearchBarMain()
        initSearchBarDestination()
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
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
                if (checker == 1) {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    if (place != null) {
                        coordOne = place.latLng?.latitude.toString()
                        coordTwo = place.latLng?.longitude.toString()
                    }
                } else {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    if (place != null) {
                        Log.i(TAG,"Place: " + place.name + ", " + place.id + ", " + place.latLng)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 16.0f))
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
               // var status = data?.let { Autocomplete.getStatusFromIntent(it) }
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
        // TODO: Limt only to campus buildings as polygons could highlight anything
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        //Populate the bottom sheet with building information
        val buildingNameText: TextView = requireActivity().findViewById(R.id.bottom_sheet_building_name)
        buildingNameText.text = p.tag.toString()

        //Navigation here
        val directionsButton: Button = requireActivity().findViewById(R.id.bottom_sheet_directions_button)
        directionsButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            //Get the building that the user clicked on
            var selectedBuilding :Building? = null
            for (campus in viewModel.getCampuses()) {
                for (building in campus.getBuildings()) {
                    if (p.tag.toString() == building.getName())
                        selectedBuilding  = building
                }
            }

            //TODO: This full clear and redraw should probably be removed when the directions
            // system is implemented. It was added to show only one route at a time
            map.clear()
            drawBuildingPolygons()
            if (selectedBuilding != null) {
                placeMarkerOnMap(LatLng(selectedBuilding.getLocation().latitude, selectedBuilding.getLocation().longitude))
            }

            //Generate directions from current location to the selected building
            fusedLocationClient.lastLocation.addOnSuccessListener(activity as Activity) { location ->
                if (location != null) {

                    //Open the search bars and the location texts inside
                    searchBarDestination.openSearch()
                    searchBarMain.openSearch()

                    if (tansportationMode() == "shuttle") {
                        //Setting the top bar "from" to the name of the selected building.
                        if (selectedBuilding != null) {
                            searchBarMain.text = selectedBuilding.getName()
                        }

                        // TODO: In the future check selectedBuilding.getName() == SGW_buildings <-- Grab this part from campus.
                        if (selectedBuilding != null) {
                            if (selectedBuilding.getName() == "Henry F. Hall Building" || selectedBuilding.getName() == "EV Building" || selectedBuilding.getName() == "John Molson School of Business" || selectedBuilding.getName() == "Faubourg Saint-Catherine Building" || selectedBuilding.getName() == "Guy-De Maisonneuve Building" || selectedBuilding.getName() == "Faubourg Building" || selectedBuilding.getName() == "Visual Arts Building" || selectedBuilding.getName() == "Pavillion J.W. McConnell Building") { //<-- TO FIX
                                generateDirections(location, selectedBuilding.getLocation(), "shuttleToSGW")
                                searchBarMain.text = "Shuttle Bus Stop Loyola"
                                searchBarDestination.text = selectedBuilding.getName()
                            } else {
                                generateDirections(location, selectedBuilding.getLocation(), "shuttleToLOY")
                                searchBarMain.text = "Shuttle Bus Stop SGW"
                                searchBarDestination.text = selectedBuilding.getName()
                            }
                        }
                    } else {
                        if (selectedBuilding != null) {
                            searchBarMain.text = "Current Location"
                            generateDirections(location, selectedBuilding.getLocation(), tansportationMode())
                            searchBarDestination.text = selectedBuilding.getName()
                        }
                    }
                }

                if (tansportationMode()!= "shuttle") {
                    //Move the camera to the starting location
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude), 16.0f))
                }

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

   private fun tansportationMode() : String {
        checker = 1
        //Checking which transportation mode is selected, default is walking.
        var transportationMode = "driving"
       when (radioTransportGroup.checkedRadioButtonId) {
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

       /*
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
        }*/
    return transportationMode
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
            if (null != addresses && addresses.isNotEmpty()) {
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
        }
    }

    private fun drawBuildingPolygons() {

        //Highlight both SGW and Loyola Campuses
        for (campus in viewModel.getCampuses()) {
            for (building in campus.getBuildings()) {
                map.addPolygon(building.getPolygonOptions()).tag = building.getName()
            }
        }
    }

    private fun initPlacesSearch() {

        Places.initialize(activity as Activity, getString(R.string.ApiKey), Locale.CANADA)
        placesClient = Places.createClient(activity as Activity)

    }

    fun sendQuery(query : String, searchBar : MaterialSearchBar) : Boolean{

        var doesPredict = false

        //Set up your query here
        val token : AutocompleteSessionToken  = AutocompleteSessionToken.newInstance()
        //Here you would bound your search (to montreal for example)
       val bounds : RectangularBounds = RectangularBounds.newInstance(LatLng(45.509958, -74.152854), LatLng(45.610739, -73.163261))
        val request : FindAutocompletePredictionsRequest  = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        val searchResults  = mutableListOf<String>()
        //Get your query results here
        placesClient.findAutocompletePredictions(request).addOnSuccessListener {

            for ( prediction in it.autocompletePredictions) {
                Log.i(TAG, prediction.placeId)
                Log.i(TAG, prediction.getPrimaryText(null).toString())
                searchResults.add(prediction.getPrimaryText(null).toString())
            }
            searchBar.lastSuggestions = searchResults
            doesPredict = searchResults.size != 0
            searchBar.showSuggestionsList()
            if (!doesPredict)
                searchBar.hideSuggestionsList()

        }.addOnFailureListener {
            if (it is ApiException) {
             val apiException =  it
            Log.e(TAG, "Place not found: " + apiException.statusCode)
            }
        }
        return doesPredict
    }

    private fun initSearchBarMain() {

        searchBarMain.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{

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

        searchBarMain.addTextChangeListener( object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.isNullOrBlank())
                {
                    searchBarMain.hideSuggestionsList()
                    searchBarMain.clearSuggestions()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                 val doesPredict = sendQuery(p0.toString(), searchBarMain)
                if (!doesPredict)
                    searchBarMain.hideSuggestionsList()
            }
        })
    }

    private fun initSearchBarDestination() {
        searchBarDestination.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {
            }
            override fun onSearchStateChanged(enabled: Boolean) {
            }
            override fun onSearchConfirmed(text: CharSequence?) {
            }
        })

        searchBarDestination.addTextChangeListener( object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.isNullOrBlank())
                {
                    searchBarDestination.hideSuggestionsList()
                    searchBarDestination.clearSuggestions()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val doesPredict = sendQuery(p0.toString(), searchBarDestination)
                if (!doesPredict)
                    searchBarDestination.hideSuggestionsList()
            }
        })
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
                map.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, (slideOffset * bottom_sheet.height).toInt())
            }
        })
    }

    private fun generateDirections(origin: Location, destination: LatLng, mode: String) {

        val directionsURL:String = when (mode) {
            "shuttleToSGW" -> {
                "https://maps.googleapis.com/maps/api/directions/json?origin=45.497132,-73.578519&destination=45.458398,-73.638241&waypoints=via:45.492767,-73.582678|via:45.463749,-73.628861&mode=" + mode + "&key=" + getString(R.string.ApiKey)
            }
            "shuttleToLOY" -> {
                "https://maps.googleapis.com/maps/api/directions/json?origin=45.458398,-73.638241&destination=45.497132,-73.578519&mode=" + mode + "&key=" + getString(R.string.ApiKey)
            }
            else -> {
                "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude.toString() + "," + origin.longitude.toString() + "&destination=" + destination.latitude.toString() + "," + destination.longitude.toString() +"&mode=" + mode +"&key=" + getString(R.string.ApiKey)
            }
        }

        if (mode == "shuttleToSGW") {
            //Move the camera to the LOY Campus
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(45.458398,-73.638241), 16.0f))
        }

        //Move the camera to the SGW Campus
        if (mode == "shuttleToLOY") {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(45.497132,-73.578519), 16.0f))
        }

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

                if (mode == "shuttleToSGW" || mode == "shuttleToLOY") {
                    for (x in 1..3) {
                        Toast.makeText(activity, "The selected Transportation mode is: Shuttle. Total distance is: ${totalKm.getString("text")}. Total travel time is: ${travelTime.getString("text")}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    for (x in 1..3) {
                        Toast.makeText(activity, "The selected Transportation mode is: $mode. Total distance is: ${totalKm.getString("text")}. Total travel time is: ${travelTime.getString("text")}", Toast.LENGTH_LONG).show()
                    }
                }


                val path: MutableList<List<LatLng>> = ArrayList()

                //Build the path polyline
                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                   // val instructions = steps.getJSONObject(i).getString("html_instructions")  //Getting the route instructions
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
