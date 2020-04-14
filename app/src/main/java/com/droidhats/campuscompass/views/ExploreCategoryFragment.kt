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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.ExplorePlaceAdapter
import com.droidhats.campuscompass.models.ExplorePlace
import com.droidhats.campuscompass.viewmodels.ExplorePlaceViewModel

/**
 * A View Fragment for the Explore places for one of the existing categories: Food, Study and Drinks.
 * It displays all the UI components of the places and dynamically interacts with the user click.
 */
class ExploreCategoryFragment: Fragment() ,AdapterView.OnItemSelectedListener {

    private lateinit var root : View
    private lateinit var viewModel: ExplorePlaceViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var type: String
    private lateinit var campus: String
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

       retrieveArguments()

        swipeRefreshLayout = root.findViewById(R.id.swipe_container)
        swipeRefreshLayout.isRefreshing = false;
        swipeRefreshLayout.isEnabled = false;
        recyclerView = root.findViewById(R.id.explore_recycler_view)

        observePlaces()

        return root
    }

    private fun observePlaces(){
        viewModel.fetchPlaces(campus, type)
        viewModel.getPlaces().observe(viewLifecycleOwner, Observer {
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()
        })

    }

    private fun updateRecyclerView() {
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = ExplorePlaceAdapter(viewModel.getPlaces().value!!, onExplorePlaceClickListener)
        }
    }

    private fun retrieveArguments(){
        val categoryName = arguments?.getString("name")
        if(categoryName != null) {
            val fragmentTitle: TextView = root.findViewById(R.id.text_category) as TextView
            fragmentTitle.text = "Explore - $categoryName"
        }

        val campusName = arguments?.getString("campus")
        if(campusName != null){
            campus = campusName
        }

        val typeName = arguments?.getString("type")
        if(typeName != null){
            type = typeName
        }
    }

    interface OnExplorePlaceClickListener {
        fun onExplorePlaceClick(item: ExplorePlace?)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {}

    override fun onNothingSelected(arg0: AdapterView<*>) {}
}