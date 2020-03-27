package com.droidhats.campuscompass.repositories

import android.content.Context
import android.util.Log
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.lang.StringBuilder

/**
 * A Repository for the map.
 * Reads data from external files and process this data to initialize campus and building objects.
 *
 * @constructor Uses the application context to locate and read external files.
 * @param applicationContext: Used to start an input stream that reads external files.
 */
class MapRepository(applicationContext: Context) {

    private var campuses: MutableList<Campus> = mutableListOf()
    private var buildings: MutableList<Building> = mutableListOf()
    private val jsonObject: JSONObject

    fun getCampuses(): List<Campus> = campuses

    /**
     * Returns a list of all buildings.
     */
    fun getBuildings(): List<Building> = buildings

    init {
        val inputStream: InputStream = applicationContext.assets.open("buildings.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        jsonObject = JSONObject(json)
        initializeCampuses()
        initializeBuildings()
    }

    companion object {
        // Singleton instantiation
        private var instance: MapRepository? = null

        fun getInstance(applicationContext: Context) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: MapRepository(applicationContext).also { instance = it }
                }
    }

    /**
     * Initializes the objects for both the SGW and Loyola campuses
     */
    private fun initializeCampuses() {

        // Parse the json object to read the lat and lang coordinates of both campuses
        val sgwLatCoordinate: Double = jsonObject.getJSONArray("SGW_Campus_Location")[0].toString().toDouble()
        val sgwLongCoordinate: Double = jsonObject.getJSONArray("SGW_Campus_Location")[1].toString().toDouble()
        val loyolaLatCoordinate: Double = jsonObject.getJSONArray("Loyola_Campus_Location")[0].toString().toDouble()
        val loyolaLongCoordinate: Double = jsonObject.getJSONArray("Loyola_Campus_Location")[1].toString().toDouble()
        val sgwCampusCoordinates = LatLng(sgwLatCoordinate, sgwLongCoordinate)
        val loyolaCampusCoordinates = LatLng(loyolaLatCoordinate, loyolaLongCoordinate)

        // Initialize both campuses using the parsed coordinates and the list of buildings.
        campuses.add(
            Campus(sgwCampusCoordinates, "SGW", getBuildingsFromJSON("SGW"))
        )
        campuses.add(
            Campus(loyolaCampusCoordinates, "Loyola", getBuildingsFromJSON("Loyola"))
        )
    }

