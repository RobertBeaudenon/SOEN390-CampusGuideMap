package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Location
import com.droidhats.campuscompass.views.SearchFragment
import kotlinx.android.synthetic.main.search_suggestion_recycler_item.view.*

class SearchAdapter(
    private val items: List<Location>,  //the search results
    private val listener: OnSearchResultClickListener?
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

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

        holder.suggestion.text = item.name
        with(holder.view) {
            tag = item
           setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val suggestion: TextView = view.search_suggestion
    }

    interface OnSearchResultClickListener {
        fun onSearchResultClickListener(item: Location?)
    }

}