package com.droidhats.campuscompass


import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelProviders
import com.droidhats.campuscompass.viewmodels.CalendarViewModel
import com.droidhats.campuscompass.views.CalendarFragment
import com.droidhats.campuscompass.MainActivity


import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.ClassRule
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.models.CalendarEvent


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CalendarViewModelTest {

    var calendarViewModel:  CalendarViewModel



   //initializing the calendarviewmodel
    init {
       val context: Application = RuntimeEnvironment.application
        calendarViewModel = CalendarViewModel(context)


   }



    // Checks the status of the initializations to make sure they return true
    @Test
    fun testEvents() {
        val colorArray = CalendarViewModel.GOOGLE_CALENDAR_COLOR_MAP.keys.toTypedArray()
        var userCalendars = mutableMapOf<String, Calendar>()



        for(i in colorArray){

            var eventList: ArrayList<CalendarEvent> = arrayListOf()
            if(i=="any") {
                var calendar= Calendar("robert", "makram", "nick", "neeham", "amanda", i, true)
                eventList.add(
                    CalendarEvent( "a",
                        "b",
                        "c",
                        "d",
                        "e",
                        "f",
                        i)
                )
                calendar.events = eventList
                userCalendars[i] = calendar
            }else{


                     var calendar= Calendar("robert", "makram", "nick", "neeham", "amanda", i, false)
                 eventList.add(
                CalendarEvent( "a",
                    "b",
                    "c",
                    "d",
                    "e",
                    "f",
                    i)
                )
                calendar.events = eventList
                userCalendars[i] = calendar
            }
        }




    }


}