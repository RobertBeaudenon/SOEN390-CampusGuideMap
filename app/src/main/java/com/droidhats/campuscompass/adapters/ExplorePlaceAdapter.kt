package com.droidhats.campuscompass.adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.roomdb.ExplorePlaceEntity
import com.droidhats.campuscompass.views.ExploreCategoryFragment
import kotlinx.android.synthetic.main.explore_recycler_item.view.*
import androidx.navigation.findNavController
import com.droidhats.campuscompass.models.Explore_Place


class ExplorePlaceAdapter(private val items: ArrayList<Explore_Place>, private val listener: ExploreCategoryFragment.OnExplorePlaceClickListener?):
    RecyclerView.Adapter<ExplorePlaceAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as ExplorePlaceEntity
            listener?.onExplorePlaceClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorePlaceAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.explore_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorePlaceAdapter.ViewHolder, position: Int) {
        val item = items[position]

        holder.titleView.text = item.place_name
        holder.rateView.text = item.place_rating
        holder.locationView.text = if (item.place_address.isNullOrBlank()) "None" else item.place_address

//        val drawable =
//            ContextCompat.getDrawable(holder.view.context, getPlaceImageResourceID(item.name))
//        holder.imageView.setImageDrawable(drawable)


       // holder.cardView.setCardBackgroundColor(item.color!!.toInt())

        with(holder.view) {

            holder.cardView.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("destExploreLocation",item.place_address )
                findNavController().popBackStack()
                findNavController().navigate(R.id.search_fragment, bundle)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.place_title_item
        val rateView: TextView = view.place_rating_item
        val locationView: TextView = view.place_location_item
        val imageView: ImageView = view.place_image
        var cardView: CardView = view.findViewById(R.id.explore_card_view)
      //  var navButton : ImageButton = view.findViewById(R.id.navigateFromEvent)

    }

    /**
     * Returns the explore place image from drawable resources
     * @param placeName: Used to map the place name to the place image.
     */
    fun getPlaceImageResourceID(placeName: String): Int {

        // The id for the place image resource is of Int type
        // Return the place image resource id that corresponds to the place name
        return when (placeName) {
            "Restaurant Maison Prathet Thai" -> R.drawable.resto_restaurantmaisonprathetthai
            "Les Saisons de Corée" -> R.drawable.resto_lessaisonsdecoree
            "Bar-B-Barn" -> R.drawable.resto_bar_b_barn
            "Kazu" -> R.drawable.resto_kazu
            "Bacaro Pizzeria" -> R.drawable.resto_bacaropizzeria
            "Garage Beirut" -> R.drawable.resto_garagebeirut
            "Da Vinci Ristorante" -> R.drawable.resto_davinciristorante
            else -> Log.v("ImageError", "couldn't load image")
        }
    }
}