    /**
     * Initializes and returns a list of all building objects in a specific campus by parsing
     * the buildings.json data
     *
     * @param campusName: Specifies which campus data will be processed.
     */
    private fun getBuildingsFromJSON(campusName: String): List<Building> {
        val buildingsList: MutableList<Building> = mutableListOf()
        try {
            val buildingsArray : JSONArray = when (campusName) {
                // Important that at the creation of the campus object, its name is either SGW or
                // Loyola; otherwise the parsing fails
                "SGW" -> {
                    jsonObject.getJSONArray("SGW_buildings")
                }
                "Loyola" -> {
                    jsonObject.getJSONArray("LOY_buildings")
                }
                else -> {
                    throw JSONException(
                        "Unable to parse buildings from JSON\nMake " +
                        " that at the creation of the campus object the name parameter is " +
                        "either SGW or Loyola)\nMake sure that the values of the string " +
                        "resources SGW_Campus_Name and Loyola_Campus_Name are 'SGW' " +
                        "and 'Loyola'"
                    )
                }
            }

            var coordinatesArray : JSONArray

            // Traverse each building in the array
            for(i in 0 until buildingsArray.length()) {
                val buildingName : String = buildingsArray.getJSONObject(i).get("name").toString()
                val buildingAddress : String = buildingsArray.getJSONObject(i).get("address").toString()
                val buildingLocationArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("location")
                val buildingCenterLocationArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("center_location")
                val departmentsArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("departments")
                val servicesArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("services")
                val buildingLocation = LatLng(
                    buildingLocationArray[0].toString().toDouble(),
                    buildingLocationArray[1].toString().toDouble()
                )
                val buildingCenterLocation = LatLng(
                    buildingCenterLocationArray[0].toString().toDouble(),
                    buildingCenterLocationArray[1].toString().toDouble()
                )
                val buildingImageResourceID: Int = this.getBuildingImageResourceID(buildingName)!!
                val buildingMarkersIcons: Int = this.getBuildingMarkersIcons(buildingName)!!

                coordinatesArray = buildingsArray.getJSONObject(i).getJSONArray("coordinates")
                val openHoursArray = buildingsArray.getJSONObject(i).getJSONArray("open_hours")
                val polygonCoordinatesList: MutableList<LatLng> = mutableListOf()

                val hoursBuilder = StringBuilder()

                //Traverse each opening hours array of each building
                for(j in 0 until openHoursArray.length()) {
                    val day: String = openHoursArray.getJSONArray(j)[0].toString()
                    val hours: String = openHoursArray.getJSONArray(j)[1].toString()

                    if(j == openHoursArray.length()){
                        hoursBuilder.append(day + "\t" + hours)
                    } else {
                        hoursBuilder.append(day + "\t" + hours + "\n")
                    }
                }

                // Traverse each coordinate arrays of each building
                for(k in 0 until coordinatesArray.length()) {
                    val latCoordinate: Double = coordinatesArray.getJSONArray(k)[0].toString().toDouble()
                    val longCoordinate: Double = coordinatesArray.getJSONArray(k)[1].toString().toDouble()

                    // Add all the edge coordinates of the building to the list
                    polygonCoordinatesList.add(LatLng(latCoordinate, longCoordinate))
                }

                buildingsList.add(Building(buildingLocation, buildingName, buildingCenterLocation,
                     polygonCoordinatesList, buildingAddress, hoursBuilder.toString(),
                    getInfoFromTraversal(departmentsArray), getInfoFromTraversal(servicesArray),
                    buildingImageResourceID, buildingMarkersIcons))
            }
        } catch(e: JSONException) {
            Log.v("Parsing error", "Make sure that:" +
                    "\nJSON has arrays 'SGW_buildings' and 'LOY_buildings'" +
                    "\nJSON has NO typos using https://jsonlint.com/ ")
            Log.v("Parsing error", e.toString())
        }
        return buildingsList
    }

    //Helper method
    private fun getInfoFromTraversal(jsonArray: JSONArray): String{
        val builder = StringBuilder()

        //Traverse each object in the array
        for(k in 0 until jsonArray.length()){
            if(k == jsonArray.length()){
                builder.append(jsonArray[k].toString())
            }else{
                builder.append(jsonArray[k].toString() + "\n")
            }
        }
        return builder.toString()
    }

    /**
     * Constructs a list of all Concordia buildings by combining the lists of all buildings in each
     * campus
     */
    private fun initializeBuildings() {
        // Iterate through both campuses and add all the buildings in each to the buildings class var
        for (campus in this.campuses) {
            buildings.addAll(campus.getBuildings())
        }
    }

