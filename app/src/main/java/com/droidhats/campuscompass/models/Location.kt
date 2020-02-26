package com.droidhats.campuscompass.models

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.json.JSONObject

/*
* Model for location classes
* Data relating to locations should be stored in this file
* */

abstract class Location(coordinate: LatLng) {
    private var coordinate: LatLng = coordinate
}

//
class Campus(coordinate: LatLng, name: String, jsonObject: JSONObject) : Location (coordinate){
    private lateinit var buildingsList: List<Building>
    private val name: String = name
    private val jsonObject: JSONObject = jsonObject
    fun getName(): String = name

    fun createBuildings(){
        /*TO-DO:
        - Move coordinates to buildings.json
        - Parse building info from JSON, create buildings & populate buildingsList
         */
        Log.d("JSON is", jsonObject.getString("buildings")) //so far this works, JSON is printed on Logcat/debug
    }
}

// Model for building class, data relating to buildings should be stored here
class Building(coordinate: LatLng, name: String, desc: String) : Location (coordinate) {
    private val name: String = name
    private val desc: String = desc
    private val polygonColor = 4289544510.toInt()
    private lateinit var polygon : Polygon
    private lateinit var polygonCoordinatesList: List<LatLng>

    fun getName(): String = name
    fun getDescription(): String = desc

    fun getPolygonOptions(): PolygonOptions{

        var polygonOptions = PolygonOptions()
            .fillColor(polygonColor)
            .clickable(true)

        for(polygonCoordinate in polygonCoordinatesList){
            polygonOptions.add(polygonCoordinate)
        }

        return polygonOptions
    }
}
