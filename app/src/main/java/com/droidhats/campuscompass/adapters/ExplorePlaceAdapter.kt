package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.views.ExploreCategoryFragment
import kotlinx.android.synthetic.main.explore_recycler_item.view.*
import com.droidhats.campuscompass.models.ExplorePlace
import com.squareup.picasso.Picasso;

/**
 * This class will create access to the data items in the view
 * in order to populate the explore places
 * @param items
 * @param listener
 */
class ExplorePlaceAdapter(private val items: ArrayList<ExplorePlace>, private val listener: ExploreCategoryFragment.OnExplorePlaceClickListener?):
    RecyclerView.Adapter<ExplorePlaceAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as ExplorePlace?
            listener?.onExplorePlaceClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorePlaceAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.explore_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorePlaceAdapter.ViewHolder, position: Int) {
        val item = items[position]

        holder.titleView.text = item.placeName
        holder.rateView.text = item.placeRating
        var rate: Double = item.placeRating!!.toDouble()
        holder.rateStars.rating = rate.toFloat()
        holder.locationView.text = if (item.placeAddress.isNullOrBlank()) "None" else item.placeAddress
        Picasso.get().load(item.placeImage).into(holder.imageView);

        with(holder.view) {
            tag = item
            holder.cardView.setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.place_title_item
        val rateView: TextView = view.place_rating_item
        val rateStars: RatingBar = view.rating
        val locationView: TextView = view.place_location_item
        val imageView: ImageView = view.place_image
        var cardView: CardView = view.findViewById(R.id.explore_card_view)
    }
}