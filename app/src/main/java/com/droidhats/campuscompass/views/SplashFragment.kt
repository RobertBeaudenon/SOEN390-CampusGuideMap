package com.droidhats.campuscompass.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

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
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //do nothing on purpose - user won't be able to exit during API initialization
            }
        })
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        splashViewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        splashViewModel.init()

        //hardCode is a dummy variable that we observe
        splashViewModel.hardCode.observe(viewLifecycleOwner, Observer {

            //thread is used to represent initialization time
            //I couldn't put it in SplashViewModel (wouldn't let me change value in background thread)
            GlobalScope.launch {
                delay(3000)
                if(it) {
                    findNavController().navigate(R.id.action_splashFragment_to_mapsActivity)
                }
            }
        })
    }
}
