package com.droidhats.campuscompass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.droidhats.campuscompass.views.CalendarFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        if(!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
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

        when (requestCode) {
            READ_CALENDAR_PERMISSION_REQUEST_CODE  -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //If the user allowed the READ_CALENDAR permission, refresh the calendar fragment
                    //Note: This assumes the permission request was launched from CalendarFragment !
                      val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                      val calendarFragment  = navHostFragment?.childFragmentManager!!.fragments[0] as CalendarFragment
                      calendarFragment.refresh()
                }
                return
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                //TODO: Location Request Callback
                return
            }
        }
    }
}