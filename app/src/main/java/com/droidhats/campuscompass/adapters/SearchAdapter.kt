package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Location
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.*

class SearchAdapter(
    private val items: List<Location>,  //the search results
    private val listener: OnSearchResultClickListener?,
    private val root: View
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var onClickListener: View.OnClickListener

    companion object{
        var navigateClick = false
    }

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

        with (holder){
            suggestion.text = item.name
            view.tag = item
            val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
            val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)
            val swapButton  = root.findViewById<ImageButton>(R.id.swapSearchButton)
            val backButton  = root.findViewById<ImageButton>(R.id.backFromNavigationButton)
            setNavigation.setOnClickListener {
                navigateClick = true
                destinationBar.visibility = View.VISIBLE
                swapButton.visibility = View.VISIBLE
                backButton.visibility = View.VISIBLE
                mainBar.maxWidth = root.resources.getDimension(R.dimen.search_bar_max_width).toInt()

                destinationBar.setQuery(item.name, false)

                mainBar.setQuery(mainBar.query, true)
                mainBar.queryHint = "From"

            }

            if (!navigateClick)
                view.setOnClickListener(onClickListener)
            else
                view.setOnClickListener{
                    if (mainBar.isActivated)
                        mainBar.setQuery(item.name, true)
                    if(destinationBar.isActivated)
                        destinationBar.setQuery(item.name, true)
                }
        }

    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val suggestion: TextView = view.search_suggestion
        val setNavigation: ImageButton = view.setNavigationPoint
    }

    interface OnSearchResultClickListener {
        fun onSearchResultClickListener(item: Location?)
    }

}