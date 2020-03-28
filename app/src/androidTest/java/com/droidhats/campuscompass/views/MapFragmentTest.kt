package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.assertEquals
import kotlinx.android.synthetic.main.bottom_sheet_layout.bottom_sheet
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION"
        )
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
        onView(
            allOf(
                withId(R.id.mt_placeholder), withText("Search"), isDisplayed()
            )
        ).check(matches(withText("Search")))

        //Checks if magnifying glass button is displayed
        onView(
            allOf(
                withId(R.id.mt_search), isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if Search bar can be clicked
        onView(
            allOf(
                withId(R.id.mapFragSearchBar), isDisplayed()
            )
        ).perform(click())

        //Checks if text "Search" is displayed even after search bar was clicked
        onView(
            allOf(
                withId(R.id.search_src_text), withHint("Search"), isDisplayed()
            )
        ).check(matches(withHint("Search")))

        //Checks if info text that prompts to enter the text in the text field is displayed
        onView(
            allOf(
                withId(R.id.search_info), withText("Enter Street, Address, Concordia Classroom…"),
                isDisplayed()
            )
        ).check(matches(withText("Enter Street, Address, Concordia Classroom…")))

        //Checks if Autocomplete options for text "h" are displayed
        onView(
            allOf(
                withId(R.id.search_src_text), isDisplayed()
            )
        ).perform(ViewActions.typeText("h"), ViewActions.closeSoftKeyboard())

        Thread.sleep(2000)

        //Checks if room suggestion H-400 is displayed and performs a click on the search card
        onView(
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
        ).perform(click())

        //Performs click on the Set Navigation Button
        onView(
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
        ).perform(click())

        //Checks if back button is displayed
        onView(
            allOf(
                withId(R.id.backFromNavigationButton), withContentDescription("Back"),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if The "From" field displays text "From"
        onView(
            allOf(
                withId(R.id.search_src_text), withHint("From"),
                isDisplayed()
            )
        ).check(matches(withHint("From")))

        //Checks that if a Clear button is displayed next to the search field
        onView(
            allOf(
                withId(R.id.search_close_btn), withContentDescription("Clear query"),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if Navigation button is displayed
        onView(
            allOf(
                withId(R.id.startNavigationButton), withContentDescription("Navigate"),
                isDisplayed()
            )
        ).check(matches(isDisplayed()))

        //Checks if current location button is displayed
        onView(
            allOf(
                withId(R.id.myCurrentLocationFAB),
                isDisplayed()
            )
        ).perform(click())

        //Checks if "from" field is replaced by test "Your Location"
        onView(
            allOf(
                withId(R.id.search_src_text), withText("Your Location"),
                isDisplayed()
            )
        ).check(matches(withText("Your Location")))

        //Checks if Clear button for secondary (To) searchbar is displayed
        onView(
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
        ).check(matches(isDisplayed()))

        //Checks if Clear button for main (from) searchbar is displayed
        onView(
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
        ).check(matches(isDisplayed()))

        //Checking if driving option is displayed
        onView(withId(R.id.radio_transport_mode_driving)).check(matches(isDisplayed()))

        //Checking when the driving option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_driving)).perform(click()).check(matches(isChecked()))

        //Checking if transit option is displayed
        onView(withId(R.id.radio_transport_mode_transit)).check(matches(isDisplayed()))

        //Checking when the transit option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_transit)).perform(click()).check(matches(isChecked()))

        //Checking if walking option is displayed
        onView(withId(R.id.radio_transport_mode_walking)).check(matches(isDisplayed()))

        //Checking when the walking option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_walking)).perform(click()).check(matches(isChecked()))

        //Checking if bicycle option is displayed
        onView(withId(R.id.radio_transport_mode_bicycle)).check(matches(isDisplayed()))

        //Checking when the bicycle option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_bicycle)).perform(click()).check(matches(isChecked()))

        //Checking if shuttle option is displayed
        onView(withId(R.id.radio_transport_mode_shuttle)).check(matches(isDisplayed()))

        //Checking when the shuttle option is clicked, it's indeed selected
        onView(withId(R.id.radio_transport_mode_shuttle)).perform(click()).check(matches(isChecked()))
    }

    @Test
    fun test_PlaceInfoCard() {

        //function that searches for restaurant Ganadara
        fun searchOutdoorLocation() {

            // check if search bar exists and clicks on it
            onView(withId(R.id.mapFragSearchBar)).check(matches(isDisplayed())).perform(click())

            //Checking if action id indeed took you to the correct fragment
            onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))

            //search for restaurant Ganadara
            onView(
                allOf(
                    withId(R.id.search_src_text), isDisplayed()
                )
            ).perform(ViewActions.typeText("ganadara"), ViewActions.closeSoftKeyboard())

            //allow suggestions to load
            Thread.sleep(2000)

            //click on the Ganadara restaurant suggestion from list
            onView(allOf(withId(R.id.search_suggestion), withText("Ganadara"))).perform(click())
        }

        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //check that place info card is displayed
        onView(withId(R.id.place_card)).check(matches(isDisplayed()))

        //check that location name is displayed
        onView(withId(R.id.place_card_name)).check(matches(isDisplayed()))

        //check that location address is displayed
        onView(withId(R.id.place_card_category)).check(matches(isDisplayed()))

        //check that favorites button is displayed and click it
        onView(withId(R.id.place_card_favorites_button)).check(matches(isDisplayed())).perform(click())

        //Ensuring the text of the favorites button is Save
        onView(withId(R.id.place_card_favorites_button)).check(matches(withText("Save")))

        //check that close button is displayed
        onView(withId(R.id.place_card_close_button)).check(matches(isDisplayed()))

        //check that directions button is displayed
        onView(withId(R.id.place_card_directions_button)).check(matches(isDisplayed()))

        //Ensuring the text of the directions button is Directions
        onView(withId(R.id.place_card_directions_button)).check(matches(withText("Directions")))

        //click on close button
        onView(withId(R.id.place_card_close_button)).perform(click())

        //allow place info card to close
        Thread.sleep(1000)

        //check if place info card really closed
        onView(withId(R.id.place_card)).check(matches(not(isDisplayed())))

        //repeat search for restaurant Ganadara in order to test navigation from the place info card
        searchOutdoorLocation()

        //allow map to readjust view
        Thread.sleep(1500)

        //click on directions button
        onView(withId(R.id.place_card_directions_button)).perform(click())

        //allow search page to load
        //Thread.sleep(1500)

        //check if we are taken to search page
        onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))

        //check if location name is set as the destination
        onView(allOf(withId(R.id.search_src_text), withText("Ganadara"))).check(matches(isDisplayed()))
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