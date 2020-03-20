package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.models.Map
import com.droidhats.campuscompass.repositories.MapRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon

/**
 * A ViewModel for the map.
 * Receives data from the MapRepository and Sends initialized map and other information to the MapFragment .
 *
 * @constructor Reads data from the map repository.
 * @param application: The android view model interface requires that the (not null) main application be passed.
 */
class  MapViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private var campuses: List<Campus>? = null
    private var buildings: List<Building>? = null
    private val mapRepository = MapRepository.getInstance(context)

    init {
        campuses = mapRepository.getCampuses()
        buildings = mapRepository.getBuildings()
    }

    /**
     * Returns a list of campus objects (SJW and Loyola).
     */
    fun getCampuses(): List<Campus> = campuses!!

    /**
     * Returns a list of all buildings in both campuses
     */
    fun getBuildings(): List<Building> = buildings!!

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
        for (building in this.buildings!!) {
            initializedGoogleMap.addPolygon(building.getPolygonOptions()).tag = building.getName()
            var polygon: Polygon = initializedGoogleMap.addPolygon(building.getPolygonOptions())
            building.setPolygon(polygon)
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
        for (building in this.buildings!!) {
            if (polygonTag == building.getName())
                selectedBuilding = building
        }

        return selectedBuilding
    }

}

