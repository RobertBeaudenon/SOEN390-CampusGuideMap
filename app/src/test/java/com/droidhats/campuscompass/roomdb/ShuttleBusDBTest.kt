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

    private val shuttlebusDB: ShuttleBusDB= ShuttleBusDB.getInstance(RuntimeEnvironment.application)
    private lateinit var shuttleBusDAO: ShuttleBus_DAO
    private lateinit var shuttleBus_Loyola_Entity:ShuttleBus_Loyola_Entity
    private lateinit var shuttleBus_SGW_Entity:ShuttleBus_SGW_Entity

    @Before
    fun setup(){

        shuttleBus_Loyola_Entity = ShuttleBus_Loyola_Entity(999)
        shuttleBus_Loyola_Entity.shuttle_day= "F"
        shuttleBus_Loyola_Entity.shuttle_time= "11:30"

        shuttleBus_SGW_Entity = ShuttleBus_SGW_Entity(998)
        shuttleBus_SGW_Entity.shuttle_day= "MTWT"
        shuttleBus_SGW_Entity.shuttle_time= "13:30"
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
    fun insertTimings(){
        val testDB: ShuttleBusDB= ShuttleBusDB.getInstance(RuntimeEnvironment.application)

        shuttleBusDAO = testDB!!.shuttleBusDAO()
        shuttleBusDAO.saveLoyolaShuttleTime(shuttleBus_Loyola_Entity)
        shuttleBusDAO.saveSGWShuttleTime(shuttleBus_SGW_Entity)

        var sgw_count = 66
        var loyola_count = 65

        Assert.assertEquals(sgw_count, shuttleBusDAO.getSGWShuttleTimeCount())
        Assert.assertEquals(loyola_count, shuttleBusDAO.getLoyolaShuttleTimeCount())
    }
}