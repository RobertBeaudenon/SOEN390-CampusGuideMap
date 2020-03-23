package com.droidhats.campuscompass.viewmodels

import android.os.Build
import com.droidhats.campuscompass.models.Campus
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MapViewModelTest {
    private val viewmodel: MapViewModel = MapViewModel(RuntimeEnvironment.application)

    @Test
    fun testInit() {
        val campuses: List<Campus> = viewmodel.getCampuses()
        val sgw: String = "SGW"
        val loyola: String = "Loyola"

        // Assert that the proper campuses are there
        Assert.assertEquals(sgw, campuses[0].getName())
        Assert.assertEquals(loyola, campuses[1].getName())
    }
}