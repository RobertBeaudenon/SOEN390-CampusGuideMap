package com.droidhats.campuscompass.views

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import junit.framework.AssertionFailedError
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
            ApplicationProvider.getApplicationContext()
        )
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
            onView(ViewMatchers.withId(R.id.coordinate_layout))
                .check(matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun testDirectionsButton() {
        val device = UiDevice.getInstance(getInstrumentation())

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))

        //Expand bottom sheet
        bottomSheet.swipe(Direction.UP, 1.0f)

        //Click directions button
        device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_directions_button"))
            .click()

        Thread.sleep(2000) //allow NavigationFragment to load

        //Verify start navigation button is displayed & click it
        onView(ViewMatchers.withId(R.id.startNavigationButton))
            .check(matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Click close instructions button button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonMinimizeInstructions"))
            .click()

        //Click Resume Navigation button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonResumeNavigation"))
            .click()

        val nextArrow = onView(ViewMatchers.withId(R.id.nextArrow))
            .check(matches(ViewMatchers.isDisplayed()))

        do {
            nextArrow.perform(ViewActions.click())
        } while (nextArrowIsDisplayed())

        // Reached the end of the step instructions

        //Press previous arrow for coverage
        device.findObject(By.res("com.droidhats.campuscompass:id/prevArrow"))
            .click()

        //Press previous arrow for coverage
        device.findObject(By.res("com.droidhats.campuscompass:id/nextArrow"))
            .click()
    }

    @Test
    fun testDirectionsButtonByWalkingMode() {
        val device = UiDevice.getInstance(getInstrumentation())

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Click directions button
        device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_directions_button"))
            .click()

        Thread.sleep(5000) //allow NavigationFragment to load

        //Verify transport mode radio button and click it
        onView(ViewMatchers.withId(R.id.radio_transport_mode_walking))
            .check(matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Verify start navigation button is displayed & click it
        onView(ViewMatchers.withId(R.id.startNavigationButton))
            .check(matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Click close instructions button button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonMinimizeInstructions"))
            .click()

        //Click Resume Navigation button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonResumeNavigation"))
            .click()

        val nextArrow = onView(ViewMatchers.withId(R.id.nextArrow))
            .check(matches(ViewMatchers.isDisplayed()))

        do {
            nextArrow.perform(ViewActions.click())
        } while (nextArrowIsDisplayed())
    }

    @Test
    fun testDirectionsButtonByTransitMode() {

        val device = UiDevice.getInstance(getInstrumentation())

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Click directions button
        device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_directions_button"))
            .click()

        Thread.sleep(5000) //allow NavigationFragment to load

        onView(ViewMatchers.withId(R.id.radio_transport_mode_transit))
            .check(matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Verify start navigation button is displayed & click it
        onView(ViewMatchers.withId(R.id.startNavigationButton))
            .check(matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

        //Click close instructions button button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonMinimizeInstructions"))
            .click()

        //Click Resume Navigation button
        device.findObject(By.res("com.droidhats.campuscompass:id/buttonResumeNavigation"))
            .click()

        val nextArrow = onView(ViewMatchers.withId(R.id.nextArrow))
            .check(matches(ViewMatchers.isDisplayed()))

        do {
            nextArrow.perform(ViewActions.click())
        } while (nextArrowIsDisplayed())

    }

    private fun nextArrowIsDisplayed(): Boolean {
        return try {
            onView(ViewMatchers.withId(R.id.nextArrow))
                .check(matches(ViewMatchers.isDisplayed()))
            true
        } catch (e: AssertionFailedError) {
            false
        }
    }
}