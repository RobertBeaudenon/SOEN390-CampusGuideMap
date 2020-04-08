package com.droidhats.campuscompass.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.SplashViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var splashViewModel: SplashViewModel

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
        splashViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        splashViewModel.init()

        //this structure will change based on refactoring our app
        //for now, it lets MainActiivty manage the navigation if no permissions were given
        if(!(activity as MainActivity).checkLocationPermission()) {
            (activity as MainActivity).requestLocationPermission()
        } else {
            //if permission is granted, navigate to MapFragment
            navigateToMapFragment()
        }
    }

    fun navigateToMapFragment() {
        GlobalScope.launch {
            delay(2000)
            requireActivity().runOnUiThread{
                findNavController().navigate(R.id.action_splashFragment_to_mapsActivity)
            }
        }
        //UnLock drawer
        val drawer : DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}
