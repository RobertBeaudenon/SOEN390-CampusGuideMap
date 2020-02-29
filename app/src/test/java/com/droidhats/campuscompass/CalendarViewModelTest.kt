package com.droidhats.campuscompass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.droidhats.campuscompass.viewmodels.CalendarViewModel

import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.ClassRule
import java.util.*

@RunWith(JUnit4::class)
class CalendarViewModelTest {

    // Necessary for instantiating view model
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    // Necessary for instantiating view model
    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    private lateinit var calendarViewModel: CalendarViewModel

    @Before
    fun setUp() {
       // calendarViewModel = CalendarViewModel()
    }


}