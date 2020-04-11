package com.droidhats.campuscompass.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.FavoritesAdapter
import com.droidhats.campuscompass.models.FavoritePlace
import com.droidhats.campuscompass.viewmodels.FavoritesViewModel

class MyPlacesFragment : DialogFragment() {
    lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var recyclerView : RecyclerView

    companion object {
        var onFavoriteClickListener: FavoritesAdapter.OnFavoriteClickListener? = null
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

        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_menu)
        sideDrawerButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(
                GravityCompat.START)
        }

        recyclerView = root.findViewById(R.id.favorites_recycler_view)

        favoritesViewModel.getCurrentLocation()

        favoritesViewModel.currentLocation.observe(viewLifecycleOwner, Observer {
            updateRecyclerView()
            recyclerView.adapter?.notifyDataSetChanged()
        })

        return root
    }

    private fun updateRecyclerView() {
        val places : List<FavoritePlace> = favoritesViewModel.getFavorites()
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = FavoritesAdapter(places, onFavoriteClickListener, favoritesViewModel)
        }
    }


}
