package com.droidhats.campuscompass.roomdb

import android.os.Build
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert
import org.junit.Before

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ShuttleBusDBTest {

    private val shuttlebusDB: ShuttleBusDB = ShuttleBusDB.getInstance(RuntimeEnvironment.application)
    private lateinit var shuttleBusDAO: ShuttleBusDAO
    private lateinit var shuttleBusLoyolaEntity:ShuttleBusLoyolaEntity
    private lateinit var shuttleBusSGWEntity:ShuttleBusSGWEntity

    @Before
    fun setup() {

        shuttleBusLoyolaEntity = ShuttleBusLoyolaEntity(999)
        shuttleBusLoyolaEntity.shuttle_day = "F"
        shuttleBusLoyolaEntity.shuttle_time = "11:30"

        shuttleBusSGWEntity = ShuttleBusSGWEntity(998)
        shuttleBusSGWEntity.shuttle_day = "MTWT"
        shuttleBusSGWEntity.shuttle_time = "13:30"
    }

    @Test
    fun testSingleton() {
        val testDB: ShuttleBusDB= ShuttleBusDB.getInstance(RuntimeEnvironment.application)

        Assert.assertEquals(
            testDB.toString(),
            shuttlebusDB.toString()
        )
    }

    @Test
    fun insertTimings() {
        val testDB: ShuttleBusDB = ShuttleBusDB.getInstance(RuntimeEnvironment.application)

        shuttleBusDAO = testDB.shuttleBusDAO()
        shuttleBusDAO.saveLoyolaShuttleTime(shuttleBusLoyolaEntity)
        shuttleBusDAO.saveSGWShuttleTime(shuttleBusSGWEntity)

        val sgwCount = 66
        val loyolaCount = 65

        Assert.assertEquals(sgwCount, shuttleBusDAO.getSGWShuttleTimeCount())
        Assert.assertEquals(loyolaCount, shuttleBusDAO.getLoyolaShuttleTimeCount())
    }
}