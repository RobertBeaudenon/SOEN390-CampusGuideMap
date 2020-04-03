package com.droidhats.campuscompass.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.R
import com.google.android.libraries.places.api.model.Place

class ExploreCategoryFragment: Fragment() ,AdapterView.OnItemSelectedListener {
    private lateinit var root : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)





    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //launching the explore fragment
        root = inflater.inflate(R.layout.explore_category_fragment, container, false)

        //adding navigation to explore menu through the burger icon
        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_explore)
        sideDrawerButton.setOnClickListener {
            findNavController().popBackStack()
        }

        retrieveArguments()

        //initialize spinner of distance
        val spinner1:Spinner = root.findViewById(R.id.spinner1)
        val distances = resources.getStringArray(R.array.distances)
        if (spinner1 != null) {
            val adapter = ArrayAdapter<String>(
                requireActivity(),
                android.R.layout.simple_spinner_item,distances
            )
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner1.adapter = adapter
        }





        return root
    }

    private fun retrieveArguments(){
        val categoryName = arguments?.getString("name")
        if(categoryName != null) {
            val fragmentTitle: TextView = root.findViewById(R.id.text_category) as TextView
            fragmentTitle.text = "Explore - $categoryName"
        }

    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        //textView_msg!!.text = "Selected : "+[position]
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }
}