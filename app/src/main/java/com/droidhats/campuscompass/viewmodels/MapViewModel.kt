package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.models.*
import com.droidhats.campuscompass.models.Map
import com.droidhats.campuscompass.repositories.MapRepository
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.roomdb.FavoritesDatabase
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
    private val favoritesDb : FavoritesDatabase

    init {
        mapRepository = MapRepository.getInstance(context)
        navigationRepository = NavigationRepository.getInstance(getApplication())
        campuses = mapRepository.getCampuses()
        buildings = mapRepository.getBuildings()
        favoritesDb = FavoritesDatabase.getInstance(context)
    }

    fun getFavoritesDb() : FavoritesDatabase = favoritesDb

    /**
     * Returns a list of campus objects (SJW and Loyola).
     */
    fun getCampuses(): List<Campus> = campuses

    /**
     * Returns a list of all buildings in both campuses
     */
    fun getBuildings(): List<Building> = buildings

    /**
     * Initializes a map model object that contains an initialized google map.
     * Returns an initialized Map (map model) object.
     * @param mapFragmentOnMarkerClickListener: a listener for marker clicks that will be attached to the map.
     * @param mapFragmentOnPolygonClickListener: a listener for polygon clicks that will be attached to the map.
     * @param mapFragmentOnCameraIdleListener: a listener for when the camera is idle in the map.
     * @param activity: Used to check the location permission from the main activity.
     */
    fun getMapModel(googleMap: GoogleMap,
               mapFragmentOnMarkerClickListener: GoogleMap.OnMarkerClickListener,
               mapFragmentOnPolygonClickListener: GoogleMap.OnPolygonClickListener,
               mapFragmentOnCameraIdleListener: GoogleMap.OnCameraIdleListener,
               activity: FragmentActivity
    ): Map
    {
        // Get initialized map model from Map Model using singleton.
        val mapModel = Map(
            googleMap, buildings
        )

        if ((activity as MainActivity).checkLocationPermission()) {
            // Enables the my-location layer which draws a light blue dot on the user’s location.
            // It also adds a button to the map that, when tapped, centers the map on the user’s location.
            mapModel.googleMap.isMyLocationEnabled = true
        }

        mapModel.googleMap.setOnPolygonClickListener(mapFragmentOnPolygonClickListener)
        mapModel.googleMap.setOnCameraIdleListener(mapFragmentOnCameraIdleListener)
        mapModel.googleMap.setOnMarkerClickListener(mapFragmentOnMarkerClickListener)

        return mapModel
    }

    fun getNavigationRoute() : MutableLiveData<NavigationRoute> = navigationRepository.getNavigationRoute()

    /**
     * Searches and returns the building object that matches the polygon tag from the mapFragment
     */
    fun findBuildingByPolygonTag(polygonTag: String): Building?
    {
        var selectedBuilding : Building? = null

        //Iterate through all buildings in both campuses until the polygon tag matches the building Name
        for (building in this.buildings) {
            if (polygonTag == building.name)
                selectedBuilding = building
        }
        return selectedBuilding
    }

    /**
     * Searches and returns the building object with the corresponding marker title.
     * @return If there is no match, it will return a null building object. This is handled by the
     * method calling this method
     */
    fun findBuildingByMarkerTitle(marker: Marker?): Building?{
        var selectedBuilding: Building? = null

        //Iterate through all buildings in both campuses until the marker matches the building name
        for (building in this.buildings) {
            if (marker != null && building.name == marker.title) {
                    selectedBuilding = building
            }
        }
        return selectedBuilding
    }

    /**
     * Searches and returns the building object that matches the initial
     */
    fun findBuildingByInitial(initial: String): Building?
    {
        var selectedBuilding : Building? = null

        //Iterate through all buildings in both campuses until the initial matches the building initial
        for (building in this.buildings) {
            if (initial == building.getIndoorInfo().first)
                selectedBuilding = building
        }
        return selectedBuilding
    }
}

