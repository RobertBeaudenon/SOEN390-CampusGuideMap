package com.droidhats.campuscompass.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.DroidHats.ProcessMap
import com.droidhats.campuscompass.models.IndoorLocation
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.InputStream

class IndoorLocationRepository private constructor(private val indoorLocationDao: IndoorLocationDao) {

    companion object {
        // Singleton instantiation
        private var instance: IndoorLocationRepository? = null

        fun getInstance(indoorLocationDao : IndoorLocationDao ) =
            instance
                ?: synchronized(this) {
                    instance
                        ?: IndoorLocationRepository(indoorLocationDao).also { instance = it }
                }
    }

    fun getIndoorLocations() : LiveData<List<IndoorLocation>> = indoorLocationDao.getAll()
    fun getClassrooms() : LiveData<List<IndoorLocation>> = indoorLocationDao.getAllClassrooms()
    fun getMatchedClassrooms(query : SimpleSQLiteQuery) : LiveData<List<IndoorLocation>>
            = indoorLocationDao.getMatchedClassrooms(query)
    fun insertIndoorLocation(loc: IndoorLocation) = indoorLocationDao.insertIndoorLocation(loc)

    fun initializeIndoorLocations(context: Context) {
        val inputStream: InputStream = context.assets.open("config.json")
        val json: String = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        val config: String = jsonObject.getString("mode")
        if (config == "debug") {
            indoorLocationDao.deleteAllIndoor()
            insertClasses(context)
        }
        if (config == "production") {
            if (indoorLocationDao.getOne().value == null) {
                insertClasses(context)
            }
        }
    }

    fun insertClasses(context: Context) {
        val inputStream: InputStream = context.assets.open("hall8.svg")
        val file: String = inputStream.bufferedReader().use { it.readText() }
        val mapProcessor: ProcessMap = ProcessMap()
        mapProcessor.readSVGFromString(file)
        val classes = mapProcessor.getClasses()
        var x = 0
        for (classRoom in classes) {
            val newClass = IndoorLocation(
                classRoom.getID().substring(4, 7).toInt() + x,
                classRoom.getID(),
                8,
                "classroom"
            )
            indoorLocationDao.insertIndoorLocation(newClass)
            x++
        }
    }
}

