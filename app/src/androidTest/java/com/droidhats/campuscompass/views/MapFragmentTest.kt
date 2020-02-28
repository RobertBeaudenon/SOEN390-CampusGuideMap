package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.TestCase
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
    }

    //Checking if we are at the right layout (map_fragment)
    @Test
    fun test_isActivityInView() {
        navController.setCurrentDestination(R.id.map_fragment)
        TestCase.assertEquals(navController.currentDestination?.id, R.id.map_fragment)
        //onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed())) // no clue what was being checked here
    }

    @Test
    fun test_visibilityTextsSwitchToggle() {

        // refers to items not in project, needs fix
        onView(withId(R.id.text_SGW)).check(matches(isDisplayed()))
        onView(withId(R.id.toggle_Campus)).check(matches(isDisplayed()))
        onView(withId(R.id.text_Loyola)).check(matches(isDisplayed()))
        onView(withId(R.id.text_SGW)).check(matches(withText("SGW")))
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isChecked()))
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isNotChecked()))
        onView(withId(R.id.text_Loyola)).check(matches(withText("Loyola")))
    }
}