package com.droidhats.campuscompass.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.roomdb.ExplorePlaceEntity
import kotlinx.android.synthetic.main.explore_recycler_item.view.*

class ExplorePlaceAdapter(private val items: ArrayList<ExplorePlaceEntity>):
    RecyclerView.Adapter<ExplorePlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorePlaceAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.explore_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorePlaceAdapter.ViewHolder, position: Int) {
        val item = items[position]

        holder.titleView.text = item.name
        holder.rateView.text = item.rating.toString()

        holder.locationView.text = if (item.address.isNullOrBlank()) "None" else item.address

       // holder.cardView.setCardBackgroundColor(item.color!!.toInt())

//        with(holder.view) {
//            tag = item
//            holder.navButton.setOnClickListener{
//                val bundle = Bundle()
//                bundle.putString("destEventLocation",item.location )
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.search_fragment, bundle)
//            }
//        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.place_title_item
        val rateView: TextView = view.place_rating_item
        val locationView: TextView = view.place_location_item
        var cardView: CardView = view.findViewById(R.id.explore_card_view)
      //  var navButton : ImageButton = view.findViewById(R.id.navigateFromEvent)

    }
}