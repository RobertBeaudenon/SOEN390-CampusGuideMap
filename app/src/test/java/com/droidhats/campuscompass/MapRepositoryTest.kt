package com.droidhats.campuscompass

import android.os.Build
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import com.droidhats.campuscompass.repositories.MapRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import org.junit.Test
import org.junit.Assert
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MapRepositoryTest {

    private val json: String = "{\n" +
            "    \"SGW_buildings\": [{\n" +
            "        \"name\": \"Henry F. Hall Building\",\n" +
            "        \"address\": \"Boulevard de Maisonneuve O, Montreal, QC H3G 1M8\",\n" +
            "        \"location\": [45.497320, -73.579031],\n" +
            "        \"coordinates\": [\n" +
            "            [45.497164, -73.579544],\n" +
            "            [45.497710, -73.579034],\n" +
            "            [45.497373, -73.578338],\n" +
            "            [45.496828, -73.578850]\n" +
            "        ],\n" +
            "        \"open_hours\": [\n" +
            "            [\"Monday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Tuesday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Wednesday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Thursday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Friday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Saturday\", \"9a.m.-5p.m.\"],\n" +
            "            [\"Sunday\", \"9a.m.-5p.m.\"]\n" +
            "        ],\n" +
            "        \"departments\": [\n" +
            "            \"Geography, Planning and Environment\",\n" +
            "            \"Political Science, Sociology and Anthropology, Economics\",\n" +
            "            \"School of Irish Studies\"\n" +
            "        ],\n" +
            "        \"services\": [\n" +
            "            \"Welcome Crew Office\",\n" +
            "            \"DB Clarke Theatre\",\n" +
            "            \"Dean of Students\",\n" +
            "            \"Aboriginal Student Resource Centre\",\n" +
            "            \"Concordia Student Union\"\n" +
            "        ]\n" +
            "    }]\n" +
            "}"
    private var instance: MapRepository = MapRepository.getInstance(json)

    @Test
    fun testCreateCampuses() {
        // Assert campus variables are well assigned
        val campuses: List<Campus> = instance.getCampuses()
        Assert.assertEquals(campuses[0].getName(), "SGW")
        Assert.assertEquals(campuses[0].getCoordinate(), LatLng(45.495637, -73.578235))
    }

    @Test
    fun testGetBuildingsFromJSON() {
        // Assert building variables were well assigned
        val buildings: List<Building> = instance.getCampuses()[0].getBuildings()
        val HBuilding: Building = buildings[0]
        Assert.assertEquals(HBuilding.getName(), "Henry F. Hall Building", HBuilding.getName())
        Assert.assertEquals(HBuilding.getOpenHours(), "Monday\t9a.m.-5p.m.\n" +
                "Tuesday\t9a.m.-5p.m.\n" +
                "Wednesday\t9a.m.-5p.m.\n" +
                "Thursday\t9a.m.-5p.m.\n" +
                "Friday\t9a.m.-5p.m.\n" +
                "Saturday\t9a.m.-5p.m.\n" +
                "Sunday\t9a.m.-5p.m.\n", HBuilding.getOpenHours())
        Assert.assertEquals(HBuilding.getLocation(), LatLng(45.497320, -73.579031))
        Assert.assertEquals(HBuilding.getAddress(), "Boulevard de Maisonneuve O, Montreal, QC H3G 1M8", HBuilding.getAddress())
        Assert.assertEquals(HBuilding.getDepartments(), "Geography, Planning and Environment\n" +
                "Political Science, Sociology and Anthropology, Economics\n" +
                "School of Irish Studies\n", HBuilding.getDepartments())
        Assert.assertEquals(HBuilding.getServices(), "Welcome Crew Office\n" +
                "DB Clarke Theatre\n" +
                "Dean of Students\n" +
                "Aboriginal Student Resource Centre\n" +
                "Concordia Student Union\n", HBuilding.getServices())

        val buildingOptions: PolygonOptions = HBuilding.getPolygonOptions()
        Assert.assertEquals(buildingOptions.fillColor, 4289544510.toInt())
        Assert.assertEquals(buildingOptions.strokeWidth, 2F)
        Assert.assertTrue(buildingOptions.isClickable)

        val coordinates: List<LatLng> = listOf(
            LatLng(45.497164, -73.579544),
            LatLng(45.497710, -73.579034),
            LatLng(45.497373, -73.578338),
            LatLng(45.496828, -73.578850)
        )
        Assert.assertEquals(buildingOptions.points, coordinates)
    }

}

