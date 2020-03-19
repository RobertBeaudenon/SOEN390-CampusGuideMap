package com.droidhats.campuscompass.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.models.NavigationRoute
import com.droidhats.campuscompass.roomdb.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import org.json.JSONArray
import org.json.JSONObject

/**
 * This class will create a connection with the SQLite DB in order to get the
 * SGW and Loyola shuttle times
 * @param application
 */
class NavigationRepository(private val application: Application) {

    private  var shuttleBusDAO: ShuttleBus_DAO
    private  var loyolaShuttleTimes: LiveData<List<ShuttleBus_Loyola_Entity>>
    private  var sgwShuttleTimes: LiveData<List<ShuttleBus_SGW_Entity>>
    var routeTimes = MutableLiveData<MutableMap<String, String>>()

    init {
        val db = ShuttleBusDB.getInstance(application)
        shuttleBusDAO = db.shuttleBusDAO()
        loyolaShuttleTimes = shuttleBusDAO.getLoyolaShuttleTime()
        sgwShuttleTimes = shuttleBusDAO.getSGWShuttleTime()
    }

    /**
    * @return loyolaShuttleTimes
    */
    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBus_Loyola_Entity>> {
        return loyolaShuttleTimes
    }

    /**
     * @return sgwShuttleTimes
     */
    fun getSGWShuttleTime(): LiveData<List<ShuttleBus_SGW_Entity>> {
        return sgwShuttleTimes
    }
    /**
     * route times for all transportation methods
     */
    private fun fetchRouteTimes(origin: Location, destination: Location) {
        val times = mutableMapOf<String, String>()
        for (method in NavigationRoute.TransportationMethods.values()){
         val directionsURL = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin="+ origin.coordinate.latitude.toString() + ","+ origin.coordinate.longitude.toString() +
                    "&destination="+ destination.coordinate.latitude.toString() + "," + destination.coordinate.longitude.toString()+
                    "&mode=" + method.string +
                    "&key=" + application.applicationContext.getString(R.string.ApiKey)

            val directionRequest = StringRequest(
                Request.Method.GET, directionsURL,
                Response.Listener { response ->

                    //Retrieve response (a JSON object)
                    val jsonResponse = JSONObject(response)

                    // Get route information from json response
                    val routesArray: JSONArray = jsonResponse.getJSONArray("routes")
                    if (routesArray.length() > 0) {
                        val routes: JSONObject = routesArray.getJSONObject(0)
                        val legsArray: JSONArray = routes.getJSONArray("legs")
                        val legs: JSONObject = legsArray.getJSONObject(0)
                        times[method.string] = legs.getJSONObject("duration").getString("text")
                    }
                    else{
                        times[method.string] = "N/A"
                    }
                    routeTimes.value = times
                },
                Response.ErrorListener { Log.e("Volley Error:", "HTTP response error") })

            //Confirm and add the request with Volley
            val requestQueue = Volley.newRequestQueue(application)
            requestQueue.add(directionRequest)
        }
    }

    fun getRouteTimes(origin: Location, destination: Location) : MutableLiveData<MutableMap<String, String>> {
        if (origin is GooglePlace && destination is GooglePlace) {
            val placeFields: List<Place.Field> =
                arrayListOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val placesClient = Places.createClient(application.applicationContext)

            val originRequest = FetchPlaceRequest.newInstance(origin.placeID, placeFields)
            val destinationRequest = FetchPlaceRequest.newInstance(destination.placeID, placeFields)
            placesClient.fetchPlace(originRequest)
                .addOnSuccessListener { originResponse: FetchPlaceResponse ->
                    origin.place = originResponse.place
                    origin.coordinate = originResponse.place.latLng!!

                    placesClient.fetchPlace(destinationRequest)
                        .addOnSuccessListener { destResponse: FetchPlaceResponse ->
                            destination.place = destResponse.place
                            destination.coordinate = destResponse.place.latLng!!

                           fetchRouteTimes(origin, destination)

                        }
                        .addOnFailureListener{
                          if (destination.isCurrentLocation)
                            fetchRouteTimes(origin, destination)
                        }
                }.addOnFailureListener{
                    if (origin.isCurrentLocation)
                        fetchRouteTimes(origin,destination)
                }
        }
        return routeTimes
    }

}