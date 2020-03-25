package com.droidhats.campuscompass.views

import android.view.View
import android.view.ViewGroup
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.assertEquals
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private lateinit var navController: TestNavHostController
    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )

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
            assertEquals(navController.currentDestination?.id!!, R.id.map_fragment)

            //Waiting 5 seconds for splash screen to load
            Thread.sleep(5000)

            //Checking if that action id did take you to map_fragment view
            onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
        }
    }


    @Test
    fun test_SearchBar() {
        //Checks if the search bar with text "search" exists
        val searchTextView = onView(
            allOf(
                withId(R.id.mt_placeholder), withText("Search"), isDisplayed()
            )
        )
        searchTextView.check(matches(withText("Search")))

        //Checks if magnifying glass button is displayed
        val magnifyingGlassView = onView(
            allOf(
                withId(R.id.mt_search), isDisplayed()
            )
        )
        magnifyingGlassView.check(matches(isDisplayed()))

        //Checks if Search bar can be clicked
        val containerSearchBar = onView(
            allOf(
                withId(R.id.mapFragSearchBar), isDisplayed()
            )
        )
        containerSearchBar.perform(click())

        //Checks if text "Search" is displayed even after search bar was clicked
        val searchText = onView(
            allOf(
                withId(R.id.search_src_text), withHint("Search"), isDisplayed()
            )
        )
        searchText.check(matches(withHint("Search")))

        //Checks if info text that prompts to enter the text in the text field is displayed
        val infoTextView = onView(
            allOf(
                withId(R.id.search_info), withText("Enter Street, Address, Concordia Classroom..."),
                isDisplayed()
            )
        )
        infoTextView.check(matches(withText("Enter Street, Address, Concordia Classroom...")))

        //Checks if Autocomplete options for text "h" are displayed
        val searchAutoComplete = onView(
            allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        )
        searchAutoComplete.perform(ViewActions.typeText("h"), ViewActions.closeSoftKeyboard())

        Thread.sleep(2000)

        //Checks if room suggestion H-400 is displayed and performs a click on the search card
        val searchCardView = onView(
            allOf(
                withId(R.id.search_suggestions_card_view),
                withChild(
                    allOf(
                        withId(R.id.relative_layout1),
                        withChild(
                            allOf(
                                withId(R.id.search_suggestion),
                                withText("H-400")
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        )

        searchCardView.perform(click())

        //Performs click on the Set Navigation Button
        val setNavImageButton = onView(
            allOf(
                withId(R.id.setNavigationPoint),
                withContentDescription("SetNavigation"),
                hasSibling(
                    allOf(
                        withId(R.id.relative_layout1),
                        withChild(
                            allOf(
                                withId(R.id.search_suggestion),
                                withText("H-400")
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        )

        setNavImageButton.perform(click())

        //Checks if back button is displayed
        val backImageButton = onView(
            allOf(
                withId(R.id.backFromNavigationButton), withContentDescription("Back"),
                isDisplayed()
            )
        )
        backImageButton.check(matches(isDisplayed()))

        //Checks if The "From" field displays text "From"
        val fromText = onView(
            allOf(
                withId(R.id.search_src_text), withHint("From"),
                isDisplayed()
            )
        )
        fromText.check(matches(withHint("From")))

        //Checks that if a Clear button is displayed next to the search field
        val clearButtonImageView = onView(
            allOf(
                withId(R.id.search_close_btn), withContentDescription("Clear query"),
                isDisplayed()
            )
        )
        clearButtonImageView.check(matches(isDisplayed()))

        //Checks if Navigation button is displayed
        val navigateImageButton = onView(
            allOf(
                withId(R.id.startNavigationButton), withContentDescription("Navigate"),
                isDisplayed()
            )
        )
        navigateImageButton.check(matches(isDisplayed()))

        //Checks if current location button is displayed
        val currentLocationButton = onView(
            allOf(
                withId(R.id.myCurrentLocationFAB),
                isDisplayed()
            )
        )
        currentLocationButton.perform(click())

        //Checks if "from" field is replaced by test "Your Location"
        val currentLocationTextField = onView(
            allOf(
                withId(R.id.search_src_text), withText("Your Location"),
                isDisplayed()
            )
        )
        currentLocationTextField.check(matches(withText("Your Location")))

        //Checks if Clear button for secondary (To) searchbar is displayed
        val clearButton1ImageView = onView(
            allOf(
                withId(R.id.secondarySearchBar),
                withChild(
                    allOf(
                        withId(R.id.search_bar),
                        withChild(
                            allOf(
                                withId(R.id.search_edit_frame),
                                withChild(
                                    allOf(
                                        withId(R.id.search_plate),
                                        withChild(
                                            withId(R.id.search_close_btn)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        )
        clearButton1ImageView.check(matches(isDisplayed()))

        //Checks if Clear button for main (from) searchbar is displayed
        val clearButton2ImageView = onView(
            allOf(
                withId(R.id.mainSearchBar),
                withChild(
                    allOf(
                        withId(R.id.search_bar),
                        withChild(
                            allOf(
                                withId(R.id.search_edit_frame),
                                withChild(
                                    allOf(
                                        withId(R.id.search_plate),
                                        withChild(
                                            withId(R.id.search_close_btn)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                isDisplayed()
            )
        )
        clearButton2ImageView.check(matches(isDisplayed()))

    }


    @Test
    fun test_SwitchToggle() {

        //Checking if toggle button is displayed
        onView(withId(R.id.toggleButton)).check(matches(isDisplayed()))

        //Checking when the toggle is clicked, it's indeed checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isChecked()))
        Thread.sleep(2000)

        //Ensuring the text of the toggle just clicked is indeed SWG
        onView(withId(R.id.toggleButton)).check(matches(withText("SGW")))

        //Checking when the toggle is clicked again, it's indeed not checked
        onView(withId(R.id.toggleButton)).perform(click()).check(matches(isNotChecked()))
        Thread.sleep(2000)

        //Ensuring the text of the toggle just clicked is indeed LOY
        onView(withId(R.id.toggleButton)).check(matches(withText("LOY")))
    }

    @Test
    fun test_additionalMenuBar() {

        BottomSheetBehavior.from(activityRule.activity.bottom_sheet).state =
            BottomSheetBehavior.STATE_EXPANDED

        //Setting a delay to allow the bottom sheet to load
        Thread.sleep(1000)

        //Checking if building image is displayed
        onView(withId(R.id.building_image)).check(matches(isDisplayed()))

        //Checking if separator bar is displayed
        onView(withId(R.id.separator_bar)).check(matches(isDisplayed()))

        //Checking if building name is displayed
        onView(withId(R.id.bottom_sheet_building_name)).check(matches(isDisplayed()))

        //Checking if building address is displayed
        onView(withId(R.id.bottom_sheet_building_address)).check(matches(isDisplayed()))

        //Checking if opening hours are displayed
        onView(withId(R.id.bottom_sheet_open_hours)).check(matches(isDisplayed()))

        //Checking if services are displayed
        onView(withId(R.id.bottom_sheet_services)).check(matches(isDisplayed()))

        //Checking if departments are displayed
        onView(withId(R.id.bottom_sheet_departments)).check(matches(isDisplayed()))

        //Checking if the direction button is displayed and named Directions
        onView(withId(R.id.bottom_sheet_directions_button)).check(matches(isDisplayed()))
            .check(matches(withText("Directions")))
    }

}