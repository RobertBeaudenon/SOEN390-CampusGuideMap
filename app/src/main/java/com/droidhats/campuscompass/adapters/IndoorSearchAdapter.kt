package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.views.IndoorSearchFragment
import com.droidhats.campuscompass.views.IndoorSearchFragment.Companion.isNavigationViewOpen
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.setNavigationPoint
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.search_category
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.search_suggestion

class IndoorSearchAdapter(
    private val items: List<Location>,  //the search results
    private val listener: OnSearchResultClickListener?,
    private val indoorSearchFragment: IndoorSearchFragment,
    private val root: View
) : RecyclerView.Adapter<IndoorSearchAdapter.ViewHolder>() {

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
            if (item is IndoorLocation) {
                val indoorType = "Concordia University ${item.type}"
                category.text = indoorType
            }

            val mainBar =  root.findViewById<SearchView>(R.id.mainSearchBar)
            val destinationBar =  root.findViewById<SearchView>(R.id.secondarySearchBar)

            setNavigation.setOnClickListener {
                indoorSearchFragment.showNavigationView(item, false)
                indoorSearchFragment.confirmSelection(destinationBar, item, false)
            }

            if (!isNavigationViewOpen) {
                view.setOnClickListener(onClickListener)
            }
            else {
                view.setOnClickListener {
                    if (mainBar.isActivated) {
                        indoorSearchFragment.confirmSelection(mainBar, item, true)
                    }
                    if (destinationBar.isActivated) {
                        indoorSearchFragment.confirmSelection(destinationBar, item, true)
                    }
                }
            }
        }
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

    internal fun setResults(results : List<IndoorLocation>) {

    }
}