package com.droidhats.campuscompass.views

import android.app.Activity
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.SearchAdapter
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.viewmodels.SearchViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SearchFragment : Fragment()  {

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var root: View
    private var columnCount = 1

    companion object{
        var onSearchResultClickListener: SearchAdapter.OnSearchResultClickListener? = null
        var isNavigationViewOpen = false
        // The Navigation Start and End points. Each search bar must contain a valid location to initiate navigation
        var NavigationPoints = mutableMapOf<Int, Location?>(R.id.mainSearchBar to null,
                                                            R.id.secondarySearchBar to null)

        fun expandNavigationView(root : View){
            isNavigationViewOpen = true
            val startNavButton  = root.findViewById<ImageButton>(R.id.startNavigationButton)
            val backButton  = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
            val myLocationFAB = root.findViewById<FloatingActionButton>(R.id.myCurrentLocationFAB)
            val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
            val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)

            mainBar.maxWidth = root.resources.getDimension(R.dimen.search_bar_max_width).toInt()
            destinationBar.visibility = View.VISIBLE
            startNavButton.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
            myLocationFAB.show()
            mainBar.queryHint = "From"

            if (NavigationPoints[mainBar.id] == null)
                mainBar.setQuery("", true)
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

        val backButton  = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
        backButton.setOnClickListener{
            isNavigationViewOpen = false
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit()
        }
    }

    private fun observeSearchSuggestions() {
        viewModel.googleSearchSuggestions.observe(viewLifecycleOwner , Observer { googlePredictions ->
            viewModel.indoorSearchSuggestions.observe(viewLifecycleOwner , Observer {indoorResults ->
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
            else { //Initiate Navigation
                Toast.makeText(context, "Start Navigation\n" +
                "From: ${NavigationPoints[mainSearchBar.id]?.name}\n" +
                "To: ${NavigationPoints[secondarySearchBar.id]?.name}",
                 Toast.LENGTH_LONG).show()
            }
        }
        initCurrentLocationHandler(mainSearchBar, secondarySearchBar)
    }

    private fun areRouteParametersSet() : Boolean {
        return (NavigationPoints[R.id.mainSearchBar] != null && NavigationPoints[R.id.secondarySearchBar] != null)
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
                    "Your Freakin Location",
                    coordinates.toString(),
                    coordinates
                )
                searchView.setQuery(currentLocation.name, false)
                val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
                searchText.setTextColor(Color.GREEN)
                NavigationPoints[searchView.id] = currentLocation
                Toast.makeText(context, "Current Location Set\n $coordinates", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initQueryTextListener(searchView: SearchView) {
        val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                searchText.setTextColor(Color.WHITE)
                NavigationPoints[searchView.id] = null
                if (!p0.isNullOrBlank()) {
                   return viewModel.sendSearchQueries(p0)
                }
                else {
                    viewModel.searchSuggestions.value = emptyList()
                    return false
                }
            }
        })
       searchView.setOnQueryTextFocusChangeListener { _, isFocused ->
            searchView.isActivated = isFocused
          if(searchView.isActivated)
           viewModel.sendSearchQueries(searchView.query.toString())
        }
    }

    private fun updateRecyclerView() {
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = SearchAdapter(viewModel.searchSuggestions.value!!, onSearchResultClickListener, root)
        }
    }

    override fun onDetach() {
        super.onDetach()
        reset()
    }

    private fun reset(){
        isNavigationViewOpen = false
        NavigationPoints = mutableMapOf(R.id.mainSearchBar to null, R.id.secondarySearchBar to null)
    }
}
