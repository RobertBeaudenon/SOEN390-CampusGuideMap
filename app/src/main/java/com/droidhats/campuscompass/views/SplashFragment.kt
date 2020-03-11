package com.droidhats.campuscompass.views

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.SplashViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var splashViewModel: SplashViewModel

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1 }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Lock the nav drawer access during to the splash screen
        val drawer : DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            //do nothing on purpose - user won't be able to exit during API initialization
        }
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        splashViewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        getLocationPermission()
        splashViewModel.init()

        //thread is used to represent initialization time
        //I couldn't put it in SplashViewModel (wouldn't let me change value in background thread)
        GlobalScope.launch {
            delay(3000)
               findNavController().navigate(R.id.action_splashFragment_to_mapsActivity)
            //UnLock drawer
            val drawer : DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    //requests the user's permission to access their current location
    private fun getLocationPermission() {
        //If the ACCESS_FINE_LOCATION permission has not been granted, request it now.
        if (ActivityCompat.checkSelfPermission(
                activity as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}
