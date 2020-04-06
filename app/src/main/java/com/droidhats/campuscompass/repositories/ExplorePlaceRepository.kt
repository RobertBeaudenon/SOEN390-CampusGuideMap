package com.droidhats.campuscompass.repositories

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.ExplorePlace
import com.droidhats.campuscompass.models.Explore_Place
import com.droidhats.campuscompass.roomdb.ExplorePlaceDAO
import com.droidhats.campuscompass.roomdb.ExplorePlaceDB
import com.droidhats.campuscompass.roomdb.ExplorePlaceEntity
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * This class will create a connection with the SQLite DB in order to get the
 * Places
 * @param application
 */
class ExplorePlaceRepository (private val application: Application)  {

    private var explorePlaceDAO: ExplorePlaceDAO
    private var allPlaces: LiveData<List<ExplorePlaceEntity>>
    private var list: ArrayList<Explore_Place> = ArrayList()
    internal  var placesList = MutableLiveData<ArrayList<Explore_Place>>()

    companion object {
        // Singleton instantiation
        private var instance: ExplorePlaceRepository? = null

        fun getInstance(application: Application) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: ExplorePlaceRepository(application).also { instance = it }
                }
    }

    init {
        val db = ExplorePlaceDB.getInstance(application)
        explorePlaceDAO = db.ExplorePlaceDAO()
        allPlaces = explorePlaceDAO.getAllPlaces()
    }

    /**
     * @return allPlaces
     */
    fun getAllPlaces(): LiveData<List<ExplorePlaceEntity>> {
        return allPlaces
    }

    /**
     * Returns the explore place image from drawable resources
     * @param placeName: Used to map the place name to the place image.
     */
    fun getPlaceImageResourceID(placeName: String): Int {

        // The id for the place image resource is of Int type
        // Return the place image resource id that corresponds to the place name
        return when (placeName) {
            "Restaurant Maison Prathet Thai" -> R.drawable.resto_restaurantmaisonprathetthai
            "Les Saisons de Corée" -> R.drawable.resto_lessaisonsdecoree
            "Bar-B-Barn" -> R.drawable.resto_bar_b_barn
            "Kazu" -> R.drawable.resto_kazu
            "Bacaro Pizzeria" -> R.drawable.resto_bacaropizzeria
            "Garage Beirut" -> R.drawable.resto_garagebeirut
            "Da Vinci Ristorante" -> R.drawable.resto_davinciristorante
            else -> Log.v("ImageError", "couldn't load image")
        }
    }

    fun getPlaces(campus:String, type: String){
        val placesRequest = object : StringRequest(
            Method.GET,
            constructRequestURL(campus, type),
            Response.Listener { response ->

                // placesList = MutableLiveData<ArrayList<Explore_Place>>()

                //Retrieve response (a JSON object)
                val jsonResponse = JSONObject(response)

                //Get places info from JSON response
                val results = jsonResponse.getJSONArray("results")
                if(results.length() > 0){
                    for(i in 0 until results.length()-1){
                        var item_detail: JSONObject = results.getJSONObject(i)
                        var geometry: JSONObject = item_detail.getJSONObject("geometry")
                        var location: JSONObject = geometry.getJSONObject("location")
                        
                        var explorePlace = Explore_Place(
                            item_detail.getString("name"),
                            item_detail.getString("vicinity"),
                            item_detail.getString("rating"),
                            item_detail.getString("place_id"),
                            item_detail.getString("icon"),
                            LatLng(location.getString("lat").toDouble(),location.getString("lng").toDouble())
                        )
                        list.add(explorePlace)
                    }
                    placesList.value = list
                }
            },
                Response.ErrorListener {
                    Log.e("Volley Error:", "HTTP response error")
                })
        {}
        //Confirm and add the request with Volley
        val requestQueue = Volley.newRequestQueue(application)
        requestQueue.add(placesRequest)
    }

    private fun constructRequestURL(
        campus: String,
        type: String
    ): String {

        if (campus.equals("Loyola")) {
            println("Robert Inside")
            return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "&location=" + "45.458488,-73.639862" +
                    "&radius=" + "600" +
                    "&type=" + type +
                    "&key=" + application.applicationContext.getString(R.string.ApiKey)
        } else {
            return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "&location=" + "45.497406,-73.577102" +
                    "&radius=" + "600" +
                    "&type=" + type +
                    "&key=" + application.applicationContext.getString(R.string.ApiKey)
        }

    }

}