package com.droidhats.campuscompass.repositories

import android.content.Context
import android.os.Build
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MapRepositoryTest {

    private val json: String = "    \"SGW_buildings\": [{\n" +
            "            \"name\": \"Henry F. Hall Building\",\n" +
            "            \"address\": \"1455, de, Maisonneuve Blvd W, Montréal, QC H3G 1M8\",\n" +
            "\t\t\t\"center_location\": [45.497320, -73.579031],\n" +
            "            \"location\": [45.497320, -73.579031],\n" +
            "            \"coordinates\": [\n" +
            "                [45.497164, -73.579544],\n" +
            "                [45.497710, -73.579034],\n" +
            "                [45.497373, -73.578338],\n" +
            "                [45.496828, -73.578850]\n" +
            "            ],\n" +
            "            \"open_hours\": [\n" +
            "                [\"Monday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Tuesday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Wednesday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Thursday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Friday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Saturday\", \"7a.m.-11p.m.\"],\n" +
            "                [\"Sunday\", \"7a.m.-11p.m.\"]\n" +
            "            ],\n" +
            "            \"departments\": [\n" +
            "                \"Geography, Planning and Environment\",\n" +
            "                \"Political Science, Sociology and Anthropology, Economics\",\n" +
            "                \"School of Irish Studies\"\n" +
            "            ],\n" +
            "            \"services\": [\n" +
            "                \"Welcome Crew Office\",\n" +
            "                \"DB Clarke Theatre\",\n" +
            "                \"Dean of Students\",\n" +
            "                \"Aboriginal Student Resource Centre\",\n" +
            "                \"Concordia Student Union\"\n" +
            "            ]\n" +
            "        }"

    // App context is needed to create the repository and read the external file
    private val context : Context = RuntimeEnvironment.application.applicationContext

    @Test
    fun externalFileExistenceTest(){
        var inputStream: InputStream? = null

        // Try to open the file
        try {
            inputStream = context.assets.open("buildings.json")
        } catch (e: IOException){}

        // Assert if input stream is null to check if the file exists
        Assert.assertNotNull("buildings.json file does not exist", inputStream)
    }

    // If the External file exists, then the repository can be created.
    private var instance: MapRepository = MapRepository.getInstance(context)

    @Test
    fun getCampusesTest(){
        //Assert campuses are returned as a list
        val campuses: List<Campus> = instance.getCampuses()
        Assert.assertNotNull(campuses)
        Assert.assertTrue(campuses is List<Campus>)
    }

    @Test
    fun createCampusesTest() {
        // Assert campus variables are well assigned
        val campuses: List<Campus> = instance.getCampuses()
        Assert.assertEquals(campuses[0].name, "SGW")
        Assert.assertEquals(campuses[0].getLocation(), LatLng(45.495637, -73.578235))
    }

    @Test
    fun getBuildingsFromJSONTest() {
        // Assert building variables were well assigned
        val buildings: List<Building> = instance.getCampuses()[0].getBuildings()
        val h_building: Building = buildings[0]
        Assert.assertEquals(h_building.name, "Henry F. Hall Building", h_building.getName())
        Assert.assertEquals(h_building.getOpenHours(), "Monday\t7a.m.-11p.m.\n" +
                "Tuesday\t7a.m.-11p.m.\n" +
                "Wednesday\t7a.m.-11p.m.\n" +
                "Thursday\t7a.m.-11p.m.\n" +
                "Friday\t7a.m.-11p.m.\n" +
                "Saturday\t7a.m.-11p.m.\n" +
                "Sunday\t7a.m.-11p.m.\n", h_building.getOpenHours())
        Assert.assertEquals(h_building.getLocation(), LatLng(45.497320, -73.579031))
        Assert.assertEquals(h_building.getAddress(), "1455, de, Maisonneuve Blvd W, Montréal, QC H3G 1M8", h_building.getAddress())
        Assert.assertEquals(h_building.getDepartments(), "Geography, Planning and Environment\n" +
                "Political Science, Sociology and Anthropology, Economics\n" +
                "School of Irish Studies\n", h_building.getDepartments())
        Assert.assertEquals(h_building.getServices(), "Welcome Crew Office\n" +
                "DB Clarke Theatre\n" +
                "Dean of Students\n" +
                "Aboriginal Student Resource Centre\n" +
                "Concordia Student Union\n", h_building.getServices())

        val buildingPolygonOptionsOptions: PolygonOptions = h_building.getPolygonOptions()
        Assert.assertEquals(buildingPolygonOptionsOptions.fillColor, 4289544510.toInt())
        Assert.assertEquals(buildingPolygonOptionsOptions.strokeWidth, 2F)
        Assert.assertTrue(buildingPolygonOptionsOptions.isClickable)

        val coordinates: List<LatLng> = listOf(
            LatLng(45.497164, -73.579544),
            LatLng(45.497710, -73.579034),
            LatLng(45.497373, -73.578338),
            LatLng(45.496828, -73.578850)
        )
        Assert.assertEquals(buildingPolygonOptionsOptions.points, coordinates)

        val markerOptions: MarkerOptions = h_building.getMarkerOptions()
        Assert.assertEquals(markerOptions.position, LatLng(45.497320, -73.579031))
        Assert.assertEquals(markerOptions.anchorU, 0.5f)
        Assert.assertEquals(markerOptions.anchorV, 0.5f)
        Assert.assertEquals(markerOptions.title, "Henry F. Hall Building")
    }

}

