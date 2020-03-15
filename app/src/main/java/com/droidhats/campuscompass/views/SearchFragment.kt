package com.droidhats.campuscompass.views

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.SearchAdapter
import com.droidhats.campuscompass.viewmodels.SearchViewModel

class SearchFragment : Fragment()  {

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var root: View
    private var columnCount = 1

    companion object{
        var onSearchResultClickListener: SearchAdapter.OnSearchResultClickListener? = null
        var isNavigationViewOpen = false
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.search_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_suggestions_recycler_view)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.init()
        initSearch()
        observeSearchSuggestions()

        val backButton  = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
        backButton.setOnClickListener{
            isNavigationViewOpen = false
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit()

        }
    }


    private fun observeSearchSuggestions()
    {
        viewModel.googleSearchSuggestions.observe(viewLifecycleOwner , Observer { googlePredictions ->

            viewModel.indoorSearchSuggestions.observe(viewLifecycleOwner , Observer {indoorResults ->
                //Prepending indoor results to the google places results
                viewModel.searchSuggestions.value = indoorResults + googlePredictions
            })
        })

        //On change to the above results, the recycler view will be updated here
        viewModel.searchSuggestions.observe(viewLifecycleOwner, Observer {
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()
        })
    }

    private fun  initSearch(){
        val mainSearchBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
        val mainSearchText = mainSearchBar.findViewById<EditText>(R.id.search_src_text)
        mainSearchText.setTextColor(Color.WHITE)
        mainSearchBar.isIconified = false
        mainSearchBar.isActivated = true

        val secondarySearchBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)
        val secondarySearchText = secondarySearchBar.findViewById<EditText>(R.id.search_src_text)
        secondarySearchText.setTextColor(Color.WHITE)
        secondarySearchText.isActivated = false

        val swapButton  = root.findViewById<ImageButton>(R.id.swapSearchButton)
        swapButton.setOnClickListener{
            var temp = mainSearchBar.query
            mainSearchBar.setQuery(secondarySearchBar.query, false)
            secondarySearchBar.setQuery(temp, false)
        }

        initQueryTextListener(mainSearchBar)
        initQueryTextListener(secondarySearchBar)
    }

    private fun initQueryTextListener(searchView: SearchView)
    {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

       searchView.setOnQueryTextFocusChangeListener { _, isFocused ->
            searchView.isActivated = isFocused
          if(searchView.isActivated)
           viewModel.sendSearchQueries(searchView.query.toString())

        }
    }

    private fun updateRecyclerView() {
        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = SearchAdapter(viewModel.searchSuggestions.value!!, onSearchResultClickListener, root)
        }
    }

    override fun onDetach() {
        super.onDetach()
        isNavigationViewOpen = false
    }
}
