package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.models.Map
import com.droidhats.campuscompass.repositories.MapRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import java.io.InputStream

class  MapViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var campuses: List<Campus>? = null

    fun getCampuses(): List<Campus> = campuses!!

    // The activity is required to access the assets to open our json file where the info
    // is stored
    init {
        val inputStream: InputStream = context.assets.open("buildings.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        campuses = MapRepository.getInstance(json).getCampuses()
    }

    fun getMap(googleMap: GoogleMap,
               mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
               mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
               activity: FragmentActivity
    ): GoogleMap
    {
        var initializedGoogleMap: GoogleMap = Map(googleMap, mapFragmentOnMarkerClickListener, mapFragmentOnPolygonClickListener, activity).getMap()


        //Highlight both SGW and Loyola Campuses
        for (campus in this.campuses!!) {
            for (building in campus.getBuildings()) {
                initializedGoogleMap.addPolygon(building.getPolygonOptions()).tag = building.getName()
                var polygon: Polygon = initializedGoogleMap.addPolygon(building.getPolygonOptions())
                building.setPolygon(polygon)
            }
        }

        return googleMap
    }



}

