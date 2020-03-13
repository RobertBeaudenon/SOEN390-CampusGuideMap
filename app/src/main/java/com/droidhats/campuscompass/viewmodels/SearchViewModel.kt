package com.droidhats.campuscompass.viewmodels

import android.app.Application
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Locale

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    internal var searchSuggestions = MutableLiveData<ArrayList<String>>()
    private lateinit var placesClient : PlacesClient

    fun init(){

        initPlacesSearch()
    }

    private fun initPlacesSearch() {
        Places.initialize(context, context.resources.getString(R.string.ApiKey), Locale.CANADA)
        placesClient = Places.createClient(context)
    }


    fun sendQuery(query : String) : Boolean{

        var success : Boolean = false
        //Set up your query here
        val token : AutocompleteSessionToken = AutocompleteSessionToken.newInstance()
        //Here you would bound your search (to montreal for example)
        val bounds : RectangularBounds = RectangularBounds.newInstance(LatLng(45.509958, -74.152854), LatLng(45.610739, -73.163261))
        val request : FindAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        val queryResults  = arrayListOf<String>()
        //Get your query results here
        placesClient.findAutocompletePredictions(request).addOnSuccessListener {

            for ( prediction in it.autocompletePredictions) {
                Log.i(ContentValues.TAG, prediction.placeId)
                Log.i(ContentValues.TAG, prediction.getPrimaryText(null).toString())
                queryResults.add(prediction.getPrimaryText(null).toString())
            }

                if (queryResults.size > 0) success = true

                searchSuggestions.value = queryResults

        }.addOnFailureListener {
            if (it is ApiException) {
                val apiException =  it
                Log.e(ContentValues.TAG, "Place not found: " + apiException.statusCode)
            }
        }
        return success
    }
}
