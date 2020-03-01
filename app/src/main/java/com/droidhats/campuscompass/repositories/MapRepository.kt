package com.droidhats.campuscompass.repositories

import android.util.Log
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.StringBuilder

class MapRepository(json: String) {

    private var campuses: MutableList<Campus> = mutableListOf()
    private val jsonObject: JSONObject = JSONObject(json)

    fun getCampuses(): List<Campus> = campuses

    init {
        createCampuses()
    }

    companion object {
        // Singleton instantiation
        private var instance: MapRepository? = null

        fun getInstance(json: String) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: MapRepository(json).also { instance = it }
                }
    }

    private fun createCampuses() {

        //TODO: Refactor LatLng to not have to hardcode these values (get them from the JSON)
        campuses.add(
            Campus(
                LatLng(45.495637, -73.578235),
                "SGW",
                getBuildingsFromJSON("SGW")
            )
        )
        campuses.add(
            Campus(
                LatLng(45.458159, -73.640450),
                "Loyola",
                getBuildingsFromJSON("Loyola")
            )
        )
    }

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

                coordinatesArray = buildingsArray.getJSONObject(i).getJSONArray("coordinates")
                var openHoursArray = buildingsArray.getJSONObject(i).getJSONArray("open_hours")
                var polygonCoordinatesList: MutableList<LatLng> = mutableListOf()

                var hoursBuilder = StringBuilder()

                //Traverse each opening hours array of each building
                for(i in 0 until openHoursArray.length()){
                    val day: String = openHoursArray.getJSONArray(i)[0].toString()
                    val hours: String = openHoursArray.getJSONArray(i)[1].toString()

                    if(i == openHoursArray.length()){
                        hoursBuilder.append(day + "\t" + hours)
                    }else{
                        hoursBuilder.append(day + "\t" + hours + "\n")
                    }
                }

                // Traverse each coordinate arrays of each building
                for(j in 0 until coordinatesArray.length()){
                    val latCoordinate: Double = coordinatesArray.getJSONArray(j)[0].toString().toDouble()
                    val longCoordinate: Double = coordinatesArray.getJSONArray(j)[1].toString().toDouble()

                    // Add all the edge coordinates of the building to the list
                    polygonCoordinatesList.add(LatLng(latCoordinate, longCoordinate))
                }

                buildingsList.add(Building(buildingLocation, buildingName, polygonCoordinatesList, buildingAddress, getInfoFromTraversal(departmentsArray), getInfoFromTraversal(servicesArray), hoursBuilder.toString()))
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
}