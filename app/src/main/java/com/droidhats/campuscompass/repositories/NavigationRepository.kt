package com.droidhats.campuscompass.repositories

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.NavHandler.NavHandler
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.models.NavigationRoute
import com.droidhats.campuscompass.models.OutdoorNavigationRoute
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
    var navhandler: NavHandler? = null


    companion object {
        // Singleton instantiation
        private var instance: NavigationRepository? = null

        fun getInstance(application: Application) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: NavigationRepository(application).also { instance = it }
                }

        // may return null
        fun getInstance() = instance
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

    fun setNavigationHandler(navHandler: NavHandler) {
        navHandler.getNavigationRoute()
        this.navhandler = navHandler
    }

    fun consumeNavigationHandler(): NavHandler? {
        if (navhandler != null && navhandler?.next != null) {
            setNavigationHandler(navhandler!!.next!!)
        }
        return navhandler
    }

    fun cancelNavigation() {
        navhandler = null
        navigationRoute.value = null
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
     */
    fun fetchRouteTimes(origin: Location, destination: Location) {
        val times = mutableMapOf<String, String>()
        for (method in OutdoorNavigationRoute.TransportationMethods.values()) {
            val directionRequest = StringRequest(
                Request.Method.GET, constructRequestURL(origin, destination, method.string),
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
                    if (times.size == OutdoorNavigationRoute.TransportationMethods.values().size)
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

    fun generateDirections(origin: Location, destination: Location, mode: String) {
        val instructions = arrayListOf<String>()
        if (origin.getLocation() == LatLng(0.0, 0.0) || destination.getLocation() == LatLng(0.0, 0.0))
            return
        val directionsRequest = object : StringRequest(
            Method.GET,
            constructRequestURL(origin, destination, mode),
            Response.Listener { response ->

                //Retrieve response (a JSON object)
                val jsonResponse = JSONObject(response)

                // Get route information from json response
                val routesArray = jsonResponse.getJSONArray("routes")
                if (routesArray.length() > 0) {
                    val routes = routesArray.getJSONObject(0)
                    val legsArray: JSONArray = routes.getJSONArray("legs")
                    val legs = legsArray.getJSONObject(0)
                    val stepsArray = legs.getJSONArray("steps")

                    val path: MutableList<List<LatLng>> = ArrayList()

                    //Build the path polyline as well as store instruction between 2 path into an array.
                    for (i in 0 until stepsArray.length()) {
                        val points = stepsArray.getJSONObject(i).getJSONObject("polyline")
                            .getString("points")

                        try {
                            if (mode == "transit" || mode == "walking") {
                                if (stepsArray.getJSONObject(i).has("transit_details")) {
                                    instructions.add(
                                        stepsArray.getJSONObject(i)
                                            .getString("html_instructions") + "<br><Distance: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("distance")
                                            .getString("text") + "<br>Duration: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("duration")
                                            .getString("text") + "<br>Arrival Stop: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("transit_details")
                                            .getJSONObject("arrival_stop")
                                            .getString("name") + "<br>Total Number of Stop: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("transit_details").getString("num_stops")
                                    )
                                } else {
                                    instructions.add(
                                        stepsArray.getJSONObject(i)
                                            .getString("html_instructions") + "<br><br>Distance: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("distance")
                                            .getString("text") + "<br><br>Duration: " + stepsArray.getJSONObject(
                                            i
                                        ).getJSONObject("duration").getString("text")
                                    )
                                }
                                if (stepsArray.getJSONObject(i).has("steps")) {
                                    for (j in 0 until stepsArray.getJSONObject(i)
                                        .getJSONArray("steps").length()) {
                                        instructions.add(
                                            stepsArray.getJSONObject(i).getJSONArray("steps")
                                                .getJSONObject(j).getString("html_instructions")
                                        )
                                    }
                                }
                            } else {
                                instructions.add(
                                    stepsArray.getJSONObject(i).getString("html_instructions")
                                )
                            }
                            path.add(PolyUtil.decode(points))
                        } catch (e: org.json.JSONException) {
                            Log.e("JSONException", e.message.toString())
                        }
                    }
                    val navigation = OutdoorNavigationRoute(origin, destination, mode, path, instructions)
                    navigationRoute.value = navigation
                }
            },
            Response.ErrorListener {
                Log.e("Volley Error:", "HTTP response error")
            }) {}

        //Confirm and add the request with Volley
        val requestQueue = Volley.newRequestQueue(application)
        requestQueue.add(directionsRequest)
    }


    private fun constructRequestURL(
        origin: Location,
        destination: Location,
        transportationMethod: String
    ): String {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.getLocation().latitude.toString() + "," + origin.getLocation().longitude.toString() +
                "&destination=" + destination.getLocation().latitude.toString() + "," + destination.getLocation().longitude.toString() +
                "&mode=" + transportationMethod +
                "&key=" + application.applicationContext.getString(R.string.ApiKey)
    }

    fun getNavigationRoute(): MutableLiveData<NavigationRoute> = navigationRoute
}