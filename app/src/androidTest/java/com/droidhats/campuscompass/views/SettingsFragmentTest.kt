package com.droidhats.campuscompass.views

import android.view.View
import android.view.ViewGroup
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SettingsFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)

        //Ensuring the app starts with splash_fragment
        if (navController.currentDestination?.id == R.id.splash_fragment) {

            //navigating to map_fragment - You can put either action id or destination id.
            //I chose the action id so I can check that it indeed takes you the specified destination id.
            navController.navigate(R.id.action_splashFragment_to_mapsActivity)

            //Checking if action id indeed took you to the correct destination id
            TestCase.assertEquals(
                navController.currentDestination?.id!!,
                R.id.map_fragment
            )

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            Espresso.onView(ViewMatchers.withId(R.id.coordinate_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun test_indoorSettings() {
        //Clicks the navbar
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.mt_nav),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.root),
                        childAtPosition(
                            ViewMatchers.withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        //checks that the Settings option is visible in the nav bar and clicks it
        Espresso.onView(
            Matchers.allOf(
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.nav_view),
                            0
                        )
                    ),
                    6
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        Thread.sleep(2000)

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Restrooms")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Printers")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Fountains")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Study Rooms")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.button_menu)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        //checks that the Map option is visible in the nav bar and clicks it
        Espresso.onView(
            Matchers.allOf(
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.nav_view),
                            0
                        )
                    ),
                    1
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        Thread.sleep(2000)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Click directions button
        device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_floor_map_button"))
            .click()

        Thread.sleep(2000)

        //Clicks the navbar
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.mt_nav),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.root),
                        childAtPosition(
                            ViewMatchers.withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        //checks that the Settings option is visible in the nav bar and clicks it
        Espresso.onView(
            Matchers.allOf(
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.nav_view),
                            0
                        )
                    ),
                    6
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        Thread.sleep(2000)

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Restrooms")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Printers")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Fountains")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withText("Study Rooms")))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.button_menu)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())).perform(click())

        //checks that the Map option is visible in the nav bar and clicks it
        Espresso.onView(
            Matchers.allOf(
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.nav_view),
                            0
                        )
                    ),
                    1
                ),
                ViewMatchers.isDisplayed()
            )
        ).perform(click())
        Thread.sleep(2000)

        //Click Hall building marker to trigger bottom sheet
        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Click directions button
        device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet_floor_map_button"))
            .click()

        Thread.sleep(2000)
    }
}


private fun childAtPosition(
    parentMatcher: Matcher<View>, position: Int
): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
        }
    }
}

