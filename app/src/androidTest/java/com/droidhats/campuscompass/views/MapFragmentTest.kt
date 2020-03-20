package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import junit.framework.Assert.assertEquals
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)

        //Ensuring the app starts with splash_fragment
        if (navController.currentDestination?.id == R.id.splash_fragment) {

            //navigating to map_fragment - You can put either action id or destination id. I chose the action id so I can check that it indeed takes you the specified destination id.
            navController.navigate(R.id.action_splashFragment_to_mapsActivity);

            //Checking if action id indeed took you to the correct destination id
            assertEquals(navController.currentDestination?.id!!, R.id.map_fragment)

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000);

            //Checking if that action id did take you to map_fragment view
            onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
        }
    }

    //Checking if the search bar exists
    @Test
    fun test_SearchBar() {

        //Checking if searchBar exists on map_fragment
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()))
    }


    @Test
    fun test_SwitchToggle() {

        //Checking if toggle button is displayed
        onView(withId(R.id.toggleButton)).check(matches(isDisplayed()))

        //Checking when the toggle is clicked, it's indeed checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isChecked()))

        //Ensuring the text of the toggle just clicked is indeed SWG
        onView(withId(R.id.toggleButton)).check(matches(withText("SGW")))

        //Checking when the toggle is clicked again, it's indeed not checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isNotChecked()))

        //Ensuring the text of the toggle just clicked is indeed LOY
        onView(withId(R.id.toggleButton)).check(matches(withText("LOY")))
    }

    @Test
    fun test_additionalMenuBar(){
        GlobalScope.launch {
            delay(2000) //to allow bottomsheet to load

            BottomSheetBehavior.from(activityRule.activity.bottom_sheet).state =
                BottomSheetBehavior.STATE_EXPANDED

            //Checking if building image is displayed
            onView(withId(R.id.building_image)).check(matches(isDisplayed()))
            //Checking if separator bar is displayed
            onView(withId(R.id.separator_bar)).check(matches(isDisplayed()))
            //Checking if building name is displayed
            onView(withId(R.id.bottom_sheet_building_name)).check(matches(isDisplayed()))
            //Checking if building address is displayed
            onView(withId(R.id.bottom_sheet_building_address)).check(matches(isDisplayed()))
            //Checking if opening hours are displayed
            onView(withId(R.id.bottom_sheet_open_hours)).check(matches(isDisplayed()))
            //Checking if services are displayed
            onView(withId(R.id.bottom_sheet_services)).check(matches(isDisplayed()))
            //Checking if departments are displayed
            onView(withId(R.id.bottom_sheet_departments)).check(matches(isDisplayed()))
            //Checking if the direction button is displayed
            onView(withId(R.id.bottom_sheet_directions_button)).check(matches(isDisplayed()))
            //Ensuring the text of the direction button is indeed Directions
            onView(withId(R.id.bottom_sheet_directions_button)).check(matches(withText("Directions")))
            // check the color of the button
            // check the image on the button
        }
    }
}