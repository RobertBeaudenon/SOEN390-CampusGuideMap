package com.droidhats.campuscompass.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.ShuttleViewModel
import kotlinx.android.synthetic.main.shuttle_fragment.*


class ShuttleFragment : Fragment() {

    private lateinit var viewModel: ShuttleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shuttle_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ShuttleViewModel::class.java)

        viewModel.getSGWShuttleTime().observe(viewLifecycleOwner , Observer {
            var times = ""
            for (i in it)
                times += i

            sgwPlaceholderTimes.text = times
        })

    }

}
