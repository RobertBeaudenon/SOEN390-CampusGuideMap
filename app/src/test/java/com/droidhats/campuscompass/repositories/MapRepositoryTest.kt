package com.droidhats.campuscompass.repositories

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

    private val json: String = "{\"SGW_buildings\": [{\n" +
            "\"name\": \"Henry F. Hall Building\",\n" +
            "\"address\": \"Pavillion Henry F.Hall Bldg, Boulevard de Maisonneuve O, Montreal, QC H3G 1M8\",\n" +
            "\"location\": [45.497320, -73.579031],\n" +
            "\"coordinates\": [\n" +
            "[45.497164, -73.579544],\n" +
            "[45.497710, -73.579034],\n" +
            "[45.497373, -73.578338],\n" +
            "[45.496828, -73.578850]\n" +
            "]\n" +
            "}]}"
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
        // Assert building varibales were well assigned
        val buildings: List<Building> = instance.getCampuses()[0].getBuildings()
        val HBuilding: Building = buildings[0]
        Assert.assertEquals(HBuilding.getName(), "Henry F. Hall Building", HBuilding.getName())
        Assert.assertEquals(HBuilding.getLocation(), LatLng(45.497320, -73.579031))

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

