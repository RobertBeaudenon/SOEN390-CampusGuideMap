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

/**
 * A ViewModel for the map.
 * Receives data from the MapRepository and Sends initialized map and other information to the MapFragment .
 *
 * @constructor Reads data from the map repository.
 * @param application: The android view model interface requires that the (not null) main application be passed.
 */
class  MapViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val mapRepository: MapRepository
    internal val navigationRepository: NavigationRepository
    private var campuses: List<Campus>
    private var buildings: List<Building>

    init {
        mapRepository = MapRepository.getInstance(context)
        navigationRepository = NavigationRepository.getInstance(getApplication())
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
     * Initializes a map model object that contains an initialized google map.
     * Returns an initialized Map (map model) object.
     */
    fun getMapModel(googleMap: GoogleMap,
               mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
               mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
               mapFragmentOnCameraIdleListener: GoogleMap.OnCameraIdleListener,
               activity: FragmentActivity
    ): Map
    {
        //Get initialized map from Map Model.
        return Map.getInstance(
            googleMap, mapFragmentOnMarkerClickListener, mapFragmentOnPolygonClickListener,
            mapFragmentOnCameraIdleListener, activity, buildings
        )
    }

    fun getNavigationRoute() : MutableLiveData<NavigationRoute> = navigationRepository.getNavigationRoute()

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

    /**
     * Searches and returns the building object with the corresponding marker title.
     * @return If there is no match, it will return a null building object
     */
    fun findBuildingByMarkerTitle(marker: Marker?): Building?{
        var selectedBuilding: Building? = null

        //Iterate through all buildings in both campuses until the marker matches the building name
        for (building in this.buildings!!) {
            if (marker != null) {
                if (building.name == marker.title) {
                    selectedBuilding = building
                }
            }
        }
        return selectedBuilding
    }

}

