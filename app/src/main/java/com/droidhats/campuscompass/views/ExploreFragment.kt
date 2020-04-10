package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.droidhats.campuscompass.R
import androidx.navigation.fragment.findNavController

/**
 * A View Fragment for the Explore places to choose between one of the categories: Food, Study and Drinks.
 * It displays all the UI components of the places and dynamically interacts with the user click.
 */
class ExploreFragment: Fragment() {

    private lateinit var root: View
    private var campus: String = "SGW"

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

        var toggleButton: ToggleButton = root.findViewById(R.id.toggleButton2)

        toggleButton.setOnCheckedChangeListener { _, onSwitch ->
            if (onSwitch) {
                campus = "Loyola"
            } else {
                campus = "SGW"
            }
        }

        //Event listners for button exploration categories
        val selectFoodButton: Button = root.findViewById(R.id.select_food_button)
        selectFoodButton.setOnClickListener {
            showPointsOfInterests("Food", campus)
        }

        val selectDrinksButton: Button = root.findViewById(R.id.select_drinks_button)
        selectDrinksButton.setOnClickListener {
            showPointsOfInterests("Drinks", campus)
        }

        val selectStudyButton: Button = root.findViewById(R.id.select_study_button)
        selectStudyButton.setOnClickListener {
            showPointsOfInterests("Study", campus)
        }
        return root
    }

    //Navigates to appropriate fragment
    fun showPointsOfInterests(category: String, campus: String){
        val bundle = Bundle()
        bundle.putString("name",category )
        bundle.putString("campus",campus )
        findNavController().navigate(R.id.explore_category_fragment, bundle)
    }
}