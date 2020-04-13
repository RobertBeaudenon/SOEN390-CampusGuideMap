package com.droidhats.campuscompass

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.Menu
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.droidhats.campuscompass.views.CalendarFragment
import com.droidhats.campuscompass.views.SplashFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.novoda.merlin.*

/**
 * This class has the objective of defining the commonly accessible attributes.
 * All fragments utilize the class for navigation, permission requests, and network connectivity purposes
 */
class MainActivity : MerlinActivity(), Connectable, Disconnectable, Bindable, NavigationView.OnNavigationItemSelectedListener {


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 2
    }
    private lateinit var snackbar: Snackbar
    private lateinit var navController : NavController

    /**
     * Overrides the activity's OnCreate method to instantiate the navigation component
     *
     * @param Bundle: the saved state of the application to pass between default Android methods.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        navView.setNavigationItemSelectedListener(this)
        snackbar = Snackbar.make(findViewById(android.R.id.content), "No internet found. Please check your network connection.", Snackbar.LENGTH_INDEFINITE)
    }

    /**
     *  Initializes the Merlin object to monitor the network connect while application is active
     *
     *  @return the Merlin Builder with the indicated callbacks
     */
    override fun createMerlin(): Merlin? {
        return Merlin.Builder()
            .withConnectableCallbacks()
            .withDisconnectableCallbacks()
            .withBindableCallbacks()
            .build(this)
    }

    /**
     *  Registers the Connectable, Disconnectable, and Bindable obects in superclass when activity resumes in view
     */
    override fun onResume() {
        super.onResume()
        registerConnectable(this)
        registerDisconnectable(this)
        registerBindable(this)
    }

    /**
     *  Checks the application's current network status and calls method to show alert if it's not available
     *
     *  @param networkStatus: the current network status of the application
     */
    override fun onBind(networkStatus: NetworkStatus) {
        if (!networkStatus.isAvailable) {
            onDisconnect()
        }
    }

    /**
     * Dismisses the Snackbar once the internet is made available
     */
    override fun onConnect() {
        if (snackbar != null) {
            snackbar.dismiss()
        }
    }

    /**
     *  Displays a red alert (Snackbar) when the application loses internet connection
     */
    override fun onDisconnect() {
        val snackBarView: View = snackbar.getView()
        snackBarView.setBackgroundColor(Color.RED)
        snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
        snackbar.show()
    }

    /**
     * Checks if the location permission has been previously granted
     *
     *  @return false if the location permission has not been granted and true if otherwise
     */
    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_DENIED
        ) {return false}
        return true
    }

    /**
     * Checks if the calendar permission has been previously granted
     *
     *  @return false if the calendar permission has not been granted and true if otherwise
     */
    fun checkCalendarPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR)
            == PackageManager.PERMISSION_DENIED
        ) {return false}
        return true
    }

    /**
     * Requests the permission to access the user's current location
     */
    fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Requests the permission to read the device's calendar application's data
     */
    fun requestCalendarPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_CALENDAR),
            READ_CALENDAR_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Responds to the result of requesting the location and calendar permissions
     *
     * @param requestCode: request code passed in requestPermissions() method
     * @param permissions: the requested permission
     * @param grantResults: the result of the requested permission
     */
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
        when (item.itemId) {
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
            R.id.nav_settings -> {
                navController.popBackStack(R.id.map_fragment, false)
                navController.navigate(R.id.nav_settings)
            }
            R.id.map_fragment -> navController.popBackStack(R.id.map_fragment, false)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     *  Inflates the side menu to be used by the fragments and adds items to the action bar (if present)
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav_drawer_main_menu, menu)
        return true
    }
}
