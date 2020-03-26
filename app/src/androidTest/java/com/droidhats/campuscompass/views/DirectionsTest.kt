package com.droidhats.campuscompass.views

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
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
 * Test for directions button. It generates directions from current location to H building
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class DirectionsTest {
    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
    private val twoSeconds: Long = 2000
    private val fiveSeconds: Long = 5000

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
            waitFor(fiveSeconds)

            //Checking if that action id did take you to map_fragment view
            onView(ViewMatchers.withId(R.id.coordinate_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun testDirectionButton(){
        val device = UiDevice.getInstance(getInstrumentation())

        //Allow downtown map to fully load
        waitFor(twoSeconds)

        val hMarker = device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. "))
        hMarker.click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))

        //Expand bottom sheet
        bottomSheet.swipe(Direction.UP, 1.0f)

        val directionsButton = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_directions_button"))
        directionsButton.click()

        waitFor(twoSeconds) //allow NavigationFragment to load

        //Verify start navigation button is displayed & click it
        onView(ViewMatchers.withId(R.id.startNavigationButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Verify Instructions button is displayed & click it
       onView(ViewMatchers.withId(R.id.buttonInstructions))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Verify instructions are displayed
        onView(ViewMatchers.withId(R.id.instructionsStepsID))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Verify close instructions buttons is displayed & click it
        onView(ViewMatchers.withId(R.id.buttonCloseInstructions))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    private fun waitFor(duration: Long) = Thread.sleep(duration)
}