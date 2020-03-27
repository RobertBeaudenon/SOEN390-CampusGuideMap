package com.droidhats.campuscompass.viewmodels

import android.os.Build
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.ShuttleBusSGWEntity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.droidhats.campuscompass.roomdb.ShuttleBusLoyolaEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class NavigationViewModelTest {
    
    private val viewmodel: NavigationViewModel = NavigationViewModel(RuntimeEnvironment.application)

    @Test
    fun testInit() {
        val shuttleBusSGWTimes: LiveData<List<ShuttleBusSGWEntity>> = viewmodel.getSGWShuttleTime()
        val shuttleBusLoyolaTimes: LiveData<List<ShuttleBusLoyolaEntity>> = viewmodel.getLoyolaShuttleTime()

        Assert.assertEquals(null, shuttleBusSGWTimes.value)
        Assert.assertEquals(null, shuttleBusLoyolaTimes.value)
    }
}