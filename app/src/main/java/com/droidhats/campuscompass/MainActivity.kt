package com.droidhats.campuscompass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.droidhats.campuscompass.views.CalendarFragment
import com.droidhats.campuscompass.views.SplashFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var navController : NavController

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navView.setNavigationItemSelectedListener(this)
    }

    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_DENIED
        ) {return false}
        return true
    }

    fun checkCalendarPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR)
            == PackageManager.PERMISSION_DENIED
        ) {return false}
        return true
    }

    fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun requestCalendarPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_CALENDAR),
            READ_CALENDAR_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        when (requestCode) {
            READ_CALENDAR_PERMISSION_REQUEST_CODE  -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //If the user allowed the READ_CALENDAR permission, refresh the calendar fragment
                    //Note: This assumes the permission request was launched from CalendarFragment !
                      val calendarFragment  = navHostFragment?.childFragmentManager!!.fragments[0] as CalendarFragment
                        calendarFragment.showDialog()
                        calendarFragment.refresh()
                }
                return
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                //If the user granted, denied, or cancelled the location permission, load the map
                //since splash isn't initializing components yet, it will perform normal navigation to MapFragment
                val splashFragment  = navHostFragment?.childFragmentManager!!.fragments[0] as SplashFragment
                splashFragment.navigateToMapFragment()
                return
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.my_places_fragment -> {
                navController.popBackStack(R.id.map_fragment, false)
                navController.navigate(R.id.my_places_fragment)
            }
            R.id.nav_schedule -> {
                navController.popBackStack(R.id.map_fragment, false)
                navController.navigate(R.id.nav_schedule)
            }
            R.id.nav_explore -> {
                navController.popBackStack(R.id.map_fragment, false)
                navController.navigate(R.id.nav_explore)
            }
            R.id.nav_shuttle -> {
                navController.popBackStack(R.id.map_fragment, false)
                navController.navigate(R.id.nav_shuttle)
            }
            R.id.map_fragment -> navController.popBackStack(R.id.map_fragment, false)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}