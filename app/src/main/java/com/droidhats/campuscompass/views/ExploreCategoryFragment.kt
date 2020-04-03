package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.droidhats.campuscompass.R

class ExploreCategoryFragment: Fragment() {
    private lateinit var root : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //launching the explore fragment
        root = inflater.inflate(R.layout.explore_category_fragment, container, false)


        return root
    }
}