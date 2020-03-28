package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationDrawerTest {

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
        Thread.sleep(3000)
    }

    @Test
    fun test_isNavigationCorrect() {

        //Opening side menu by clicking on menu  button
        onView(withId(R.id.mt_nav)).check(matches(isDisplayed())).perform(click())

        //checking if side menu is opened
        onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isOpen()))

        //clicking outside of the menu to check if it closes
        // tap position works with Pixel 2XL emulator and most smartphones
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).click(1050, 200)

        //wait for side menu to close
        Thread.sleep(1000)

        //check if side menu is really closed
        onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isClosed()))
    }

    @Test
    fun test_isSideMenuListElementsInView() {

        //Opening side menu by clicking on menu  button
        onView(withId(R.id.mt_nav)).check(matches(isDisplayed())).perform(click())

        //Checking if logo is displayed
        onView(withId(R.id.nav_logo)).check(matches(isDisplayed()))

        //Checking if title is displayed
        onView(withId(R.id.nav_title)).check(matches(isDisplayed()))

        //Checking if My places menu item is displayed
        onView(withText("My Places")).check(matches(isDisplayed()))

        //Checking if Resources menu item is displayed
        onView(withText("Resources")).check(matches(isDisplayed()))

        //Checking if Explore menu item is displayed
        onView(withText("Explore")).check(matches(isDisplayed()))

        //Checking if Settings menu item is displayed
        onView(withText("Settings")).check(matches(isDisplayed()))

        //Checking if Settings menu item is displayed
        onView(withText("Map")).check(matches(isDisplayed()))
    }

    @Test
    fun test_isMapNavigationCorrect() {

        //Opening side menu by clicking on menu  button
        onView(withId(R.id.mt_nav)).check(matches(isDisplayed())).perform(click())

        //checking if side menu is opened
        onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isOpen()))

        //clicking on Map menu list item
        onView(withText("Map")).perform(click())

        //check if side menu closes
        onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isClosed()))

        //check if Map Fragment is in view
        onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
    }
}