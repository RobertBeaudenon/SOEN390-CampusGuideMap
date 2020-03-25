package com.droidhats.campuscompass.viewmodels

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.droidhats.campuscompass.IndoorLocationDatabase
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.repositories.IndoorLocationRepository
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    internal var googleSearchSuggestions = MutableLiveData<MutableList<GooglePlace>>()  //google places search results to be displayed
    internal var indoorSearchSuggestions : LiveData<List<IndoorLocation>>? = null // concordia indoor location search results from SQLite database to be displayed
    internal var searchSuggestions =  MutableLiveData<List<Location>>()  // The combined indoor and google search results to be displayed

    private lateinit var indoorLocationDatabase: IndoorLocationDatabase
    private lateinit var placesClient : PlacesClient // Used to query google places
    private lateinit var indoorLocationRepository : IndoorLocationRepository //Used to query indoorLocations
    internal lateinit var navigationRepository: NavigationRepository  //Used to retrieve route information (ie: transportation times)

    private val context = getApplication<Application>().applicationContext

    fun init(){
        initPlacesSearch()
        indoorLocationDatabase = Room.inMemoryDatabaseBuilder(context, IndoorLocationDatabase::class.java).build()
        indoorLocationRepository = IndoorLocationRepository.getInstance(IndoorLocationDatabase.getInstance(context).indoorLocationDao())
        navigationRepository = NavigationRepository.getInstance(getApplication())
    }

    private fun initPlacesSearch() {
        Places.initialize(context, context.resources.getString(R.string.ApiKey), Locale.CANADA)
        placesClient = Places.createClient(context)
    }

    private fun sendGooglePlacesQuery(query : String) : Boolean{
        var success = false
        //Set up your query here
        val token : AutocompleteSessionToken = AutocompleteSessionToken.newInstance()
        //Here you would bound your search (to montreal for example)
        val bounds : RectangularBounds = RectangularBounds.newInstance(LatLng(45.477803, -73.580243), LatLng(45.511822, -73.578240))
        val request : FindAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setSessionToken(token)
            .setQuery(query)
            .build()
        //Get your query results here
        val queryResults  = arrayListOf<GooglePlace>()
        placesClient.findAutocompletePredictions(request).addOnSuccessListener {

            for ( prediction in it.autocompletePredictions) {
                queryResults.add(GooglePlace(prediction.placeId,
                    prediction.getPrimaryText(null).toString(),
                    prediction.getSecondaryText(null).toString(),
                    LatLng(0.0, 0.0)))
            }
            if (queryResults.size > 0)
                success = true

            googleSearchSuggestions.value = queryResults

        }.addOnFailureListener {
            if (it is ApiException) {
                val apiException =  it
                Log.e(TAG, "Place not found: " + apiException.statusCode)
            }
        }
        return success
    }

    private fun sendSQLiteQuery(query: String): Boolean {
     if (query.isBlank()) return false
     val qEsc = query.replace("'","")
        val queryString =
            "SELECT * " +
            "FROM IndoorLocation " +
             "WHERE location_type ='classroom' " +
              "AND location_name like '%$qEsc%' " +
               "OR location_name like '%${qEsc.toUpperCase(Locale.ROOT)}%' " +
                    "LIMIT 3"

        val sqliteQuery = SimpleSQLiteQuery(queryString)
        indoorSearchSuggestions = indoorLocationRepository.getMatchedClassrooms(sqliteQuery)

        return indoorSearchSuggestions != null
    }

    // Send indoor and outdoor queries ASYNCHRONOUSLY
    fun sendSearchQueries(query: String): Boolean {
        val success = sendGooglePlacesQuery(query)
        sendSQLiteQuery(query)
        return success
    }

    fun getRouteTimes(origin : Location, destination : Location)  {
        val handler = CoroutineExceptionHandler{_, throwable ->
            Log.e(TAG, throwable.message!!)
        }
       GlobalScope.launch(Dispatchers.Default + handler) {
           if (origin is GooglePlace && !origin.isCurrentLocation)
               navigationRepository.fetchPlace(origin)
           if (destination is GooglePlace && !destination.isCurrentLocation)
               navigationRepository.fetchPlace(destination)

           navigationRepository.fetchRouteTimes(origin, destination)

           if (destination is Building)
               navigationRepository.fetchRouteTimes(origin, destination)
       }
    }
}
