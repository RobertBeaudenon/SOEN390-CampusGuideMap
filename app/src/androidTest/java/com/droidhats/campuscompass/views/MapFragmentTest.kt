package com.droidhats.campuscompass.views

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MapFragmentTest {



    //Checking if we are at the right layout (map_fragment)
    @Test
    fun test_isActivityInView() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.coordinate_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun test_visibilityTextsSwitchToggle() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.text_SGW)).check(matches(isDisplayed()));
        onView(withId(R.id.toggle_Campus)).check(matches(isDisplayed()));
        onView(withId(R.id.text_Loyola)).check(matches(isDisplayed()));
        onView(withId(R.id.text_SGW)).check(matches(withText("SGW")));
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.toggle_Campus)).perform(click()).check(matches(isNotChecked()));
        onView(withId(R.id.text_Loyola)).check(matches(withText("Loyola")));
    }
}