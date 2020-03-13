package com.droidhats.campuscompass.viewmodels

import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.ClassRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@RunWith(JUnit4::class)
class SplashViewModelTest {

    // Necessary for instantiating view model
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    // Necessary for instantiating view model
    companion object {
        @ClassRule
        @JvmField
        val schedulers =
            RxImmediateSchedulerRule()
    }

    private lateinit var splashViewModel: SplashViewModel


    @Before
    fun setUp() {
        splashViewModel = SplashViewModel()
    }

    // Checks the status of the initializations to make sure they return true
    @Test
    fun properlyInitializes() {
        splashViewModel.init()
        val initResult: Boolean? = splashViewModel.initCode.value
        assert(initResult!!)
    }
}