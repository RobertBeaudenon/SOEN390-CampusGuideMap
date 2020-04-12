package com.droidhats.campuscompass.views

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings.Panel
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SplashFragmentTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE"
        )

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        //needs to be here to see which fragment is being opened
        navController.setGraph(R.navigation.navigation)
    }

    //Tests if all images appear on view
    @Test
    fun test_isFragmentImagesInView() {

        //tests if splash_fragment is in view
        assertEquals(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //sees if the images display
        onView(withId(R.id.splash_screen)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_logo)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_title)).check(matches(isDisplayed()))
        onView(withId(R.id.cc_university)).check(matches(isDisplayed()))
    }

    //Tests possible navigation paths from splash_fragment
    @Test
    fun test_isNavigationCorrect() {

        //tests if splash_fragment is in view
        assertEquals(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //tests if the side menu is visible
        onView(withId(R.id.nav_menu)).check(doesNotExist())

        //tests if the back button will navigate away from splash_fragment
        navController.navigateUp()
        assertNotSame(navController.currentDestination?.id,
            R.id.splash_fragment
        )

        //test that the backstack is empty
        assert(navController.backStack.isEmpty())

        //tests if there is navigation to the map_fragment
        navController.navigate(R.id.action_splashFragment_to_mapsActivity)

        assertEquals(navController.currentDestination?.id,
            R.id.map_fragment
        )
    }

    //Tests the connectivity of the device to Wi-Fi in order to trigger the snackbar in all scenarios.
    @Test
    fun test_networkConnectivity(){
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Trigger settings panel is only for Android 10 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            val panelIntent = Intent(Panel.ACTION_WIFI)
            activityRule.activity.startActivityForResult(panelIntent, 545)
            if(device.findObject(UiSelector().textContains("ON")).exists() && device.findObject(UiSelector().textContains("ON")).className == "android.widget.Switch"){
                device.findObject(UiSelector().textContains("ON")).click()
                Thread.sleep(2000) //Wait 2 seconds for wi-fi to turn off and snack bar to disappear
                device.findObject(UiSelector().textContains("OFF")).click()
                Thread.sleep(10000) //Allow 10 seconds for device to automatically reconnect to Wi-Fi
                device.findObject(UiSelector().textContains("Done")).click()
            }else{
                device.findObject(UiSelector().textContains("OFF")).click()
                Thread.sleep(10000) //Wait 10 seconds for wi-fi to turn on and snack bar to appear
                device.findObject(UiSelector().textContains("ON")).click()
                Thread.sleep(2000)
                device.findObject(UiSelector().textContains("OFF")).click() //Leave Wi-Fi ON
                device.findObject(UiSelector().textContains("Done")).click()
            }
        } else{ // Turn Wi-Fi ON and OFF for devices on Android versions below 10
            // This method is deprecated for Android 10 and below
            val wifiManager =
                activityRule.activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if(wifiManager.isWifiEnabled){
                wifiManager.isWifiEnabled = false
                Thread.sleep(2000)
                wifiManager.isWifiEnabled = true
                Thread.sleep(10000) //Allow 10 seconds for device to connect to Wi-Fi
            }else{ //Wi-Fi is initially off
                wifiManager.isWifiEnabled = true
                Thread.sleep(10000) //Allow 10 seconds for device to connect to Wi-Fi
                wifiManager.isWifiEnabled = false
                Thread.sleep(2000)
                wifiManager.isWifiEnabled = true
            }
        }
    }
}