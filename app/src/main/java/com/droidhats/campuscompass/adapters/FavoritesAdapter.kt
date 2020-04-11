package com.droidhats.campuscompass.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.viewmodels.FavoritesViewModel
import com.google.android.gms.maps.model.LatLng

class FavoritesAdapter(
    private val items: List<FavoritePlace>,
    private val listener: OnFavoriteClickListener?,
    private val favoritesViewModel: FavoritesViewModel
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var onClickListener: View.OnClickListener

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

        val distanceToPlace : Double? = favoritesViewModel.haversineDist(LatLng(item.latitude, item.longitude))
        holder.distanceView.text = if (distanceToPlace != null) (String.format("%.2f km", distanceToPlace)) else ""

        with(holder) {
            view.tag = item
            navButton.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("destEventLocation",item.address )
                view.findNavController().popBackStack()
                view.findNavController().navigate(R.id.search_fragment, bundle)
            }

            view.setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.findViewById(R.id.favorites_title_item)
        val locationView: TextView = view.findViewById(R.id.favorites_location_item)
        val navButton : ImageButton = view.findViewById(R.id.setNavigationPoint)
        val distanceView: TextView = view.findViewById(R.id.favorites_distance_view)
    }

    interface OnFavoriteClickListener {
        fun onFavoriteClick(item: FavoritePlace?)
    }
}