package com.droidhats.campuscompass.views


import android.view.View
import android.view.ViewGroup
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ShuttleBusTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
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

        //Ensuring the app starts with splash_fragment
        if (navController.currentDestination?.id == R.id.splash_fragment) {

            //navigating to map_fragment - You can put either action id or destination id.
            //I chose the action id so I can check that it indeed takes you the specified destination id.
            navController.navigate(R.id.action_splashFragment_to_mapsActivity)

            //Checking if action id indeed took you to the correct destination id
            Assert.assertEquals(navController.currentDestination?.id!!,
                R.id.map_fragment
            )

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            onView(ViewMatchers.withId(R.id.coordinate_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
    @Test
    fun testShuttleBus() {
        //Clicks the navbar
        onView(
            allOf(
                withId(R.id.mt_nav),
                childAtPosition(
                    allOf(
                        withId(R.id.root),
                        childAtPosition(
                            withId(R.id.mt_container),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed())).perform(click())
        //checks that the shuttle bus option is visible in the nav bar and clicks it
        onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    5
                ),
                isDisplayed())).perform(click())
        Thread.sleep(2000)

        //Checks whether option card with content description "SGW TO LOY" is visible
        onView(
            allOf(
                withContentDescription("SGW TO LOY"),
                isDisplayed() )).check(matches(isDisplayed()))

        //Checks whether option card with content description "LOY TO SGW" is visible
        onView(
            allOf(
                withContentDescription("LOY TO SGW"),
                isDisplayed())).check(matches(isDisplayed()))

        //Checks whether text "SGW TO LOY" is visible
        onView(
            allOf(
                withText("SGW TO LOY"),
                isDisplayed())).check(matches(withText("SGW TO LOY")))

        //Checks whether text "LOY TO SGW" is visible
        onView(
            allOf(
                withText("LOY TO SGW"),
                isDisplayed())).check(matches(withText("LOY TO SGW")))

        //Checks that there is the message showing next departures
        onView(
            allOf(
                withId(R.id.nextShuttleDeparture), withText("Next Shuttle Departure: -"),
                isDisplayed())).check(matches(withText("Next Shuttle Departure: -")))

        //Checks that start shuttle trip button is displayed
        onView(
            allOf(
                withId(R.id.navigateWithShuttle),
                isDisplayed())).check(matches(isDisplayed()))

        //Checks that the table with the Shuttle times is populated with at least 26 visible entries
        onView(
            allOf(
                withId(R.id.shuttleTimesTable),
                hasMinimumChildCount(26),
                isDisplayed())).check(matches(isDisplayed()))

        //Perform click on "LOY TO SGW" option
        onView(
            allOf(
                withContentDescription("LOY TO SGW"),
                isDisplayed())).perform(click())

        //Perform click on "SGW TO LOY " option
        onView(
            allOf(
                withContentDescription("SGW TO LOY"),
                isDisplayed())).perform(click())

        //Checks that a click on the Start Shuttle Trip button is displayed
        onView(
            allOf(
                withId(R.id.navigateWithShuttle), withText("Start Shuttle Trip"),
                isDisplayed())).perform(click())

        //Click to view LOY campus view
        onView(
            allOf(
                withId(R.id.toggleButton), withText("SGW"),
                isDisplayed())).perform(click())

        onView(
            allOf(
                withId(R.id.toggleButton), withText("LOY"),
                isDisplayed())).check(matches(isDisplayed()))
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
}
