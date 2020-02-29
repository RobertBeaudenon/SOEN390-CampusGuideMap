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





    }


}