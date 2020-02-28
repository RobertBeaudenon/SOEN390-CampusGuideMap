package com.droidhats.campuscompass.models

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/*
* Model for location classes
* Data relating to locations should be stored in this file
* */

abstract class Location(coordinate: LatLng) {
    private var coordinate: LatLng = coordinate
}

// Model for Campus class
class Campus(coordinate: LatLng, name: String, jsonObject: JSONObject) : Location(coordinate) {
    private var buildingsList: MutableList<Building> = mutableListOf()
    private val name: String = name
    private val jsonObject: JSONObject = jsonObject
    fun getName(): String = name
    fun getBuildings(): MutableList<Building> = buildingsList

    init{
        createBuildings()
    }

    private fun createBuildings() {
        try{
            var buildingsArray : JSONArray = when (name) {
                "SGW" -> { //Important that at the creation of the campus object, its name is either SGW or Loyola; otherwise the parsing fails
                    jsonObject.getJSONArray("SGW_buildings")
                }
                "Loyola" -> {
                    jsonObject.getJSONArray("LOY_buildings")
                }
                else -> {
                    Log.v("Parsing error", "Unable to parse buildings from JSON\nMake sure that at the creation of the campus object the name parameter is either SGW or Loyola)" +
                            "\nMake sure that the values of the string resources SGW_Campus_Name and Loyola_Campus_Name are 'SGW' and 'Loyola'")
                    return
                }
            }

            var coordinatesArray : JSONArray

            //Traverse each building in the array
            for(i in 0 until buildingsArray.length()){
                val buildingName : String = buildingsArray.getJSONObject(i).get("name").toString()
                val buildingLocationArray: JSONArray = buildingsArray.getJSONObject(i).getJSONArray("location")
                val buildingLocation = LatLng(buildingLocationArray[0].toString().toDouble(), buildingLocationArray[1].toString().toDouble())

                //Create the building object
                var building = Building(buildingLocation, buildingName)
                coordinatesArray = buildingsArray.getJSONObject(i).getJSONArray("coordinates")

                //Traverse each coordinate arrays of each building
                for(j in 0 until coordinatesArray.length()){

                    val latCoordinate: Double = coordinatesArray.getJSONArray(j)[0].toString().toDouble()
                    val longCoordinate: Double = coordinatesArray.getJSONArray(j)[1].toString().toDouble()

                    //Add all the edge coordinates of the building to the list
                    building.getPolygonCoordinatesList().add(LatLng(latCoordinate, longCoordinate))
                }

                buildingsList.add(building)
            }
        }catch(e: JSONException){
            Log.v("Parsing error", "Make sure that:" +
                    "\nJSON has arrays 'SGW_buildings' and 'LOY_buildings'" +
                    "\nJSON has NO typos using https://jsonlint.com/ ")
        }
    }
}

// Model for building class, data relating to buildings should be stored here
class Building(coordinate: LatLng, name: String) : Location(coordinate) {
    private var location : LatLng = coordinate
    private val name: String = name
    private val polygonColor = 4289544510.toInt()
    private lateinit var polygon: Polygon
    private var polygonCoordinatesList: MutableList<LatLng> = mutableListOf()

    fun getName(): String = name
    fun getLocation(): LatLng = location
    fun getPolygonCoordinatesList(): MutableList<LatLng> = polygonCoordinatesList
    fun getPolygon(): Polygon = polygon

    fun setPolygon(buildingPolygon: Polygon){
        this.polygon = buildingPolygon
        this.polygon.tag = name
    }

    fun getPolygonOptions(): PolygonOptions {

        var polygonOptions = PolygonOptions()
            .fillColor(polygonColor)
            .strokeWidth(2F)
            .clickable(true)
        for (polygonCoordinate in polygonCoordinatesList) {
            polygonOptions.add(polygonCoordinate)
        }
        return polygonOptions
    }
}
