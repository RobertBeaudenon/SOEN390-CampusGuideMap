package com.droidhats.campuscompass.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.droidhats.mapprocessor.ProcessMap
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.IndoorLocation
import org.json.JSONObject
import java.io.File
import java.io.InputStream

class IndoorLocationRepository private constructor(private val indoorLocationDao: IndoorLocationDao) {

    var currentMap: String = "hall8.svg"

    companion object {
        // Singleton instantiation
        private var instance: IndoorLocationRepository? = null

        fun getInstance(indoorLocationDao : IndoorLocationDao ) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: IndoorLocationRepository(indoorLocationDao).also { instance = it }
                }

        private var buildingNumberMap: MutableMap<String, MutableMap<Int, Int>> = mutableMapOf()
    }

    fun getIndoorLocations() : List<IndoorLocation> = indoorLocationDao.getAll()
    fun getClassrooms() : LiveData<List<IndoorLocation>> = indoorLocationDao.getAllClassrooms()
    fun getMatchedClassrooms(query : SimpleSQLiteQuery) : LiveData<List<IndoorLocation>>
            = indoorLocationDao.getMatchedClassrooms(query)
    fun insertIndoorLocation(loc: IndoorLocation) = indoorLocationDao.insertIndoorLocation(loc)

    fun initializeIndoorLocations(context: Context, map: MapRepository) {
        val inputStream: InputStream = context.assets.open("config.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        val config: String = jsonObject.getString("mode")
        if (config == "debug") {
            indoorLocationDao.deleteAllIndoor()
        }
        if (config == "debug" || indoorLocationDao.getOne().value == null) {
            for ((index, building) in map.getBuildings().withIndex()) {
                insertClasses(context, building, index)
            }
        }
    }

    private fun insertClasses(context: Context, building: Building, index: Int) {

        for (floorMap in building.getIndoorInfo().second) {
            val inputStream: InputStream = context.assets.open(floorMap.value)
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor = ProcessMap()
            mapProcessor.readSVGFromString(file)
            val classes = mapProcessor.getClasses()

            var floorValue: String = floorMap.value.split(building.getIndoorInfo().first)[1].split(".svg")[0]
            for ((x, classRoom) in classes.withIndex()) {
                if (classRoom.getID() == "") continue
                if (!classRoom.getID().contains(floorValue)) {
                    continue
                }
                val newClass = IndoorLocation(
                    classRoom.getID(),
                    convertIDToName(classRoom.getID(), building.getIndoorInfo().first, floorMap.key),
                    floorMap.key,
                    floorMap.value,
                    "classroom",
                    index,
                    building.coordinate.latitude,
                    building.coordinate.longitude
                )
                println(newClass)
                indoorLocationDao.insertIndoorLocation(newClass)
            }
        }

    }

    /**
     * Converts id, building name and floor number into the proper name
     * for the appropriate Class Room
     * @param id This is the id generated from the svg file
     * @param buildingName This is the name for which the room belongs
     * @param floorNumber This is the number of the floor within the building
     * @return returns the string of the generated room name
     */
    private fun convertIDToName(id: String, buildingName: String, floorNumber: String): String {
        return buildingName + "-" + floorNumber + id.substring(6, id.length)
    }
}

