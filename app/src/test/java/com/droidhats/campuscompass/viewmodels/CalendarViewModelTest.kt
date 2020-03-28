package com.droidhats.campuscompass.viewmodels

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.models.CalendarEvent
import org.junit.Before
import kotlin.collections.ArrayList
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CalendarViewModelTest {

    private val calendarViewModel: CalendarViewModel = CalendarViewModel(RuntimeEnvironment.application)
    private var userCalendars = mutableMapOf<String, Calendar>()
    private val colorArray =  CalendarViewModel.GOOGLE_CALENDAR_COLOR_MAP.keys.toTypedArray()


    @Before // Creating calendars with events
    fun initCalendars() {
        var id = 0
        val dummy = "dummy"

        for(colorInt in CalendarViewModel.GOOGLE_CALENDAR_COLOR_MAP) {

            val eventList: ArrayList<CalendarEvent> = arrayListOf()

            val calendar= Calendar(
                (id++).toString(),
                dummy,
                dummy,
                dummy,
                dummy,
                colorInt.value,
                colorInt.key == "Any"
            )
            eventList.add(
                CalendarEvent(
                    dummy,
                    dummy,
                    dummy,
                    dummy,
                    dummy,
                    dummy,
                    colorInt.value
                )
            )
            calendar.events = eventList
            userCalendars[colorInt.value] = calendar
        }
        calendarViewModel.userCalendars.value = userCalendars
    }

    @Before // Randomizing user selections
    fun initUserSelections()
    {
        for (selection in calendarViewModel.selectedColors.indices) {
            val selected = Random.nextBoolean()
            println("Setting ${colorArray[selection]} selection to $selected")
            calendarViewModel.selectedColors[selection] = selected
        }

    }

    // Checks the filtered list shows the correct(selected) colors
    @Test
    fun testEvents() {

        val selectedColors = arrayListOf<String>() //the color of the selected calendars
        calendarViewModel.selectCalendars() //filter calendars

        for (i in calendarViewModel.selectedCalendars.value!!) {
            selectedColors.add(i.color!!)
        }

        for (selection in calendarViewModel.selectedColors.indices) {

            //If the color has been selected, check that it has been filtered properly by the selectCalendars() method
            if (calendarViewModel.selectedColors[selection])
                assert(selectedColors.contains(CalendarViewModel.GOOGLE_CALENDAR_COLOR_MAP[colorArray[selection]]))
            else
                assert(!selectedColors.contains(CalendarViewModel.GOOGLE_CALENDAR_COLOR_MAP[colorArray[selection]]))
        }
    }
}

