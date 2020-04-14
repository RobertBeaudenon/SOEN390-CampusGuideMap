package com.droidhats.campuscompass.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Explore_Place
import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
import org.json.JSONObject

/**
 * This class will create a connection with the SQLite DB in order to get the
 * Places
 * @param application
 */
class ExplorePlaceRepository (private val application: Application)  {

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

    fun getPlaces(campus:String, type: String){
        var list: ArrayList<Explore_Place> = ArrayList()
        val placesRequest = object : StringRequest(
            Method.GET,
            constructRequestURL(campus, type),
            Response.Listener { response ->
                //Retrieve response (a JSON object)
                val jsonResponse = JSONObject(response)

                //Get places info from JSON response
                val results = jsonResponse.getJSONArray("results")
                if(results.length() > 0){
                    for(i in 0 until results.length()-1){
                        try {
                            var item_detail: JSONObject = results.getJSONObject(i)
                            var geometry: JSONObject = item_detail.getJSONObject("geometry")
                            var location: JSONObject = geometry.getJSONObject("location")
                            var photos = item_detail.getJSONArray("photos")
                            var photo: JSONObject = photos.getJSONObject(0)
                            var imageURL: String =
                                constructImageURL(photo.getString("photo_reference"))
                            var explorePlace = Explore_Place(
                                item_detail.getString("name"),
                                item_detail.getString("vicinity"),
                                item_detail.getString("rating"),
                                item_detail.getString("place_id"),
                                imageURL,
                                LatLng(
                                    location.getString("lat").toDouble(),
                                    location.getString("lng").toDouble()
                                )
                            )
                            list.add(explorePlace)
                        }catch (e: JSONException){
                            Log.e("JSONException", e.message.toString())
                        }
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

    fun constructRequestURL(
        campus: String,
        type: String
    ): String {

        if (campus.equals("Loyola")) {
            return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "&location=" + "45.458488,-73.639862" +
                    "&radius=" + "2000" +
                    "&type=" + type +
                    "&key=" + application.applicationContext.getString(R.string.ApiKey)
        } else {
            return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "&location=" + "45.497406,-73.577102" +
                    "&radius=" + "1000" +
                    "&type=" + type +
                    "&key=" + application.applicationContext.getString(R.string.ApiKey)
        }
    }

     fun constructImageURL( reference: String): String{
        return "https://maps.googleapis.com/maps/api/place/photo?" +
                "&photoreference=" + reference +
                "&sensor=" + "false" +
                "&maxheight=" + "3024" +
                "&maxwidth=" + "3024" +
                "&key=" + application.applicationContext.getString(R.string.ApiKey)
    }
}