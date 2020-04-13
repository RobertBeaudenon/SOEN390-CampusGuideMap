package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FloorFragmentTest {

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
            ApplicationProvider.getApplicationContext()
        )
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
        Thread.sleep(3000)
    }

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun navigationFromBuildingTest() {

        Thread.sleep(5000)

        device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. ")).click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))

        //Checking if the direction button is displayed and named Directions
        onView(withId(R.id.bottom_sheet_directions_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Directions")))

        //Checking if the direction button is displayed and named Directions
        onView(withId(R.id.bottom_sheet_floor_map_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Indoor Maps")))

        //Expand bottom sheet fully to show buttons
        bottomSheet.swipe(Direction.UP, 1.0f)

        Thread.sleep(1000)

        //click on the indoor maps button
        onView(withId(R.id.bottom_sheet_floor_map_button)).perform(click())

        Thread.sleep(2000)

        //check if FloorFragment is in view
        onView(withId(R.id.frameLayout)).check(
            matches(
                isDisplayed()
            )
        )

        //check if Floor Fragment is in view
        onView(withId(R.id.text_floor))
            .check(matches(isDisplayed()))

        //check if floor picker is in view
        onView(withId(R.id.floorPicker))
            .check(matches(isDisplayed()))

        //swipe the floor picker to change floor map
        onView(withId(R.id.floorPicker)).perform(
            GeneralSwipeAction(
                Swipe.FAST,
                GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER,
                Press.FINGER
            )
        )
    }

    @Test
    fun navigationFromSearch() {

        //let the app load past the splash screen
        Thread.sleep(3000)

        // check if search bar exists and clicks on it
        onView(withId(R.id.mapFragSearchBar)).check(
            matches(
                isDisplayed()
            )
        ).perform(click())

        //Checking if action id indeed took you to the correct fragment
        onView(withId(R.id.search_fragment)).check(
            matches(
                isDisplayed()
            )
        )

        //search a Hall room by starting search with 'h'
        onView(
            Matchers.allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("h"), ViewActions.closeSoftKeyboard())

        //allow suggestions to load
        Thread.sleep(1000)

        //click on the hall-167 suggestion from list
        onView(
            Matchers.allOf(
                withId(R.id.search_suggestion),
                withText("hall-167")
            )
        ).perform(click())

       //check if FloorFragment is in view
        onView(withId(R.id.floormap)).check(
            matches(
                isDisplayed()
            )
        )

        //check if Floor text is in view
        onView(withId(R.id.text_floor))
            .check(matches(isDisplayed()))

        //check if floor picker is in view
        onView(withId(R.id.floorPicker))
            .check(matches(isDisplayed()))

        //swipe the floor picker to change floor map
        onView(withId(R.id.floorPicker)).perform(
            GeneralSwipeAction(
                Swipe.FAST,
                GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER,
                Press.FINGER
            )
        )
    }

    @Test
    fun floorToFloorNavigation() {
        //let the app load past the splash screen
        Thread.sleep(3000)

        // check if search bar exists and clicks on it
        onView(withId(R.id.mapFragSearchBar)).check(
            matches(
                isDisplayed()
            )
        ).perform(click())

        //Checking if action id indeed took you to the correct fragment
        onView(withId(R.id.search_fragment)).check(
            matches(
                isDisplayed()
            )
        )

        //search a Hall 8th floor room by starting search with 'h'
        onView(
            Matchers.allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("hall-8"), ViewActions.closeSoftKeyboard())

        //allow suggestions to load
        Thread.sleep(1000)

        //Performs click on the Set Navigation Button
        onView(
            Matchers.allOf(
                withId(R.id.setNavigationPoint),
                ViewMatchers.withContentDescription("SetNavigation"),
                ViewMatchers.hasSibling(
                    Matchers.allOf(
                        withId(R.id.relative_layout1),
                        ViewMatchers.withChild(
                            Matchers.allOf(
                                withId(R.id.search_suggestion),
                                withText("hall-867")
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        ).perform(click())

        //Checks if Clear button for main (from) searchbar is displayed
        onView(Matchers.allOf(withId(R.id.search_close_btn),hasSibling(withText("Your Current Location")))).check(matches(isDisplayed())).perform(click())

        //search a Hall 9th floor room by starting search with 'hall-9'
        onView(
            Matchers.allOf(
                withId(R.id.search_src_text), withHint("From"), isDisplayed()
            )
        ).perform(ViewActions.typeText("hall-9"), ViewActions.closeSoftKeyboard())

        //allow suggestions to load
        Thread.sleep(1000)

        //click on the hall-167 suggestion from list
        onView(
            Matchers.allOf(
                withId(R.id.search_suggestion),
                withText("hall-927.1")
            )
        ).perform(click())

        //allow suggestions to be inserted into from bar
        Thread.sleep(1000)

        //Performs click on the Set Navigation Button
        onView(withId(R.id.startNavigationButton)).perform(click())

        //click on back button for coverage
        onView(withId(R.id.progressFloor))
            .check(matches(isDisplayed()))

        //allow floor fragment to load
        Thread.sleep(5000)

        //check if Floor Fragment is in view
        onView(withId(R.id.floormap))
            .check(matches(isDisplayed()))

        //check if instructions is in view
        onView(withId(R.id.instructions_text))
            .check(matches(isDisplayed()))

        //click on next button
        onView(withId(R.id.nextArrowFloor))
            .check(matches(isDisplayed())).perform(click())

        Thread.sleep(1000)

        //click on done button
        onView(withId(R.id.doneButtonFloor))
            .check(matches(isDisplayed())).perform(click())
    }
}