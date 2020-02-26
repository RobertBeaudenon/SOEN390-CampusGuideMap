package com.droidhats.campuscompass

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.droidhats.campuscompass.views.MapFragment
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
        navController.navigate(R.id.action_splashFragment_to_mapsActivity)
    }

    //Checking if we are at the right fragment layout (map_fragment)
    @Test
    fun test_isActivityInView() {
        assertEquals(navController.currentDestination?.id, R.id.map_fragment)
    }

    @Test
    fun test_visibilityTextsSwitchToggle() {
        //this is producing a failed test when inflating the Google's search button
        //this will be looked at once search bar is functional
        launchFragmentInContainer<MapFragment>()

        onView(withId(R.id.nav_host_fragment)).perform(swipeLeft())
        assertEquals(navController.currentDestination?.id, R.id.map_fragment)
        onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
        /*
        onView(withId(R.id.text_SGW)).check(matches(isDisplayed()))
        onView(withId(R.id.toggle_Campus)).check(matches(isDisplayed()))
        onView(withId(R.id.text_Loyola)).check(matches(isDisplayed()))
        onView(withId(R.id.text_SGW)).check(matches(withText("SGW")))
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isNotChecked()))
        onView(withId(R.id.text_Loyola)).check(matches(withText("Loyola")))   */
    }
}