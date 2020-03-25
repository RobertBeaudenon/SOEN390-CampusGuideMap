package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.models.Map
import com.droidhats.campuscompass.models.NavigationRoute
import com.droidhats.campuscompass.repositories.MapRepository
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
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
    internal var navigationRepository: NavigationRepository

    init {
        campuses = mapRepository.getCampuses()
        buildings = mapRepository.getBuildings()
        navigationRepository = NavigationRepository.getInstance(getApplication())
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
               mapFragmentOnCameraIdleListener: GoogleMap.OnCameraIdleListener,
               activity: FragmentActivity
    ): GoogleMap
    {
        //Get initialized map from Map Model.
        val initializedGoogleMap: GoogleMap = Map(googleMap, mapFragmentOnMarkerClickListener, mapFragmentOnPolygonClickListener, mapFragmentOnCameraIdleListener, activity).getMap()

        //Highlight the buildings in both SGW and Loyola Campuses
        for (building in this.buildings!!) {
            initializedGoogleMap.addPolygon(building.getPolygonOptions()).tag = building.name
            val polygon: Polygon = initializedGoogleMap.addPolygon(building.getPolygonOptions())
            building.setPolygon(polygon)
            
            // Place marker on buildings that have center locations specified in buildings.json  
            if(building.hasCenterLocation()){
                val marker: Marker = initializedGoogleMap.addMarker(building.getMarkerOptions())
                building.setMarker(marker)
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
        for (building in this.buildings!!) {
            if (polygonTag == building.name)
                selectedBuilding = building
        }
        return selectedBuilding
    }


    fun getNavigationRoute() : MutableLiveData<NavigationRoute> = navigationRepository.getNavigationRoute()

    /**
     * Searches and returns the building object with the corresponding marker title.
     * @return If there is no match, it will return a null building object
     */
    fun findBuildingByMarkerTitle(marker: Marker?): Building?{
        var selectedBuilding: Building? = null

        //Iterate through all buildings in both campuses until the marker matches the building name
        for (campus in this.campuses!!) {
            for (building in campus.getBuildings()) {
                if (marker != null) {
                    if (building.name == marker.title) {
                        selectedBuilding = building
                    }
                }
            }
        }
        return selectedBuilding
    }

}

