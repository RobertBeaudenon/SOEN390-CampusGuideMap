package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.droidhats.campuscompass.R
import androidx.navigation.fragment.findNavController

class ExploreFragment: Fragment() {

    private lateinit var root : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //launching the explore fragment
        root = inflater.inflate(R.layout.explore_fragment, container, false)

        //adding navigation to menu through the burger icon
        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_menu_explore)
        sideDrawerButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(
                GravityCompat.START)
        }

        //Event listners for button exploration categories
        val selectFoodButton: Button = root.findViewById(R.id.select_food_button)
        selectFoodButton.setOnClickListener {
            showPointsOfInterests("Food")
        }

        val selectDrinksButton: Button = root.findViewById(R.id.select_drinks_button)
        selectDrinksButton.setOnClickListener {
            showPointsOfInterests("Drinks")
        }

        val selectStudyButton: Button = root.findViewById(R.id.select_study_button)
        selectStudyButton.setOnClickListener {
            showPointsOfInterests("Study")
        }
        return root
    }

    //Navigates to appropriate fragment
    fun showPointsOfInterests(category: String){
        val bundle = Bundle()
        bundle.putString("name",category )
        findNavController().navigate(R.id.explore_category_fragment, bundle)
    }
}