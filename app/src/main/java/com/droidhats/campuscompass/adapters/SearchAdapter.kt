package com.droidhats.campuscompass.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.GooglePlace
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.repositories.NavigationRepository
import com.droidhats.campuscompass.viewmodels.SearchViewModel
import com.droidhats.campuscompass.views.SearchFragment
import com.droidhats.campuscompass.views.SearchFragment.Companion.isNavigationViewOpen
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.setNavigationPoint
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.search_category
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.search_suggestion

class SearchAdapter(
    private val items: List<Location>,  //the search results
    private val listener: OnSearchResultClickListener?,
    private val root: View,
    private val viewModel: SearchViewModel
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as Location
            // Notify the activity/fragment that an item has been clicked
            listener?.onSearchResultClickListener(item)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_suggestion_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val infoMessage = root.findViewById<TextView>(R.id.search_info)
        if (items.isEmpty())
            infoMessage.visibility = View.VISIBLE
        else
            infoMessage.visibility = View.GONE

        with (holder){
            view.tag = item
            suggestion.text = item.name
            if (item is GooglePlace)
                category.text = item.category
            else if (item is IndoorLocation) {
                val indoorType = "Concordia University ${item.type}"
                category.text = indoorType
            }

            val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
            val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)

            setNavigation.setOnClickListener {
                SearchFragment.expandNavigationView(root)
                confirmSelection(destinationBar, item, false)
            }

            if (!isNavigationViewOpen) {
                view.setOnClickListener(onClickListener)
            }
            else {
                view.setOnClickListener {
                    if (mainBar.isActivated) {
                        confirmSelection(mainBar, item, true)
                    }
                    if (destinationBar.isActivated) {
                        confirmSelection(destinationBar, item, true)
                    }
                }
            }
        }
    }

    private fun confirmSelection(searchView: SearchView, location: Location, submit: Boolean) {

        val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
        val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)

        searchView.setQuery(location.name, submit)
        val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
        searchText.setTextColor(Color.GREEN)
        SearchFragment.NavigationPoints[searchView.id] = location

        if (SearchFragment.areRouteParametersSet())
            viewModel.getRouteTimes(SearchFragment.NavigationPoints[mainBar.id]!!, SearchFragment.NavigationPoints[destinationBar.id]!!)

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val suggestion: TextView = view.search_suggestion
        val category : TextView = view.search_category
        val setNavigation: ImageButton = view.setNavigationPoint
    }

    interface OnSearchResultClickListener {
        fun onSearchResultClickListener(item: Location?)
    }

}