    /**
     * Returns the building image from drawable resources
     * @param buildingName: Used to map the building name to the building image.
     */
    private fun getBuildingImageResourceID(buildingName: String): Int? {

        // The id for the building image resource is of Int type
        // Return the building image resource id that corresponds to the building name
        return when (buildingName) {
            "Henry F. Hall Building" -> R.drawable.building_hall
            "EV Building" -> R.drawable.building_ev
            "John Molson School of Business" -> R.drawable.building_jmsb
            "Faubourg Saint-Catherine Building" -> R.drawable.building_fg_sc
            "Guy-De Maisonneuve Building" -> R.drawable.building_gm
            "Faubourg Building" -> R.drawable.building_fg
            "Visual Arts Building" -> R.drawable.building_va
            "Pavillion J.W. McConnell Building" -> R.drawable.building_webster_library
            "Grey Nuns Building" -> R.drawable.building_grey_nuns
            "Samuel Bronfman Building" -> R.drawable.building_sb
            "GS Building" -> R.drawable.building_gs
            "Grey Nuns Annex" -> R.drawable.building_ga
            "CL Annex" -> R.drawable.building_cl
            "Q Annex" -> R.drawable.building_q
            "T Annex" -> R.drawable.building_t
            "RR Annex" -> R.drawable.building_rr
            "R Annex" -> R.drawable.building_r
            "FA Annex" -> R.drawable.building_fa
            "LD Building" -> R.drawable.building_ld
            "X Annex" -> R.drawable.building_x
            "Z Annex" -> R.drawable.building_z
            "V Annex" -> R.drawable.building_v
            "S Annex" -> R.drawable.building_s
            "CI Annex" -> R.drawable.building_ci
            "MU Annex" -> R.drawable.building_mu
            "B Annex" -> R.drawable.building_b
            "D Annex" -> R.drawable.building_d
            "MI Annex" -> R.drawable.building_mi
            "Psychology Building" -> R.drawable.building_p
            "Richard J. Renaud Science Complex" -> R.drawable.building_rjrsc
            "Communication Studies and Journalism Building" -> R.drawable.building_csj
            "Administration Building" -> R.drawable.building_a
            "Loyola Jesuit and Conference Centre" -> R.drawable.building_ljacc
            "Vanier Library Building" -> R.drawable.building_vl
            "Vanier Extension" -> R.drawable.building_ve
            "Student Center" -> R.drawable.building_sc
            "F.C. Smith. Building" -> R.drawable.building_fc
            "Stinger Dome" -> R.drawable.building_do
            "PERFORM centre" -> R.drawable.building_pc
            "Jesuit Residence" -> R.drawable.building_jr
            "Physical Services Building" -> R.drawable.building_ps
            "Oscar Peterson Concert Hall" -> R.drawable.building_pt
            "Learning Square" -> R.drawable.building_ls
            "Central Building" -> R.drawable.building_cc
            else -> Log.v("Error loading images", "couldn't load image")
        }
    }

    /**
     * Returns the building marker icon from drawable resources
     * @param buildingName: Used to map the building name to the building marker icon.
     */
    private fun getBuildingMarkersIcons(buildingName: String) : Int?{

        // The id for the building icon resource is of Int type
        // Return the building image resource id that corresponds to the building name
        return when (buildingName) {
            "Henry F. Hall Building" -> R.mipmap.ic_building_h
            "EV Building" -> R.mipmap.ic_building_ev
            "John Molson School of Business" -> R.mipmap.ic_building_jm
            "Faubourg Saint-Catherine Building" -> R.mipmap.ic_building_fg
            "Guy-De Maisonneuve Building" -> R.mipmap.ic_building_gm
            "Faubourg Building" -> R.mipmap.ic_building_fb
            "Visual Arts Building" -> R.mipmap.ic_building_va
            "Pavillion J.W. McConnell Building" -> R.mipmap.ic_building_lb
            "Grey Nuns Building" -> R.mipmap.ic_building_gn
            "Samuel Bronfman Building" -> R.mipmap.ic_building_sb
            "GS Building" -> R.mipmap.ic_building_gs
            "Learning Square" ->  R.mipmap.ic_building_ls
            "Psychology Building" -> R.mipmap.ic_building_py
            "Richard J. Renaud Science Complex" -> R.mipmap.ic_building_sp
            "Central Building" -> R.mipmap.ic_building_cc
            "Communication Studies and Journalism Building" -> R.mipmap.ic_building_cj
            "Administration Building" -> R.mipmap.ic_building_ad
            "Loyola Jesuit and Conference Centre" -> R.mipmap.ic_building_rf
            "Vanier Library Building" -> R.mipmap.ic_building_vl
            "Vanier Extension" -> R.mipmap.ic_building_ve
            "Student Centre" -> R.mipmap.ic_building_sc
            "F.C. Smith Building" ->R.mipmap.ic_building_fc
            "Stinger Dome" -> R.mipmap.ic_building_do
            "PERFORM Centre" -> R.mipmap.ic_building_pc
            "Jesuit Residence" -> R.mipmap.ic_building_jr
            "Physical Services Building" -> R.mipmap.ic_building_ps
            "Oscar Peterson Concert Hall" -> R.mipmap.ic_building_pt
            else -> Log.v("Error loading marker", "couldn't load marker icons")
        }
    }

}