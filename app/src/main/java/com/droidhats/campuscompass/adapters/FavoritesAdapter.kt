package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.views.MyPlacesFragment

class FavoritesAdapter(
    private val items: List<FavoritePlace>,
    private val listener: MyPlacesFragment.OnFavoriteClickListener?
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener
    init {
        onClickListener = View.OnClickListener { view ->
             val item = view.tag as FavoritePlace
            // Notify the activity/fragment that an item has been clicked
             listener?.onFavoriteClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.titleView.text = item.name

        holder.locationView.text = item.address?:"None"

        with(holder.view) {
            tag = item
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.findViewById(R.id.favorites_title_item)
        val locationView: TextView = view.findViewById(R.id.favorites_location_item)
        var cardView: CardView = view.findViewById(R.id.favorites_card_view)
        var navButton : ImageButton = view.findViewById(R.id.navigateFromEvent)

    }

}