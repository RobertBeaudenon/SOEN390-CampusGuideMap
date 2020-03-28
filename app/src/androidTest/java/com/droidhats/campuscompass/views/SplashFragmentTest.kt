package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SplashFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
    }

    //Tests if all images appear on view
    @Test
    fun test_isFragmentImagesInView() {

        //tests if splash_fragment is in view
        assertEquals(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //sees if the images display
        onView(withId(R.id.splash_screen)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_logo)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_title)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_university)).check(matches(isDisplayed()))
    }

    //Tests possible navigation paths from splash_fragment
    @Test
    fun test_isNavigationCorrect() {

        //tests if splash_fragment is in view
        assertEquals(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //tests if the side menu is visible
        onView(withId(R.id.nav_menu)).check(doesNotExist())

        //tests if there is navigation to the map_fragment
        navController.navigate(R.id.action_splashFragment_to_mapsActivity)
        assertEquals(navController.currentDestination?.id,
            R.id.map_fragment
        )

        //tests if the back button will navigate away from splash_fragment
        navController.navigateUp()
        assertNotSame(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //test that the backstack is empty
        assert(navController.backStack.isEmpty())
    }
}