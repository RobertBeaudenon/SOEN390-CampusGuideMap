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
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun testDirectionButton() {
        val device = UiDevice.getInstance(getInstrumentation())

        Thread.sleep(2000) //Allow downtown map to fully load

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))

        //Expand bottom sheet
        bottomSheet.swipe(Direction.UP, 1.0f)

//TODO: Fix this in the upcoming PR since the button has been moved
/*

//Click directions button
device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_directions_button")).click()

Thread.sleep(2000) //allow NavigationFragment to load

//Verify start navigation button is displayed & click it
onView(ViewMatchers.withId(R.id.startNavigationButton))
    .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())
*/
//TODO: Fix this as this is no longer in existence (It has been removed).
/*

//Verify Instructions button is displayed & click it
onView(ViewMatchers.withId(R.id.buttonInstructions))
    .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(ViewActions.click())

onView(ViewMatchers.withId(R.id.instructionsStepsID))
    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
*/

//Verify close instructions buttons is displayed & click it
        onView(ViewMatchers.withId(R.id.buttonCloseInstructions))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}