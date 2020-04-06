package com.droidhats.campuscompass.repositories

import android.app.Application
import android.content.ContentValues.TAG
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
import com.droidhats.campuscompass.roomdb.ShuttleBusSGWEntity
import com.droidhats.campuscompass.roomdb.ShuttleBusLoyolaEntity
import com.droidhats.campuscompass.roomdb.ShuttleBusDAO
import com.droidhats.campuscompass.roomdb.ShuttleBusDB
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This class will create a connection with the SQLite DB in order to get the
 * SGW and Loyola shuttle times
 * @param application
 */
class NavigationRepository(private val application: Application) {

    private var shuttleBusDAO: ShuttleBusDAO
    private var loyolaShuttleTimes: LiveData<List<ShuttleBusLoyolaEntity>>
    private var sgwShuttleTimes: LiveData<List<ShuttleBusSGWEntity>>
    private var navigationRoute = MutableLiveData<NavigationRoute>()
    var routeTimes = MutableLiveData<MutableMap<String, String>>()


    companion object {
        // Singleton instantiation
        private var instance: NavigationRepository? = null

        fun getInstance(application: Application) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: NavigationRepository(application).also { instance = it }
                }
    }

    init {
        val db = ShuttleBusDB.getInstance(application)
        shuttleBusDAO = db.shuttleBusDAO()
        loyolaShuttleTimes = shuttleBusDAO.getLoyolaShuttleTime()
        sgwShuttleTimes = shuttleBusDAO.getSGWShuttleTime()
    }

    /**
     * @return loyolaShuttleTimes
     */
    fun getLoyolaShuttleTime(): LiveData<List<ShuttleBusLoyolaEntity>> {
        return loyolaShuttleTimes
    }

    /**
     * @return sgwShuttleTimes
     */
    fun getSGWShuttleTime(): LiveData<List<ShuttleBusSGWEntity>> {
        return sgwShuttleTimes
    }

    suspend fun fetchPlace(location: Location): Unit = suspendCoroutine { cont ->
        if (location is GooglePlace) {
            val placeFields: List<Place.Field> = Place.Field.values().toList()
            val placesClient = Places.createClient(application.applicationContext)
            val request = FetchPlaceRequest.newInstance(location.placeID, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener {
                    location.place = it.place
                    location.coordinate = it.place.latLng!!
                }.addOnFailureListener {
                    if (it is ApiException)
                        Log.e(TAG, "Place not found: " + it.message)
                }.addOnCompleteListener {
                    cont.resume(Unit)
                }
        }
    }

    /**
     * Fetches the time it takes to reach the destination for all transportation methods
     * @param origin: The starting point from where the travel begins.
     * @param destination: The destination point where the travel ends.
     * @param waypoints: Waypoints along the travel path
     */
    fun fetchRouteTimes(origin: Location, destination: Location, waypoints: String?) {
        val times = mutableMapOf<String, String>()
        for (method in NavigationRoute.TransportationMethods.values()) {
            var shuttleWaypoints = waypoints
            if (method.string != NavigationRoute.TransportationMethods.SHUTTLE.string)
                shuttleWaypoints = ""
            val directionRequest = StringRequest(
                Request.Method.GET,
                constructRequestURL(origin, destination, method.string, shuttleWaypoints),
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
                    } else {
                        times[method.string] = "N/A"
                    }
                    //Set only after all the times have been retrieved (to display them all at the same time)
                    if (times.size == NavigationRoute.TransportationMethods.values().size)
                        routeTimes.value = times
                },
                Response.ErrorListener {
                    Log.e("Volley Error:", "HTTP response error")
                })

            //Confirm and add the request with Volley
            val requestQueue = Volley.newRequestQueue(application)
            requestQueue.add(directionRequest)
        }
    }

    /**
     * Fetches navigation route information
     * @param origin: The starting point from where the travel begins.
     * @param destination: The destination point where the travel ends.
     * @param mode: The transportation method
     * @param waypoints: Waypoints along the travel path
     */
    fun fetchDirections(origin: Location, destination: Location, mode: String, waypoints: String?) {
        if (origin.getLocation() == LatLng(0.0, 0.0) || destination.getLocation() == LatLng(0.0, 0.0))
            return
        var transportationMethod = mode
        //If the mode is shuttle, we want to show walking instructions towards the bus stop
        if (mode == NavigationRoute.TransportationMethods.SHUTTLE.string)
            transportationMethod = NavigationRoute.TransportationMethods.WALKING.string

        val directionsRequest = object : StringRequest(
            Method.GET,
            constructRequestURL(origin, destination, transportationMethod, waypoints),
            Response.Listener { response ->
                //Retrieve response (a JSON object)
                val jsonResponse = JSONObject(response)
              navigationRoute.value =
                  parseDirections(jsonResponse, origin, destination, transportationMethod)
            },
            Response.ErrorListener {
                Log.e("Volley Error:", "HTTP response error")
            }) {}

        //Confirm and add the request with Volley
        val requestQueue = Volley.newRequestQueue(application)
        requestQueue.add(directionsRequest)
    }

    private fun parseDirections(jsonResponse: JSONObject, origin: Location, destination: Location, mode: String) : NavigationRoute {
        val path: MutableList<List<LatLng>> = ArrayList()
        val instructions = arrayListOf<String>()
        val intCoordinates = arrayListOf<LatLng>()

        // Get route information from json response
        val routesArray = jsonResponse.getJSONArray("routes")
        if (routesArray.length() > 0) {
            val routes = routesArray.getJSONObject(0)
            val legsArray: JSONArray = routes.getJSONArray("legs")
            val legs = legsArray.getJSONObject(0)
            val stepsArray = legs.getJSONArray("steps")

            for (i in 0 until stepsArray.length()) { //Each iteration is an direction step
                try {
                    val points =
                        stepsArray.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                    instructions.add(parseInstructions(stepsArray.getJSONObject(i), mode))
                    intCoordinates.add(parseCoordinates(stepsArray.getJSONObject(i), true))
                    if (stepsArray.getJSONObject(i).has("steps")) {
                        for (j in 0 until stepsArray.getJSONObject(i).getJSONArray("steps").length()) {
                            instructions.add(
                                parseInstructions(stepsArray.getJSONObject(i).getJSONArray("steps").getJSONObject(j), mode))
                            intCoordinates.add(parseCoordinates(
                                stepsArray.getJSONObject(i).getJSONArray("steps").getJSONObject(j), true))
                        }
                    }
                } catch (e: org.json.JSONException) {
                    Log.e("JSONException", e.message.toString())
                }
            }
            intCoordinates.add(
                parseCoordinates(stepsArray.getJSONObject(stepsArray.length() - 1), false))
            instructions.add("You have arrived!")
        }
        return NavigationRoute(origin, destination, mode, path, instructions, intCoordinates)
    }

    private fun parseInstructions(jsonObject: JSONObject, transportationMethod: String): String {
        var instruction = jsonObject.getString("html_instructions")

        if (transportationMethod == NavigationRoute.TransportationMethods.TRANSIT.string ||
            transportationMethod == NavigationRoute.TransportationMethods.WALKING.string) {
            instruction += "<br>Distance: " +
                 jsonObject.getJSONObject("distance")
                    .getString("text") + "<br>Duration: " +

                 jsonObject.getJSONObject("duration")
                     .getString("text")

            if (jsonObject.has("transit_details")) {
                instruction += "<br>Arrival Stop: " +

                 jsonObject.getJSONObject("transit_details").getJSONObject("arrival_stop")
                      .getString("name") + "<br>Total Number of Stop: " +

                 jsonObject.getJSONObject("transit_details")
                     .getString("num_stops")
            }
        }
        return instruction
    }

    private fun parseCoordinates(jsonObject: JSONObject, isStartLocation : Boolean) : LatLng{
        val name = if(isStartLocation) "start_location" else "end_location"

        val lat =
            jsonObject.getJSONObject(name)
                .getString("lat").toDouble()
        val lng =
            jsonObject.getJSONObject(name)
                .getString("lng").toDouble()

        return LatLng(lat,lng)
    }

    private fun constructRequestURL(
        origin: Location,
        destination: Location,
        transportationMethod: String,
        waypoints: String?
    ): String {

        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.getLocation().latitude.toString() + "," + origin.getLocation().longitude.toString() +
                "&destination=" + destination.getLocation().latitude.toString() + "," + destination.getLocation().longitude.toString() +
                waypoints +
                "&mode=" + transportationMethod +
                "&key=" + application.applicationContext.getString(R.string.ApiKey)
    }

    fun getNavigationRoute(): MutableLiveData<NavigationRoute> = navigationRoute
}