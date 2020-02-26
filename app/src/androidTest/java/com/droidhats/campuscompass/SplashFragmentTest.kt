package com.droidhats.campuscompass

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import junit.framework.TestCase.assertEquals
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SplashFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)

    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext())

    //Tests possible navigation paths from splash_fragment
    @Test
    fun test_isNavigationCorrect() {
        navController.setGraph(R.navigation.navigation)

        //tests if splash_fragment is in view
        assertEquals(navController.currentDestination?.id, R.id.splash_fragment)

        //tests if the side menu is visible
        onView(withId(R.id.nav_menu))
            .check(doesNotExist())

        //tests if the back button will navigate away from splash_fragment
        pressBack()
        assertEquals(navController.currentDestination?.id, R.id.splash_fragment)

        //tests if there is navigation to the map_fragment
        navController.navigate(R.id.action_splashFragment_to_mapsActivity)
        assertEquals(navController.currentDestination?.id, R.id.map_fragment)
    }

    //Tests if all images appear on view
    @Test
    fun test_isFragmentImagesInView() {
        //sees if the images display
        onView(withId(R.id.splash_screen))
            .check(matches(isDisplayed()))
        onView(withId(R.id.cc_logo))
            .check(matches(isDisplayed()))
        onView(withId(R.id.cc_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.cc_university))
            .check(matches(isDisplayed()))
    }
}