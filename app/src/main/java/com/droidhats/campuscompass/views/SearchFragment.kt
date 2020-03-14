package com.droidhats.campuscompass.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.SearchAdapter
import com.droidhats.campuscompass.viewmodels.SearchViewModel


class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var root: View
    private var columnCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.search_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_suggestions_recycler_view)
        initMainSearchBar()
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.init()
        observeSearchSuggestions()
    }


    private fun observeSearchSuggestions()
    {
        viewModel.googleSearchSuggestions.observe(viewLifecycleOwner , Observer { googleResults ->
            viewModel.indoorSearchSuggestions.observe(viewLifecycleOwner , Observer {indoorResults ->
                //Prepending indoor results to the google places results
                viewModel.searchSuggestions.value = indoorResults.plus(googleResults)
            })
        })

        //On change to the above results, the recycler view will be updated here
        viewModel.searchSuggestions.observe(viewLifecycleOwner, Observer {
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()
        })
    }
    private fun initMainSearchBar()
    {
       val mainSearchBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
        mainSearchBar.isIconified = false
        mainSearchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {

                return if (!p0.isNullOrBlank()) viewModel.sendSearchQueries(p0)
                else {
                    viewModel.searchSuggestions.value = emptyList()
                    return false
                }
            }
        })
    }

    private fun updateRecyclerView() {
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = SearchAdapter(viewModel.searchSuggestions.value!!)
        }
    }

}
