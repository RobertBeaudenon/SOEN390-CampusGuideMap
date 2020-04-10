package com.droidhats.campuscompass.viewmodels

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.repositories.IndoorLocationRepository
import com.droidhats.campuscompass.repositories.IndoorNavigationRepository
import com.droidhats.campuscompass.repositories.MapRepository
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.roomdb.IndoorLocationDatabase
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.asin
import kotlin.math.sqrt
import kotlin.math.pow

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    internal var googleSearchSuggestions = MutableLiveData<MutableList<GooglePlace>>()  //google places search results to be displayed
    internal var indoorSearchSuggestions : LiveData<List<IndoorLocation>>? = null // concordia indoor location search results from SQLite database to be displayed
    internal var searchSuggestions =  MutableLiveData<List<Location>>()  // The combined indoor and google search results to be displayed

    private lateinit var indoorLocationDatabase: IndoorLocationDatabase
    private lateinit var placesClient : PlacesClient // Used to query google places
    private lateinit var indoorLocationRepository : IndoorLocationRepository //Used to query indoorLocations
    internal lateinit var navigationRepository: NavigationRepository  //Used to retrieve route information (ie: transportation times)
    internal lateinit var mapRepository: MapRepository //Used to get Campus Coordinates

    private val context = getApplication<Application>().applicationContext
    var isShuttleValid = false

    fun init(){
        initPlacesSearch()
        indoorLocationDatabase = Room.inMemoryDatabaseBuilder(context, IndoorLocationDatabase::class.java).build()
        indoorLocationRepository = IndoorLocationRepository.getInstance(IndoorLocationDatabase.getInstance(context).indoorLocationDao())
        navigationRepository = NavigationRepository.getInstance(getApplication())
        mapRepository = MapRepository.getInstance(context)
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
        val bounds : RectangularBounds = RectangularBounds.newInstance(LatLng(45.385835,-74.014743), LatLng(45.697779,-73.480629))
        val request : FindAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
            .setLocationRestriction(bounds)
            .setCountries("CA")
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
            val qEsc = query.replace("'", "")
        val queryString =
            "SELECT * " +
              "FROM IndoorLocation " +
                "WHERE location_type ='classroom' " +
                  "AND location_name like '%$qEsc%' " +
                    "OR location_name like '%${qEsc.toUpperCase(Locale.ROOT)}%' " +
                      "LIMIT 5"

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

    fun getRouteTimes(origin: Location, destination: Location) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message!!)
        }
        GlobalScope.launch(Dispatchers.Default + handler) {
           if (origin is GooglePlace && !origin.isCurrentLocation)
               navigationRepository.fetchPlace(origin)
           if (destination is GooglePlace && !destination.isCurrentLocation)
               navigationRepository.fetchPlace(destination)

           val closestShuttle = closestShuttleStop(origin)
          verifyShuttleAvailability(origin, destination, closestShuttle)
               navigationRepository.fetchRouteTimes(origin, destination, closestShuttle)
           if (destination is Building)
               navigationRepository.fetchRouteTimes(origin, destination, closestShuttle)
        }
    }

    fun setIndoorDirections(startAndEnd: Pair<String, String>) {
        IndoorNavigationRepository.getInstance().setStartAndEnd(startAndEnd)
    }

    /**
     * Method to find the closest Concordia shuttle bus stop from a given coordinate
     */
    fun closestShuttleStop(origin: Location) : String{
        val sgw =  mapRepository.getCampuses()[0]
        val loy =  mapRepository.getCampuses()[1]

        val distanceToSGW = haversine(origin, sgw)
        val distanceToLOY = haversine(origin, loy)

        return if (distanceToSGW < distanceToLOY)
            NavigationRepository.SGW_TO_LOY_WAYPOINT
        else
           NavigationRepository.LOY_TO_SGW_WAYPOINT
    }

    /**
     * Checks if the shuttle route is valid. Validity is determined by the location of the destination.
     * If the destination is too far from the exit shuttle stop, the path is invalid.
     * The out-of-range radius is set at 3km.
     */
    private fun verifyShuttleAvailability(origin : Location, destination: Location, waypoints : String){
        val radius = 3
        val sgwCampus = mapRepository.getCampuses()[0]
        val loyCampus = mapRepository.getCampuses()[1]
        val destDistToLOY = haversine(destination,loyCampus)
        val destDistToSGW = haversine(destination, sgwCampus)
        val origDistToLOY = haversine(origin,loyCampus)
        val origDistToSGW = haversine(origin, sgwCampus)

        if (waypoints == NavigationRepository.SGW_TO_LOY_WAYPOINT){
            if (destDistToLOY < radius) isShuttleValid = true
            // Extra check to verify that the starting location is not too far from the closest Shuttle Bus Stop.
            // Technically can be verified in the closestShuttleStop() method
            // but it is included here in this manner to be able to easily disable if ever required
            if (origDistToSGW > radius) isShuttleValid = false
        }
        else{
            if (destDistToSGW < radius) isShuttleValid = true
            //Same check here for LOY
            if (origDistToLOY > radius) isShuttleValid = false
        }
    }

    /**
     * Method used to find the distance between two world coordinates using the haversine formula
     * The distance returned is in km
     */
    private fun haversine(location1 : Location, location2 : Location) : Double{
        return haversine(location1.getLocation(), location2.getLocation())
    }

    fun haversine(latLng1: LatLng, latLng2: LatLng) : Double {
        val diffLat = Math.toRadians(latLng2.latitude - latLng1.latitude)
        val diffLong = Math.toRadians(latLng2.longitude- latLng1.longitude)

        val lat = Math.toRadians(latLng1.latitude)
        val lat2 = Math.toRadians(latLng2.latitude)

        val rad = 6371.0
        val a = sin(diffLat / 2).pow(2.0) +
                sin(diffLong / 2).pow(2.0) *
                cos(lat) * cos(lat2)
        val c = 2 * asin(sqrt(a))
        return rad * c
    }
}
