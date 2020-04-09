package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.FavoritesAdapter
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.viewmodels.FavoritesViewModel

class MyPlacesFragment : DialogFragment() {
    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var recyclerView : RecyclerView

    companion object {
        var onFavoriteClickListener: OnFavoriteClickListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favoritesViewModel = ViewModelProvider(this)
            .get(FavoritesViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root : View = inflater.inflate(R.layout.my_places_fragment, container, false)
        var recyclerView : RecyclerView = root.findViewById(R.id.favorites_recycler_view)

        updateRecyclerView()
        recyclerView.adapter?.notifyDataSetChanged()

        return root
    }

    private fun updateRecyclerView() {
        val places : List<FavoritePlace> = favoritesViewModel.getFavorites()

        with(recyclerView) {
            layoutManager = when {
                places.size <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, places.size)
            }

            adapter = FavoritesAdapter(
                places,
                onFavoriteClickListener
            )
        }
    }

    interface OnFavoriteClickListener {
        fun onFavoriteClick(item: FavoritePlace?)
    }
}
