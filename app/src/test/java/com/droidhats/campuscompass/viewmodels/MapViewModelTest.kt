package com.droidhats.campuscompass.viewmodels

import android.os.Build
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.Campus
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MapViewModelTest {
    private val viewmodel: MapViewModel = MapViewModel(RuntimeEnvironment.application)
    private lateinit var campuses: List<Campus>
    private lateinit var sgw: String
    private lateinit var loyola: String
    private lateinit var selectedBuilding: Building

    @Before
    fun setup(){
        campuses = viewmodel.getCampuses()
        sgw = "SGW"
        loyola = "Loyola"
    }

    @Test
    fun initTest() {
        // Assert that the proper campuses are there
        Assert.assertEquals(sgw, campuses[0].name)
        Assert.assertEquals(loyola, campuses[1].name)
    }

    @Test
    fun getNavigationRepositoryTest(){
        val navigationRepository = viewmodel.navigationRepository
        Assert.assertNotNull(navigationRepository)
    }

    @Test
    fun findBuildingByPolygonTagTest_BuildingWithMarker(){
        //Extracting Hall building
        val expectedBuilding = campuses[0].getBuildings()[0]

        selectedBuilding = viewmodel.findBuildingByPolygonTag("Henry F. Hall Building")!!
        Assert.assertEquals(expectedBuilding, selectedBuilding)
    }

    @Test
    fun findBuildingByPolygonTagTest_NullMarker(){
        //Extracting RR Annex
        val expectedBuilding = campuses[0].getBuildings()[11]

        selectedBuilding = viewmodel.findBuildingByPolygonTag("RR Annex")!!
        Assert.assertEquals(expectedBuilding, selectedBuilding)
        //Annexes do not have markers as their center location is [0,0].
        Assert.assertEquals(false, selectedBuilding.hasCenterLocation())
    }
}