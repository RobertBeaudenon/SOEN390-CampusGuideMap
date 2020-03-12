package com.droidhats.campuscompass.views

import android.graphics.drawable.VectorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.FloorViewModel

class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.floor_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FloorViewModel::class.java)


        var vd : VectorDrawable
    }

}
