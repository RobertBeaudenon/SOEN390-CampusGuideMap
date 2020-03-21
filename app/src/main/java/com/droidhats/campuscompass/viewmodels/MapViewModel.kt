package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.models.Map
import com.droidhats.campuscompass.repositories.MapRepository
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import java.io.InputStream


/**
 * A ViewModel for the map.
 * Receives data from the MapRepository and Sends initialized map and other information to the MapFragment .
 *
 * @constructor Reads data from the map repository.
 *
 * @param application: The android view model interface requires that the (not null) main application be passed.
 */
class  MapViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var campuses: List<Campus>? = null
    internal var navigationRepository: NavigationRepository

    // The activity is required to access the assets to open our json file where the info
    // is stored
    init {
        val inputStream: InputStream = context.assets.open("buildings.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        campuses = MapRepository.getInstance(context).getCampuses()
        navigationRepository = NavigationRepository(getApplication())
    }

    /**
     * Returns a list of campus objects (SJW and Loyola).
     */
    fun getCampuses(): List<Campus> = campuses!!

    /**
     * Uses the Map Model to initialize the map, and then it draws teh polygons of the campus buildings.
     * Returns an initialized GoogleMap object.
     */
    fun getMap(googleMap: GoogleMap,
               mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
               mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
               activity: FragmentActivity
    ): GoogleMap
    {
        //Get initialized map from Map Model.
        var initializedGoogleMap: GoogleMap = Map(googleMap, mapFragmentOnMarkerClickListener, mapFragmentOnPolygonClickListener, activity).getMap()

        //Highlight the buildings in both SGW and Loyola Campuses
        for (campus in this.campuses!!) {
            for (building in campus.getBuildings()) {
                initializedGoogleMap.addPolygon(building.getPolygonOptions()).tag = building.name
                var polygon: Polygon = initializedGoogleMap.addPolygon(building.getPolygonOptions())
                building.setPolygon(polygon)
            }
        }

        return googleMap
    }

    /**
     * Searches and returns the building object that matches the polygon tag from the mapFragment
     */
    fun findBuildingByPolygonTag(polygonTag: String): Building?
    {
        var selectedBuilding : Building? = null

        //Iterate through all buildings in both campuses until the polygon tag matches the building Name
        for (campus in this.campuses!!) {
            for (building in campus.getBuildings()) {
                if (polygonTag == building.name)
                    selectedBuilding = building
            }
        }

        return selectedBuilding
    }
}

