package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.ExplorePlaceAdapter
import com.droidhats.campuscompass.models.Explore_Place
import com.droidhats.campuscompass.viewmodels.ExplorePlaceViewModel

/**
 * A View Fragment for the Explore places for one of the existing categories: Food, Study and Drinks.
 * It displays all the UI components of the places and dynamically interacts with the user click.
 */
class ExploreCategoryFragment: Fragment() ,AdapterView.OnItemSelectedListener {

    private lateinit var root : View
    private lateinit var viewModel: ExplorePlaceViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var places: ArrayList<Explore_Place>
    private var columnCount = 1

    companion object {
        var onExplorePlaceClickListener: OnExplorePlaceClickListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(ExplorePlaceViewModel::class.java)
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

       var list : ArrayList<String> = retrieveArguments()

       var type: String = when(list.get(0)){
            "Food" -> "restaurant"
            "Drinks" -> "bar"
            "Study" ->  "cafe"
           else -> ""
       }

        recyclerView = root.findViewById(R.id.explore_recycler_view)
        viewModel.getPlaces(list.get(1), type).observe(viewLifecycleOwner, Observer {
             places = ArrayList()
             places = it
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()
        })
        return root
    }

    private fun updateRecyclerView() {

        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = ExplorePlaceAdapter(places, onExplorePlaceClickListener)
        }
    }

    private fun retrieveArguments(): ArrayList<String> {
        val list = ArrayList<String>()
        val categoryName = arguments?.getString("name")
        if(categoryName != null) {
            list.add(categoryName)
            val fragmentTitle: TextView = root.findViewById(R.id.text_category) as TextView
            fragmentTitle.text = "Explore - $categoryName"
        }

        val campus = arguments?.getString("campus")
        if(campus != null){
            list.add(campus)
        }
        return list
    }

    interface OnExplorePlaceClickListener {
        fun onExplorePlaceClick(item: Explore_Place?)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {}

    override fun onNothingSelected(arg0: AdapterView<*>) {}
}