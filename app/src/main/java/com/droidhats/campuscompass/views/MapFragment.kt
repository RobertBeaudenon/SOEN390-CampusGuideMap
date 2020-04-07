package com.droidhats.campuscompass.views

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.ExplorePlaceAdapter
import com.droidhats.campuscompass.adapters.SearchAdapter
import com.droidhats.campuscompass.helpers.Subject
import com.droidhats.campuscompass.models.*
import com.droidhats.campuscompass.roomdb.ExplorePlaceEntity
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import com.droidhats.campuscompass.helpers.Observer as ModifiedObserver
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import kotlinx.android.synthetic.main.instructions_sheet_layout.*
import kotlinx.android.synthetic.main.search_bar_layout.*
import kotlinx.coroutines.Dispatchers
import com.droidhats.campuscompass.models.Map as MapModel

/**
 * A View Fragment for the map.
 * It displays all the UI components of the map and dynamically interacts with the user input.
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolygonClickListener, CalendarFragment.OnCalendarEventClickListener, SearchAdapter.OnSearchResultClickListener,ExploreCategoryFragment.OnExplorePlaceClickListener, OnCameraIdleListener,
    Subject {

    private var mapModel: MapModel? = null
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val observerList = mutableListOf<ModifiedObserver?>()
    private val currentNavigationPath = arrayListOf<Polyline>()
    private var locationUpdateState = false

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val MAP_PADDING_TOP = 200
        private const val MAP_PADDING_RIGHT = 15
        private var tracker = 0
        private var currentNavigationRoute : NavigationRoute? = null
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var viewModel: MapViewModel

    private lateinit var root : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.map_fragment, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)

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

        //Event click callbacks
        CalendarFragment.onCalendarEventClickListener = this
        SearchFragment.onSearchResultClickListener = this
        ExploreCategoryFragment.onExplorePlaceClickListener = this

        createLocationRequest()
        initBottomSheetBehavior()
        initSearchBar()
        handleCampusSwitch()
        observeNavigation()
        setNavigationButtons()
    }

    /**
     * Initializes the map and adds markers or lines and attaches listeners
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * @param googleMap a necessary google map object on which we add markers and attach listeners.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // Get the map from the viewModel.
        mapModel = viewModel.getMapModel(googleMap, this, this, this, this.activity as MainActivity)
        map = mapModel?.googleMap

        //Add custom style to map
        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.map_style));
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (e: android.content.res.Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }

        // Move camera to SGW
        // TODO when navigation path is being shown, the camera should be moved to current location
        moveTo(viewModel.getCampuses()[0].getLocation(), 16f)

        map!!.setOnMapClickListener {
            //Dismiss the bottom sheet when clicking anywhere on the map
            dismissBottomSheet()
        }

        attachBuildingObservers()
        if (currentNavigationRoute != null) drawPathPolyline(currentNavigationRoute!!.polyLinePath)
    }

    private fun attachBuildingObservers(){
        // Attach all observer buildings with initial markers
        for (building in viewModel.getBuildings()) {
            if (!observerList.contains(building) && building.hasCenterLocation()) {
                attach(building)
            }
        }
    }

    private fun detachBuildingObservers(){
        // Detach all observer buildings with initial markers
        for (building in viewModel.getBuildings()) {
            if (observerList.contains(building) && building.hasCenterLocation()) {
                detach(building)
            }
        }
    }

    private fun observeNavigation() {
        viewModel.getNavigationRoute().observe(viewLifecycleOwner, Observer {
            // The observer's OnChange is called when the Fragment gets pushed back even when the object didn't change
            // Remove the condition check to keep the path drawn on the screen even after changing activities
            // If the condition is removed though, camera movement must be handled properly not to override other movement
            if ( it != null && it != currentNavigationRoute) {
                currentNavigationRoute = it
                drawPathPolyline(it.polyLinePath)
                showInstructions(it.instructions)
                Handler().postDelayed({
                    moveTo(it.origin!!.getLocation(), 19.0f)
                }, 100)
            }
        })
        tracker = 0
    }

    private fun setNavigationButtons() {
        val buttonCloseInstructions : ImageButton = requireActivity().findViewById(R.id.buttonCloseInstructions)
        buttonCloseInstructions.setOnClickListener{
            toggleInstructionsView(false)
        }
        val buttonResumeNavigation : Button = requireActivity().findViewById(R.id.buttonResumeNavigation)
        buttonResumeNavigation.setOnClickListener{
            dismissBottomSheet()
            toggleInstructionsView(true)
        }

        if(currentNavigationRoute != null) {
            showInstructions(currentNavigationRoute!!.instructions)
            toggleInstructionsView(false)
        }
    }

    private fun createLocationRequest() {
        // 1 create an instance of LocationRequest, add it to an instance of LocationSettingsRequest.Builder and retrieve and handle any changes to be made based on the current state of the user’s location settings.
        locationRequest = LocationRequest()
        // 2 specifies the rate at which your app will like to receive updates.
        locationRequest.interval = 10000
        // 3 specifies the fastest rate at which the app can handle updates. Setting the fastestInterval rate places a limit on how fast updates will be sent to your app.
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // 4 check location settings before asking for location updates
        val client = LocationServices.getSettingsClient(requireActivity())
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
        //requests for location updates.
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null //* Looper *//*
        )
    }

    // 1 Override AppCompatActivity’s onActivityResult() method and start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2 Override onPause() to stop location update request
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        dismissBottomSheet()
    }

    // 3 Override onResume() to restart the location update request.
    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        detachBuildingObservers()
    }

    //implements methods of interface GoogleMap.GoogleMap.OnPolygonClickListener
    override fun onPolygonClick(p: Polygon) {
        val selectedBuilding : Building = viewModel.findBuildingByPolygonTag(p.tag.toString())
            ?: return

        handleBuildingClick(selectedBuilding)
    }

    //implements methods of interface   GoogleMap.OnMarkerClickListener
    override fun onMarkerClick(marker: Marker?): Boolean {
        val selectedBuilding: Building? = viewModel.findBuildingByMarkerTitle(marker)
        //There is a building associated with the marker that was clicked
        if (selectedBuilding != null) {
            handleBuildingClick(selectedBuilding)
            return true //disable ability to tap the marker to avoid default behavior
        }
        return false
    }

    private fun handleBuildingClick(building: Building) {
        expandBottomSheet()
        updateAdditionalInfoBottomSheet(building.getPolygon())

        val directionsButton: Button = requireActivity().findViewById(R.id.bottom_sheet_directions_button)
        directionsButton.setOnClickListener {
            dismissBottomSheet()
            val bundle = Bundle()
            bundle.putParcelable("destBuilding", building)
            findNavController().navigate(R.id.search_fragment, bundle)
        }
    }

    //Handle the switching views between the two campuses. Should probably move from here later
    private fun handleCampusSwitch() {
        var campusView: LatLng

        //Setting Toggle button listener
        toggleButton.setOnCheckedChangeListener { _, onSwitch ->
            if (onSwitch) {
                campusView = LatLng(45.458220, -73.639702)
                moveTo(campusView, 16f)
            } else {
                campusView = LatLng(45.495784, -73.577197)
                moveTo(campusView, 16f)
            }
            dismissBottomSheet()
        }
    }

    private fun showInstructions(instructions : ArrayList<String>) {
        toggleInstructionsView(true)
        arrayInstruction.text = Html.fromHtml(instructions[0]).toString()
        prevArrow.visibility = View.INVISIBLE

        nextArrow.setOnClickListener {
            tracker++
            prevArrow.visibility = View.VISIBLE
            if(tracker < instructions.size) {
                arrayInstruction.text = Html.fromHtml(instructions[tracker]).toString()
            }
            if (tracker == instructions.size-1) {
                nextArrow.visibility = View.INVISIBLE
            }
        }
        prevArrow.setOnClickListener {
            tracker--
            nextArrow.visibility = View.VISIBLE
            if(tracker < instructions.size) {
                arrayInstruction.text = Html.fromHtml(instructions[tracker]).toString()
            }
            if (tracker == 0) {
                prevArrow.visibility = View.INVISIBLE
            }
        }
    }

    private fun toggleInstructionsView(isVisible: Boolean){
        val instructionsView : CardView = requireActivity().findViewById(R.id.instructionLayout)
        if(isVisible){
            instructionsView.visibility = View.VISIBLE
            buttonResumeNavigation.visibility = View.INVISIBLE
            map?.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, instructionsView.height+20)
        }
        else{
            instructionsView.visibility = View.INVISIBLE
            map?.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, 0)
            if(currentNavigationRoute != null)
                buttonResumeNavigation.visibility = View.VISIBLE
        }
    }

    private fun drawPathPolyline(path : MutableList<List<LatLng>>) {
      clearNavigationPath()  //Clear existing path to show only one path at a time
        for (i in 0 until path.size) {
         val polyline= map!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            currentNavigationPath.add(polyline)
        }
    }

    private fun clearNavigationPath() {
        for ( polyline in currentNavigationPath){
            polyline.remove()
        }
        currentNavigationPath.clear()
    }

    private fun initSearchBar() {
        mapFragSearchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{

            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode) {
                    //Open the Nav Bar
                    MaterialSearchBar.BUTTON_NAVIGATION -> requireActivity().
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                }
            }
            override fun onSearchStateChanged(enabled: Boolean) {
                  if (enabled) {
                      dismissBottomSheet()
                      findNavController().navigate(R.id.search_fragment)
                      mapFragSearchBar.closeSearch()
                  }
            }
            override fun onSearchConfirmed(text: CharSequence?) {
            }
        })
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change
                // The following code can be used if we want to do certain actions related
                // to the change of state of the bottom sheet
           }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Adjusting the google zoom buttons to stay on top of the bottom sheet
                //Multiply the bottom sheet height by the offset to get the effect of them being anchored to the top of the sheet
                map!!.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, (slideOffset * root.findViewById<NestedScrollView>(R.id.bottom_sheet).height).toInt())
            }
        })

        val indoorMapsButton: Button = requireActivity().findViewById(R.id.bottom_sheet_floor_map_button)
        indoorMapsButton.setOnClickListener {

            findNavController().navigate(R.id.floor_fragment)
        }
    }

    private fun dismissBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun expandBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            togglePlaceCard(false)
            toggleInstructionsView(false)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun updateAdditionalInfoBottomSheet(p: Polygon) {
        // Update the bottom sheet with building information
        val buildingName: TextView = requireActivity().findViewById(R.id.bottom_sheet_building_name)
        val buildingAddress: TextView =
            requireActivity().findViewById(R.id.bottom_sheet_building_address)
        val buildingOpenHours: TextView = requireActivity().findViewById(R.id.bottom_sheet_open_hours)
        val buildingServices: TextView = requireActivity().findViewById(R.id.bottom_sheet_services)
        val buildingDepartments: TextView =
            requireActivity().findViewById(R.id.bottom_sheet_departments)
        val buildingImage: ImageView = requireActivity().findViewById(R.id.building_image)

        for (building in viewModel.getBuildings()) {
            if (building.getPolygon().tag == p.tag) {
                buildingName.text = p.tag.toString()
                buildingAddress.text = building.getAddress()
                buildingOpenHours.text = building.getOpenHours()
                buildingServices.text = building.getServices()
                buildingDepartments.text = building.getDepartments()
                buildingImage.setImageResource(building.getImageResId())
                //TODO: Leaving events empty for now as the data is not loaded from json. Need to figure out in future how to implement
            }
        }
    }

    override fun onCalendarEventClick(item: CalendarEvent?) {}

    override fun onSearchResultClickListener(item: com.droidhats.campuscompass.models.Location?) {
        var isCampusBuilding = false
        if (item is GooglePlace) {
 			findNavController().popBackStack(R.id.map_fragment, false)
            GlobalScope.launch(Dispatchers.Main) {
                for (building in viewModel.getBuildings())
                    if (building.getPlaceId() == item.placeID) { //Check if location is a concordia building
                        isCampusBuilding = true
                        handleBuildingClick(building)
                    }
              focusLocation(item, isCampusBuilding, true)
            }        
        } else if (item is IndoorLocation) {
            val bundle: Bundle = Bundle()
            bundle.putString("id", (item as IndoorLocation).lID)
            findNavController().navigate(R.id.floor_fragment, bundle)
        }
    }

    private fun focusLocation(location: GooglePlace, isCampusBuilding : Boolean, isRequired : Boolean){
       GlobalScope.launch {
           if(isRequired) {
               viewModel.navigationRepository.fetchPlace(location)
           }
       }.invokeOnCompletion {
           requireActivity().runOnUiThread{
               moveTo(location.coordinate, 17.0f)
               if (!isCampusBuilding)
               populatePlaceInfoCard(location)
           }
       }
    }

    private fun moveTo(coordinates: LatLng, zoomLevel: Float){
        map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel))
    }

    private fun populatePlaceInfoCard(location: GooglePlace){
        val favoritesButton : Button = requireActivity().findViewById(R.id.place_card_favorites_button)
        val placeName: TextView = requireActivity().findViewById(R.id.place_card_name)
        val placeCategory: TextView = requireActivity().findViewById(R.id.place_card_category)
        val closeButton : ImageView = requireActivity().findViewById(R.id.place_card_close_button)

        placeName.text = location.name
        placeCategory.text = location.category

        placeName.setOnClickListener {
            moveTo(location.coordinate, 17.0f)
        }

        closeButton.setOnClickListener{
            togglePlaceCard(false)
        }

        val directionsButton : Button = requireActivity().findViewById(R.id.place_card_directions_button)
        directionsButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("destPlace", location.place)
            findNavController().navigate(R.id.search_fragment, bundle)
        }
        togglePlaceCard(true)
    }

    private fun togglePlaceCard(isVisible : Boolean){
        val placeCard : CardView = requireActivity().findViewById(R.id.place_card)
        if(isVisible){
            placeCard.visibility = View.VISIBLE
            map?.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, placeCard.height+75)
        }
        else{
            placeCard.visibility = View.INVISIBLE
            map?.setPadding(0, MAP_PADDING_TOP, MAP_PADDING_RIGHT, 0)
        }
    }

    /**
     * Notify observers whenever the camera of the Google map is idle
     */
    override fun onCameraIdle() {
        notifyObservers()
    }

    override fun attach(observer: ModifiedObserver?) {
        if (observer != null) {
            observerList.add(observer)
        }
    }

    override fun detach(observer: ModifiedObserver?) {
        if (observer != null) {
            observerList.remove(observer)
        }
    }

    override fun notifyObservers() {
        for (observer in observerList) {
            observer?.update(map!!.cameraPosition.zoom)
        }
    }

    override fun onExplorePlaceClick(item: Explore_Place?) {
        findNavController().popBackStack(R.id.map_fragment, false)

        val exploreLocation = GooglePlace(
            item?.place_placeID!!,
            item.place_name!!,
           item?.place_address!!,
            item!!.place_coordinate
        )
        Handler().postDelayed({
            focusLocation(exploreLocation, false, true)
        }, 1000)
    }
}



