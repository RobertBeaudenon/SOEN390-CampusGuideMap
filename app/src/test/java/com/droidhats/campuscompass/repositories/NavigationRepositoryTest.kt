package com.droidhats.campuscompass.repositories

import android.os.Build
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.ShuttleBusLoyolaEntity
import com.droidhats.campuscompass.roomdb.ShuttleBusSGWEntity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class NavigationRepositoryTest {
    private val navigationRepository: NavigationRepository= NavigationRepository(RuntimeEnvironment.application)

    @Test
    fun testInit() {
        val shuttleBusSGWTimes: LiveData<List<ShuttleBusSGWEntity>> = navigationRepository.getSGWShuttleTime()
        val shuttleBusLoyolaTimes: LiveData<List<ShuttleBusLoyolaEntity>> = navigationRepository.getLoyolaShuttleTime()

        Assert.assertEquals(null, shuttleBusSGWTimes.value)
        Assert.assertEquals(null, shuttleBusLoyolaTimes.value)
    }


}