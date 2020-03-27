package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Direction
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test for buildings initials (Markers) in the map of the MapFragment class.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class MarkerTest {
    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )
    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)

        //Ensuring the app starts with splash_fragment
        if (navController.currentDestination?.id == R.id.splash_fragment) {

            //navigating to map_fragment - You can put either action id or destination id.
            //I chose the action id so I can check that it indeed takes you the specified destination id.
            navController.navigate(R.id.action_splashFragment_to_mapsActivity)

            //Checking if action id indeed took you to the correct destination id
            Assert.assertEquals(navController.currentDestination?.id!!, R.id.map_fragment)

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            Espresso.onView(ViewMatchers.withId(R.id.coordinate_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun testBuildingMarkers(){
        val device = UiDevice.getInstance(getInstrumentation())
        val toggleButton = device.findObject(By.res("com.droidhats.campuscompass:id/toggleButton"))
        val googleMap = device.findObject(UiSelector().descriptionContains("Google Map"))

        //Allow downtown map to fully load
        Thread.sleep(2000)

        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))
        //Click on the map to dismiss bottom sheet for coverage
        UiDevice.getInstance(getInstrumentation()).click(1200, 600)

        device.findObject(UiSelector().descriptionContains("Pavillion J.W. McConnell Building. ")).click()
        //Expand bottom sheet fully for coverage
        bottomSheet.swipe(Direction.UP, 1.0f)
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Visual Arts Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("GS Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Learning Square. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("EV Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("John Molson School of Business. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Faubourg Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Grey Nuns Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on FG, GM marker
        googleMap.pinchOut(20, 50)

        device.findObject(UiSelector().descriptionContains("Faubourg Saint-Catherine Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Guy-De Maisonneuve Building. ")).clickTopLeft()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        Thread.sleep(2000)

        // Switch to Loyola campus
        toggleButton.click()

        Thread.sleep(2000)

        device.findObject(UiSelector().descriptionContains("Jesuit Residence. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Richard J. Renaud Science Complex. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Communication Studies and Journalism Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("PERFORM Centre. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Stinger Dome. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on the rest of these markers
        googleMap.pinchOut(20, 50)

        device.findObject(UiSelector().descriptionContains("Physical Services Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Vanier Extension.")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Psychology Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("F.C. Smith Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Loyola Jesuit and Conference Centre. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Central Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Administration Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Oscar Peterson Concert Hall. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        device.findObject(UiSelector().descriptionContains("Vanier Library Building. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on SC marker
        googleMap.pinchOut(20, 50)

        device.findObject(UiSelector().descriptionContains("Student Centre. ")).click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Zoom out until markers disappear for coverage
        googleMap.pinchIn(80, 50)
    }
}