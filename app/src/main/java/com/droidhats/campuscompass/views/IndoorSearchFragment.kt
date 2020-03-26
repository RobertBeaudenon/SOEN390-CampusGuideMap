package com.droidhats.campuscompass.views

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.IndoorSearchAdapter
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.viewmodels.SearchViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IndoorSearchFragment : Fragment()  {

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var indoorSearchAdapter: IndoorSearchAdapter
    private lateinit var root: View
    private var columnCount = 1

    companion object{
        var onSearchResultClickListener: IndoorSearchAdapter.OnSearchResultClickListener? = null
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
        root = inflater.inflate(R.layout.indoor_search_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_suggestions_recycler_view)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.init()
        initSearch()
        observeSearchSuggestions()

        val backButton = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
        backButton.setOnClickListener{
            isNavigationViewOpen = false
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit()
        }
    }

    private fun observeSearchSuggestions() {
        viewModel.googleSearchSuggestions.observe(viewLifecycleOwner , Observer { googlePredictions ->     //FIND A WAY TO OBSERVE ONLY INDOOR RESULTS
                viewModel.getIndoorSearchQueries()?.observe(viewLifecycleOwner , Observer {indoorResults ->
                    //Prepending indoor results
                    viewModel.searchSuggestions.value = indoorResults
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
            else {
                //initiateNavigation()
                findNavController().navigate(R.id.floor_fragment)
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
        val fragment= this
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            indoorSearchAdapter = IndoorSearchAdapter(viewModel.searchSuggestions.value!!, onSearchResultClickListener, fragment, root)
            adapter = indoorSearchAdapter
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

    override fun onDetach() {
        super.onDetach()
        reset()
    }

    private fun reset(){
        isNavigationViewOpen = false
        NavigationPoints = mutableMapOf(R.id.mainSearchBar to null, R.id.secondarySearchBar to null)
    }

    fun showNavigationView(destinationPlace : Location, startFromCurrentLocation : Boolean){
        isNavigationViewOpen = true
        val startNavButton = root.findViewById<ImageButton>(R.id.startNavigationButton)
        val backButton = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
        val myLocationFAB = root.findViewById<FloatingActionButton>(R.id.myCurrentLocationFAB)
        val mainBar = root.findViewById<SearchView>(R.id.mainSearchBar)
        val destinationBar = root.findViewById<SearchView>(R.id.secondarySearchBar)
        val infoMessage = root.findViewById<TextView>(R.id.search_info)
        val searchPlate = mainBar.findViewById<View>(R.id.search_plate)
        searchPlate.setBackgroundResource(R.color.colorPrimaryDark);

        mainBar.maxWidth = root.resources.getDimension(R.dimen.search_bar_max_width).toInt()
        destinationBar.visibility = View.VISIBLE
        startNavButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
        infoMessage.visibility = View.INVISIBLE
        myLocationFAB.show()
        mainBar.queryHint = "From"

        if (NavigationPoints[mainBar.id] == null)
            mainBar.setQuery("", true)

        destinationBar.setQuery(destinationPlace.name, false)
        confirmSelection(destinationBar, destinationPlace, false)
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