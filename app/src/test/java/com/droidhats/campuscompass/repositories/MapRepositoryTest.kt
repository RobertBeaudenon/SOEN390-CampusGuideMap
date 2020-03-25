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
    // App context is needed to create the repository and read the external file
    private val context: Context = RuntimeEnvironment.application.applicationContext

    @Test
    fun externalFileExistenceTest() {
        var inputStream: InputStream? = null

        // Try to open the file
        try {
            inputStream = context.assets.open("buildings.json")
        } catch (e: IOException) {
        }

        // Assert if input stream is null to check if the file exists
        Assert.assertNotNull("buildings.json file does not exist", inputStream)
    }

    // If the External file exists, then the repository can be created.
    private var instance: MapRepository = MapRepository.getInstance(context)

    @Test
    fun getCampusesTest() {
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
        Assert.assertEquals(campuses[0].getLocation(), LatLng(45.495784, -73.577197))
        Assert.assertEquals(campuses[1].name, "Loyola")
        Assert.assertEquals(campuses[1].getLocation(), LatLng(45.458220, -73.639702))
    }

    //Test whether the buildings from the json were correctly parsed.
    //Since the method traverses in a linear fashion, verifying the first and last building is sufficient.
    @Test
    fun getBuildingsFromJSONTest() {
        // Assert building variables were well assigned
        val sgwBuildings: List<Building> = instance.getCampuses()[0].getBuildings()
        val loyolaBuildings: List<Building> = instance.getCampuses()[1].getBuildings()

        //Verify first building: Hall
        val hBuilding: Building = sgwBuildings[0]
        Assert.assertEquals(hBuilding.name, "Henry F. Hall Building", hBuilding.name)
        Assert.assertEquals(
            hBuilding.getOpenHours(), "Monday\t7a.m.-11p.m.\n" +
                    "Tuesday\t7a.m.-11p.m.\n" +
                    "Wednesday\t7a.m.-11p.m.\n" +
                    "Thursday\t7a.m.-11p.m.\n" +
                    "Friday\t7a.m.-11p.m.\n" +
                    "Saturday\t7a.m.-11p.m.\n" +
                    "Sunday\t7a.m.-11p.m.\n", hBuilding.getOpenHours()
        )
        Assert.assertEquals(hBuilding.getLocation(), LatLng(45.497320, -73.579031))
        Assert.assertEquals(
            hBuilding.getAddress(),
            "1455, de, Maisonneuve Blvd W, Montréal, QC H3G 1M8",
            hBuilding.getAddress()
        )
        Assert.assertEquals(
            hBuilding.getPlaceId(),
            "ChIJtd6Zh2oayUwRAu_CnRIfoBw",
            hBuilding.getPlaceId()
        )
        Assert.assertEquals(hBuilding.getCenterLocation(), LatLng(45.497320, -73.579031))
        Assert.assertEquals(
            hBuilding.getDepartments(), "Geography, Planning and Environment\n" +
                    "Political Science, Sociology and Anthropology, Economics\n" +
                    "School of Irish Studies\n", hBuilding.getDepartments()
        )
        Assert.assertEquals(
            hBuilding.getServices(), "Welcome Crew Office\n" +
                    "DB Clarke Theatre\n" +
                    "Dean of Students\n" +
                    "Aboriginal Student Resource Centre\n" +
                    "Concordia Student Union\n", hBuilding.getServices()
        )

        val hBuildingPolygonOptionsOptions: PolygonOptions = hBuilding.getPolygonOptions()
        Assert.assertEquals(hBuildingPolygonOptionsOptions.fillColor, 4289544510.toInt())
        Assert.assertEquals(hBuildingPolygonOptionsOptions.strokeWidth, 2F)
        Assert.assertTrue(hBuildingPolygonOptionsOptions.isClickable)

        val hPolygonCoordinates: List<LatLng> = listOf(
            LatLng(45.497164, -73.579544),
            LatLng(45.497710, -73.579034),
            LatLng(45.497373, -73.578338),
            LatLng(45.496828, -73.578850)
        )
        Assert.assertEquals(hBuildingPolygonOptionsOptions.points, hPolygonCoordinates)

        val hMarkerOptions: MarkerOptions = hBuilding.getMarkerOptions()
        Assert.assertEquals(hMarkerOptions.position, LatLng(45.497320, -73.579031))
        Assert.assertEquals(hMarkerOptions.anchorU, 0.5f)
        Assert.assertEquals(hMarkerOptions.anchorV, 0.5f)
        Assert.assertEquals(hMarkerOptions.title, "Henry F. Hall Building")

        //Verify last building: Oscar Peterson Concert Hall
        val ptbuilding: Building = loyolaBuildings[14]

        Assert.assertEquals(ptbuilding.name, "Oscar Peterson Concert Hall", ptbuilding.name)
        Assert.assertEquals(
            ptbuilding.getOpenHours(), "Monday\t9a.m.-5p.m.\n" +
                    "Tuesday\t9a.m.-5p.m.\n" +
                    "Wednesday\t9a.m.-5p.m.\n" +
                    "Thursday\t9a.m.-5p.m.\n" +
                    "Friday\t9a.m.-5p.m.\n" +
                    "Saturday\t9a.m.-5p.m.\n" +
                    "Sunday\t9a.m.-5p.m.\n", ptbuilding.getOpenHours()
        )
        Assert.assertEquals(ptbuilding.getLocation(), LatLng(45.459355, -73.638976))
        Assert.assertEquals(
            ptbuilding.getAddress(),
            "7141 Sherbrooke St W, Montreal, Quebec H4B 1R6",
            ptbuilding.getAddress()
        )
        Assert.assertEquals(
            ptbuilding.getPlaceId(),
            "ChIJL7mz8TEXyUwRu9P6P2NMZYU",
            ptbuilding.getPlaceId()
        )
        Assert.assertEquals(ptbuilding.getCenterLocation(), LatLng(45.459324, -73.638960))
        Assert.assertEquals(ptbuilding.getDepartments(), "None\n", ptbuilding.getDepartments())
        Assert.assertEquals(
            ptbuilding.getServices(),
            "Oscar Peterson Concert Hall\n",
            ptbuilding.getServices()
        )

        val ptBuildingPolygonOptionsOptions: PolygonOptions = ptbuilding.getPolygonOptions()
        Assert.assertEquals(ptBuildingPolygonOptionsOptions.fillColor, 4289544510.toInt())
        Assert.assertEquals(ptBuildingPolygonOptionsOptions.strokeWidth, 2F)
        Assert.assertTrue(ptBuildingPolygonOptionsOptions.isClickable)

        val ptPolygonCoordinates: List<LatLng> = listOf(
            LatLng(45.4593165, -73.6391771),
            LatLng(45.4593344, -73.6392204),
            LatLng(45.4593508, -73.6392086),
            LatLng(45.4593598, -73.6392308),
            LatLng(45.4594813, -73.6391359),
            LatLng(45.4593010, -73.6386709),
            LatLng(45.4591622, -73.6387798),
            LatLng(45.4592203, -73.6389273)
        )
        Assert.assertEquals(ptBuildingPolygonOptionsOptions.points, ptPolygonCoordinates)

        val ptMarkerOptions: MarkerOptions = ptbuilding.getMarkerOptions()
        Assert.assertEquals(ptMarkerOptions.position, LatLng(45.459324, -73.638960))
        Assert.assertEquals(ptMarkerOptions.anchorU, 0.5f)
        Assert.assertEquals(ptMarkerOptions.anchorV, 0.5f)
        Assert.assertEquals(ptMarkerOptions.title, "Oscar Peterson Concert Hall")

    }

}

