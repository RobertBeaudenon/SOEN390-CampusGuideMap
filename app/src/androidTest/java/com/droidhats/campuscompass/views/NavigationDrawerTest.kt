package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.uiautomator.UiDevice
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationDrawerTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

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
        onView(withId(R.id.drawer_layout)).check(ViewAssertions.matches(DrawerMatchers.isOpen()));

        //clicking outside of the menu to check if it closes
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).click(1200, 200)
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
    }
}