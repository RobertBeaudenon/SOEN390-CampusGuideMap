package com.droidhats.campuscompass.viewmodels

import android.os.Build
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.ClassRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.robolectric.annotation.Config

@RunWith(JUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
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