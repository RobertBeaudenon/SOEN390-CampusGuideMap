package com.droidhats.campuscompass.views

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Direction
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)

class MarkerTest {
    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
    private val twoSeconds = 2000
    private val fiveSeconds = 5000

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
        waitFor(fiveSeconds)
    }

    @Test
    fun testBuildingMarkers(){
        val device = UiDevice.getInstance(getInstrumentation())
        val toggleButton = device.findObject(By.res("com.droidhats.campuscompass:id/toggleButton"))
        val googleMap = device.findObject(UiSelector().descriptionContains("Google Map"))

        val hMarker = device.findObject(UiSelector().descriptionContains("Henry F. Hall Building. "))
        hMarker.click()

        //Retrieve bottom sheet once it is in the view
        val bottomSheet = device.findObject(By.res("com.droidhats.campuscompass:id/bottom_sheet"))
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val lbMarker = device.findObject(UiSelector().descriptionContains("Pavillion J.W. McConnell Building. "))
        lbMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val vaMarker = device.findObject(UiSelector().descriptionContains("Visual Arts Building. "))
        vaMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val gsMarker = device.findObject(UiSelector().descriptionContains("GS Building. "))
        gsMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val lsMarker = device.findObject(UiSelector().descriptionContains("Learning Square. "))
        lsMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on GS marker
        googleMap.pinchOut(20, 50)

        val gmMarker = device.findObject(UiSelector().descriptionContains("Guy-De Maisonneuve Building. "))
        gmMarker.clickTopLeft()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        googleMap.pinchIn(40, 100)

        val evMarker = device.findObject(UiSelector().descriptionContains("EV Building. "))
        evMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val jmMarker = device.findObject(UiSelector().descriptionContains("John Molson School of Business. "))
        jmMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val fbMarker = device.findObject(UiSelector().descriptionContains("Faubourg Building. "))
        fbMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val gnMarker = device.findObject(UiSelector().descriptionContains("Grey Nuns Building. "))
        gnMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on FG marker
        googleMap.pinchOut(20, 50)

        val fgMarker = device.findObject(UiSelector().descriptionContains("Faubourg Saint-Catherine Building. "))
        fgMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        waitFor(twoSeconds)

        // Switch to Loyola campus
        toggleButton.click()

        waitFor(twoSeconds)

        val jrMarker = device.findObject(UiSelector().descriptionContains("Jesuit Residence. "))
        jrMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val spMarker = device.findObject(UiSelector().descriptionContains("Richard J. Renaud Science Complex. "))
        spMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val cjMarker = device.findObject(UiSelector().descriptionContains("Communication Studies and Journalism Building. "))
        cjMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val pcMarker = device.findObject(UiSelector().descriptionContains("PERFORM Centre. "))
        pcMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val doMarker = device.findObject(UiSelector().descriptionContains("Stinger Dome. "))
        doMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on the rest of these markers
        googleMap.pinchOut(20, 50)

        val psMarker = device.findObject(UiSelector().descriptionContains("Physical Services Building. "))
        psMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val veMarker = device.findObject(UiSelector().descriptionContains("Vanier Extension."))
        veMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val pyMarker = device.findObject(UiSelector().descriptionContains("Psychology Building. "))
        pyMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val fcMarker = device.findObject(UiSelector().descriptionContains("F.C. Smith Building. "))
        fcMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val rfMarker = device.findObject(UiSelector().descriptionContains("Loyola Jesuit and Conference Centre. "))
        rfMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val ccMarker = device.findObject(UiSelector().descriptionContains("Central Building. "))
        ccMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val adMarker = device.findObject(UiSelector().descriptionContains("Administration Building. "))
        adMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val ptMarker = device.findObject(UiSelector().descriptionContains("Oscar Peterson Concert Hall. "))
        ptMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        val vlMarker = device.findObject(UiSelector().descriptionContains("Vanier Library Building. "))
        vlMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)

        //Pinch closer to ensure click precision on SC marker
        googleMap.pinchOut(20, 50)

        val scMarker = device.findObject(UiSelector().descriptionContains("Student Centre. "))
        scMarker.click()
        bottomSheet.swipe(Direction.DOWN, 1.0f)
    }

    private fun waitFor(duration: Int) = Thread.sleep(duration.toLong())
}