package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)

class MarkerTest {
    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
        Thread.sleep(5000)
    }

    @Test
    fun testBuildingMarkers(){
        val fiveSeconds = 5000

        // Wait for the Map View to load
        waitFor(fiveSeconds)
        Espresso.onView(ViewMatchers.withId(R.id.toggleButton)).perform(ViewActions.click())
        waitFor(fiveSeconds)
        val device = UiDevice.getInstance(getInstrumentation())
        val marker = device.findObject(UiSelector().descriptionContains("EV Building. "))
        marker.click()
    }

    private fun waitFor(duration: Int) = Thread.sleep(duration.toLong())
}