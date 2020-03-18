package com.droidhats.campuscompass.repositories

import android.os.Build
import androidx.lifecycle.LiveData
import com.droidhats.campuscompass.roomdb.ShuttleBus_Loyola_Entity
import com.droidhats.campuscompass.roomdb.ShuttleBus_SGW_Entity
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
        val ShuttleBus_SGW_times: LiveData<List<ShuttleBus_SGW_Entity>> = navigationRepository.getSGWShuttleTime()
        val ShuttleBus_loyola_times: LiveData<List<ShuttleBus_Loyola_Entity>> = navigationRepository.getLoyolaShuttleTime()

        Assert.assertEquals(null, ShuttleBus_SGW_times.value)
        Assert.assertEquals(null, ShuttleBus_loyola_times.value)
    }


}