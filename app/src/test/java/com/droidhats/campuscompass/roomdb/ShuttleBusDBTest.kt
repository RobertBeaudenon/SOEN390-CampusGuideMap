package com.droidhats.campuscompass.roomdb

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ShuttleBusDBTest {
    private val shuttlebusDB: ShuttleBusDB= ShuttleBusDB.getInstance(RuntimeEnvironment.application)

    @Test
    fun testSingleton() {
        val testDB: ShuttleBusDB= ShuttleBusDB.getInstance(RuntimeEnvironment.application)
        
        Assert.assertEquals(
            testDB.toString(),
            shuttlebusDB.toString()
        )
    }
}