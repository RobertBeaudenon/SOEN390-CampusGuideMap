package com.droidhats.campuscompass

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SplashFragmentTest {

    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext())

    //Tests if the current view is splash_fragment
    @Test
    fun test_isFragmentInView() {
        ActivityScenario.launch(MainActivity::class.java)
        navController.setGraph(R.navigation.navigation)
        assertEquals(navController.currentDestination?.id, R.id.splash_fragment)
    }

    //Tests if all images appear on view
    @Test
    fun test_isFragmentImagesInView() {
        ActivityScenario.launch(MainActivity::class.java)
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

    //Tests the navigation from splash_fragment to map_fragment (as opposed to ex. calendar_fragment)
    @Test
    fun test_isRedirectedToMap() {
        ActivityScenario.launch(MainActivity::class.java)
        navController.setGraph(R.navigation.navigation)
        navController.navigate(R.id.action_splashFragment_to_mapsActivity)
        assertEquals(navController.currentDestination?.id, R.id.map_fragment)
    }

    //Tests if splash_fragment stays in view when back button is pressed
    @Test
    fun test_disabledBackButton() {
        ActivityScenario.launch(MainActivity::class.java)
        navController.setGraph(R.navigation.navigation)
        pressBack()
        assertEquals(navController.currentDestination?.id, R.id.splash_fragment)
    }

    //Tests if the side menu is available during the splash screen
    @Test
    fun test_isSideMenuDisabled() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.nav_menu))
            .check(doesNotExist())
    }
}