package com.droidhats.campuscompass.views

import android.app.Activity
import android.content.ContentValues
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Selection
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageButton
import android.widget.TextView
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RadioButton
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.SearchAdapter
import com.droidhats.campuscompass.models.*
import com.droidhats.campuscompass.viewmodels.SearchViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchFragment : Fragment()  {

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var root: View
    private var columnCount = 1
    private lateinit var selectedTransportationMethod : String

    companion object{
        var onSearchResultClickListener: SearchAdapter.OnSearchResultClickListener? = null
        var isNavigationViewOpen = false
        // The Navigation Start and End points. Each search bar must contain a valid location to initiate navigation
        var NavigationPoints = mutableMapOf<Int, Location?>(R.id.mainSearchBar to null,
                                                            R.id.secondarySearchBar to null)
        fun areRouteParametersSet() : Boolean {
            return (NavigationPoints[R.id.mainSearchBar] != null && NavigationPoints[R.id.secondarySearchBar] != null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.search_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_suggestions_recycler_view)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.init()
        initSearch()
        observeSearchSuggestions()
        initTransportationRadioGroup()
        observeRouteTimes()

        val backButton  = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
        backButton.setOnClickListener{
            isNavigationViewOpen = false
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit()
        }

        retrieveArguments()
    }

    private fun retrieveArguments(){
        val destinationPlace = arguments?.getParcelable<Place>("destPlace")
        if (destinationPlace != null) {
            val googlePlace = GooglePlace(destinationPlace.id!!,
                destinationPlace.name!!,
                destinationPlace.address!!,
                destinationPlace.latLng!!)
            showNavigationView(googlePlace, true)
            arguments?.clear()
        }

        val destinationBuilding = arguments?.getParcelable<Building>("destBuilding")
        if (destinationBuilding != null) {
            showNavigationView(destinationBuilding, true)
            arguments?.clear()
        }

        val destinationEventLocation = arguments?.getString("destEventLocation")
        if (destinationEventLocation != null) {
        val calendarLocation = GooglePlace("",destinationEventLocation, "", LatLng(0.0,0.0) )

            showNavigationView(calendarLocation, true)
            arguments?.clear()
        }
    }

    private fun observeSearchSuggestions() {
        viewModel.googleSearchSuggestions.observe(viewLifecycleOwner , Observer { googlePredictions ->
            viewModel.searchSuggestions.value = googlePredictions
          if (viewModel.indoorSearchSuggestions !=null)
            viewModel.indoorSearchSuggestions?.observe(viewLifecycleOwner , Observer {indoorResults ->
                //Prepending indoor results to the google places results
                viewModel.searchSuggestions.value = indoorResults + googlePredictions
            })
        })
        //On change to the above results, the recycler view will be updated here
        viewModel.searchSuggestions.observe(viewLifecycleOwner, Observer {
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()
        })
    }

    private fun observeRouteTimes() {
        viewModel.navigationRepository.routeTimes.observe(viewLifecycleOwner, Observer {
            showRouteTimes(it)
        })
    }

    private fun initSearch() {
        val mainSearchBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
        mainSearchBar.isIconified = false
        mainSearchBar.isActivated = true

        val secondarySearchBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)
        secondarySearchBar.isActivated = false

        initQueryTextListener(mainSearchBar)
        initQueryTextListener(secondarySearchBar)

        val startNavButton  = root.findViewById<ImageButton>(R.id.startNavigationButton)
        startNavButton.setOnClickListener{
            if (!areRouteParametersSet()) {
                Toast.makeText(context, "Set Your Route To Begin Navigation", Toast.LENGTH_LONG).show()
            }
            else {

               initiateNavigation()
            }
        }
        initCurrentLocationHandler(mainSearchBar, secondarySearchBar)
    }

    private fun initiateNavigation(){
        val origin = NavigationPoints[R.id.mainSearchBar]
        val destination = NavigationPoints[R.id.secondarySearchBar]
        findNavController().navigateUp() // Navigate Back To MapFragment
        Toast.makeText(context, "Starting Navigation\n" +
                "From: ${origin?.name}\n" +
                "To: ${destination?.name}\n" +
                "By: $selectedTransportationMethod",
            Toast.LENGTH_LONG).show()

        //Make sure BOTH coordinates are set before generating directions
        if(origin?.coordinate == LatLng(0.0, 0.0) || destination?.coordinate == LatLng(0.0, 0.0)){
            val handler = CoroutineExceptionHandler{_, throwable ->
                Log.e(ContentValues.TAG, throwable.message!!)
            }
            GlobalScope.launch(Dispatchers.Default + handler) {
                viewModel.navigationRepository.fetchPlace(origin!!)
                viewModel.navigationRepository.fetchPlace(destination!!)

                viewModel.navigationRepository.generateDirections(origin,
                    destination,
                    selectedTransportationMethod)
            }
        }else {
            viewModel.navigationRepository.generateDirections(origin!!,
                destination!!,
                selectedTransportationMethod)
        }
    }

    private fun initCurrentLocationHandler(mainSearchView: SearchView, secondarySearchView : SearchView){
        val myLocationFAB = root.findViewById<FloatingActionButton>(R.id.myCurrentLocationFAB)
        myLocationFAB.setOnClickListener{
            if (mainSearchView.isActivated){
               setCurrentLocation(mainSearchView)
            }
            if (secondarySearchView.isActivated) {
                setCurrentLocation(secondarySearchView)
            }
        }
    }

    private fun setCurrentLocation(searchView: SearchView) {
       val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        fusedLocationClient.lastLocation.addOnSuccessListener{
            if (it != null) {
                val coordinates = LatLng(it.latitude, it.longitude)
                val currentLocation = GooglePlace(
                    it.toString(),
                    "Your Current Location",
                    coordinates.toString(),
                    coordinates
                )
                currentLocation.isCurrentLocation = true
                searchView.setQuery(currentLocation.name, false)
                val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
                searchText.setTextColor(Color.GREEN)
                NavigationPoints[searchView.id] = currentLocation
                Toast.makeText(context, "Current Location Set\n $coordinates", Toast.LENGTH_LONG).show()

                if (areRouteParametersSet()) {
                    viewModel.getRouteTimes(
                        NavigationPoints[R.id.mainSearchBar]!!,
                        NavigationPoints[R.id.secondarySearchBar]!!
                    )
                    toggleNavigationButtonColor(Color.GREEN)
                }
            }
        }
    }

    private fun initQueryTextListener(searchView: SearchView) {
        val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
        searchText.textSize = 14f
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
               resetQuery(searchText, searchView)
               resetRouteTimes()
                return if (!p0.isNullOrBlank()) {
                    viewModel.sendSearchQueries(p0)
                } else {
                    viewModel.searchSuggestions.value = emptyList()
                    false
                }
                return false
            }
        })
       searchView.setOnQueryTextFocusChangeListener { _, isFocused ->
            searchView.isActivated = isFocused
          if(searchView.isActivated)
           viewModel.sendSearchQueries(searchView.query.toString())
        }
    }

    private fun updateRecyclerView() {
        val fragment= this
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            searchAdapter = SearchAdapter(viewModel.searchSuggestions.value!!, onSearchResultClickListener, fragment, root)
            adapter = searchAdapter
        }
    }

    private fun resetQuery(queryText : EditText, searchView: SearchView){
        NavigationPoints[searchView.id] = null
        toggleNavigationButtonColor(Color.WHITE)
        if (isNavigationViewOpen)
            queryText.setTextColor(Color.WHITE)
        else
            queryText.setTextColor(Color.BLACK)
    }

    private fun resetRouteTimes(){
        if (isNavigationViewOpen) {
            val defaultTextView = mutableMapOf<String, String>()
            for (i in NavigationRoute.TransportationMethods.values())
                defaultTextView[i.string] = "-"
            viewModel.navigationRepository.routeTimes.value = defaultTextView
        }
    }

    private fun initTransportationRadioGroup(){
        selectedTransportationMethod = NavigationRoute.TransportationMethods.DRIVING.string
        val radioTransportationGroup = root.findViewById<RadioGroup>(R.id.radioTransportGroup)
        radioTransportationGroup.setOnCheckedChangeListener{ radioGroup: RadioGroup?, id: Int ->

            when (radioGroup?.checkedRadioButtonId) {
                R.id.radio_transport_mode_driving -> {
                    selectedTransportationMethod = NavigationRoute.TransportationMethods.DRIVING.string
                }
                R.id.radio_transport_mode_transit -> {
                    selectedTransportationMethod = NavigationRoute.TransportationMethods.TRANSIT.string
                }
                R.id.radio_transport_mode_walking -> {
                    selectedTransportationMethod = NavigationRoute.TransportationMethods.WALKING.string
                }
                R.id.radio_transport_mode_bicycle -> {
                    selectedTransportationMethod = NavigationRoute.TransportationMethods.BICYCLE.string
                }
                 R.id.radio_transport_mode_shuttle -> {
                     selectedTransportationMethod = NavigationRoute.TransportationMethods.SHUTTLE.string
                 }
            }
        }
    }

    private fun showRouteTimes(routeTimes : MutableMap<String, String>){
        val drivingRadioButton =  root.findViewById<RadioButton>(R.id.radio_transport_mode_driving)
        val transitRadioButton =  root.findViewById<RadioButton>(R.id.radio_transport_mode_transit)
        val walkingRadioButton =  root.findViewById<RadioButton>(R.id.radio_transport_mode_walking)
        val bicycleRadioButton =  root.findViewById<RadioButton>(R.id.radio_transport_mode_bicycle)
        val shuttleRadioButton =  root.findViewById<RadioButton>(R.id.radio_transport_mode_shuttle)

        drivingRadioButton.text =  routeTimes[NavigationRoute.TransportationMethods.DRIVING.string]
        transitRadioButton.text =  routeTimes[NavigationRoute.TransportationMethods.TRANSIT.string]
        walkingRadioButton.text =  routeTimes[NavigationRoute.TransportationMethods.WALKING.string]
        bicycleRadioButton.text =  routeTimes[NavigationRoute.TransportationMethods.BICYCLE.string]
        shuttleRadioButton.text =  routeTimes[NavigationRoute.TransportationMethods.SHUTTLE.string]
    }

    override fun onDetach() {
        super.onDetach()
        reset()
    }

    private fun reset(){
        isNavigationViewOpen = false
        NavigationPoints = mutableMapOf(R.id.mainSearchBar to null, R.id.secondarySearchBar to null)
    }

    fun showNavigationView(destinationPlace : Location, startFromCurrentLocation : Boolean ){
            isNavigationViewOpen = true
             val startNavButton = root.findViewById<ImageButton>(R.id.startNavigationButton)
             val backButton = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
             val myLocationFAB = root.findViewById<FloatingActionButton>(R.id.myCurrentLocationFAB)
             val mainBar = root.findViewById<SearchView>(R.id.mainSearchBar)
             val destinationBar = root.findViewById<SearchView>(R.id.secondarySearchBar)
             val destSearchText = destinationBar.findViewById(R.id.search_src_text) as EditText
             val radioTransportationGroup = root.findViewById<RadioGroup>(R.id.radioTransportGroup)
             val infoMessage = root.findViewById<TextView>(R.id.search_info)
             val searchPlate = mainBar.findViewById<View>(R.id.search_plate)
             searchPlate.setBackgroundResource(R.color.colorPrimaryDark)

             mainBar.maxWidth = root.resources.getDimension(R.dimen.search_bar_max_width).toInt()
             destinationBar.visibility = View.VISIBLE
             startNavButton.visibility = View.VISIBLE
             backButton.visibility = View.VISIBLE
             radioTransportationGroup.visibility = View.VISIBLE
             infoMessage.visibility = View.INVISIBLE
             myLocationFAB.show()
             mainBar.queryHint = "From"

             if (NavigationPoints[mainBar.id] == null)
                 mainBar.setQuery("", true)

             if (startFromCurrentLocation)
                 setCurrentLocation(mainBar)

        destinationBar.setQuery(destinationPlace.name, true)
        destSearchText.setSelection(destinationPlace.name.length) //Sets the cursor position
        destinationBar.isIconified = false

        if(!(destinationPlace is GooglePlace && destinationPlace.placeID.isEmpty())){
            confirmSelection(destinationBar, destinationPlace, false)
        }
    }

    internal fun confirmSelection(searchView: SearchView, location: Location, submit: Boolean) {
        val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
        val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)

        searchView.setQuery(location.name, submit)
        val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
        searchText.setTextColor(Color.GREEN)
        NavigationPoints[searchView.id] = location

        if (areRouteParametersSet()) {
            viewModel.getRouteTimes(
                NavigationPoints[mainBar.id]!!,
                NavigationPoints[destinationBar.id]!!
            )
            toggleNavigationButtonColor(Color.GREEN)
        }
    }

    private fun toggleNavigationButtonColor(color : Int){
        val startNavButton = root.findViewById<ImageButton>(R.id.startNavigationButton)
        startNavButton.setColorFilter(color)
    }
}
