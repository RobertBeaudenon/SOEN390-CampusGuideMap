package com.droidhats.campuscompass.views


import android.view.View
import android.view.ViewGroup
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CalendarFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CALENDAR"
        )
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
            TestCase.assertEquals(navController.currentDestination?.id!!,
                R.id.map_fragment
            )

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun calendarFragmentTest() {
        //checks if the hamburger menu icon exists and performs a click
        val appCompatImageView = onView(
            allOf(
                withId(R.id.mt_nav),
                isDisplayed()
            )
        )
        appCompatImageView.perform(click())

        //checks if the navigation view sidebar exists and performs a click on it
        val navigationMenuItemView = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView.perform(click())

        //Checks if the select calendar button exists
        val button = onView(
            allOf(
                withId(R.id.select_calendar_button),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))

       //Checks if there exists a button with text select calendars and performs a click on it
        val appCompatButton = onView(
            allOf(
                withId(R.id.select_calendar_button), withText("select calendars"),
                isDisplayed()
            )
        )
        appCompatButton.perform(click())

       //Checks if there exists "select your Calendars" on the calendar selection widget
        val textView3 = onView(
            allOf(
                IsInstanceOf.instanceOf(android.widget.TextView::class.java),
                withText("Select Your Calendars"),
                isDisplayed()
            )
        )
        textView3.check(matches(withText("Select Your Calendars")))

        //checks if a calendar color option (text1) exists and performs a click on it
        val checkedTextView = onView(
            allOf(
                withId(android.R.id.text1),
                childAtPosition(
                    allOf(
                        IsInstanceOf.instanceOf(android.widget.ListView::class.java),
                        childAtPosition(
                            IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        checkedTextView.check(matches(isDisplayed()))
        checkedTextView.perform(click())

        //checks if there exists a button with text "okay" within the calendar selection widget an performs a click on it
        val appCompatButton2 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        appCompatButton2.perform(scrollTo(), click())

        //Performs a swipe down action to refresh the swipe container of the Schedules to show events
        val refreshScrollView = onView(
            allOf(
                withId(R.id.swipe_container),
                isDisplayed()
            )
        )
        refreshScrollView.perform(swipeDown())

        //Checks if an event card exists (called calendar card)
        val linearLayout = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.calendar_card_view),
                        childAtPosition(
                            withId(R.id.calendar_recycler_view),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        linearLayout.check(matches(isDisplayed()))

        //checks that a button with content description Navigate exists
        val imageButton = onView(
            allOf(
                withId(R.id.navigateFromEvent), withContentDescription("Navigate"),
                childAtPosition(
                    allOf(
                        withId(R.id.calendar_card_view),
                        childAtPosition(
                            withId(R.id.calendar_recycler_view),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        imageButton.check(matches(isDisplayed()))

        //Performs a click on the event (calendar) card to begin the navigation
        val cardView = onView(
            allOf(
                withId(R.id.calendar_card_view),
                childAtPosition(
                    allOf(
                        withId(R.id.calendar_recycler_view),
                        childAtPosition(
                            withId(R.id.swipe_container),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        cardView.perform(click())
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
