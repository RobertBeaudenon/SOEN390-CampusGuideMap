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
        val SGW_LatCoordinate: Double = jsonObject.getJSONArray("SGW_Campus_Location")[0].toString().toDouble()
        val SGW_LongCoordinate: Double = jsonObject.getJSONArray("SGW_Campus_Location")[1].toString().toDouble()
        val Loyola_LatCoordinate: Double = jsonObject.getJSONArray("Loyola_Campus_Location")[0].toString().toDouble()
        val Loyola_LongCoordinate: Double = jsonObject.getJSONArray("Loyola_Campus_Location")[1].toString().toDouble()
        val SGW_Campus_Coordinates = LatLng(SGW_LatCoordinate, SGW_LongCoordinate)
        val Loyola_Campus_Coordinates = LatLng(Loyola_LatCoordinate, Loyola_LongCoordinate)

        // Initialize both campuses using the parsed coordinates and the list of buildings.
        campuses.add(
            Campus(SGW_Campus_Coordinates, "SGW", getBuildingsFromJSON("SGW"))
        )
        campuses.add(
            Campus(Loyola_Campus_Coordinates, "Loyola", getBuildingsFromJSON("Loyola"))
        )
    }

    /**
     * Initializes and returns a list of all building objects in a specific campus by parsing
     * the buildings.json data
     *
     * @param campusName: Specifies which campus data will be processed.
     */
    private fun getBuildingsFromJSON(campusName: String): List<Building> {
        var buildingsList: MutableList<Building> = mutableListOf()
        try {
            var buildingsArray : JSONArray = when (campusName) {
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
                val departmentsArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("departments")
                val servicesArray: JSONArray = buildingsArray.getJSONObject(i)
                    .getJSONArray("services")
                val buildingLocation = LatLng(
                    buildingLocationArray[0].toString().toDouble(),
                    buildingLocationArray[1].toString().toDouble()
                )
                var buildingImageResourceID: Int = this.getBuildingImageResourceID(buildingName)!!

                coordinatesArray = buildingsArray.getJSONObject(i).getJSONArray("coordinates")
                var openHoursArray = buildingsArray.getJSONObject(i).getJSONArray("open_hours")
                var polygonCoordinatesList: MutableList<LatLng> = mutableListOf()

                var hoursBuilder = StringBuilder()

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

                // Create the building object and add it to the list of buildings
                buildingsList.add(Building(buildingLocation, buildingName, polygonCoordinatesList, buildingAddress, hoursBuilder.toString(), getInfoFromTraversal(departmentsArray), getInfoFromTraversal(servicesArray), buildingImageResourceID))
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
     *
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
            "Psychology Building" -> R.drawable.building_p
            "Richard J. Renaud Science Complex" -> R.drawable.building_rjrsc
            "Central Building" -> R.drawable.building_cb
            "Communication Studies and Journalism Building" -> R.drawable.building_csj
            "Administration Building" -> R.drawable.building_a
            "Loyola Jesuit and Conference Centre" -> R.drawable.building_ljacc
            else -> Log.v("Error loading images", "couldn't load image")
        }
    }

}