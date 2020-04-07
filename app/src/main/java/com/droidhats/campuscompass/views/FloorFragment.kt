package com.droidhats.campuscompass.views

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.droidhats.mapprocessor.ProcessMap
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.mancj.materialsearchbar.MaterialSearchBar
import com.otaliastudios.zoom.ZoomImageView
import kotlinx.android.synthetic.main.search_bar_layout.mapFragSearchBar
import java.io.InputStream


class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.floor_fragment, container, false)

        var floor: String? = arguments?.getString("floornum")

        var mapToDisplay: String = "hall9.svg" // default value
        val building : Building? = arguments?.getParcelable("building")
        val maps: List<String>? = building?.getIndoorInfo()?.second
        val floormap : String? = arguments?.getString("floormap")
        if(floormap.isNullOrBlank()) {
            if (maps != null) mapToDisplay = maps[0]
        }
        else {
            mapToDisplay = floormap
        }

        var inputStream: InputStream = requireContext().assets.open(mapToDisplay)
        val buildingToHighlight: String? = arguments?.getString("id")

        if (buildingToHighlight == null) {
            val svg: SVG = SVG.getFromInputStream(inputStream)
            setImage(svg)
        } else {
            val mapProcessor: ProcessMap = ProcessMap()
            val file = inputStream.bufferedReader().use { it.readText() }
            val highlightedSVG = mapProcessor.highlightClassroom(file, buildingToHighlight)
            val svg: SVG = SVG.getFromString(highlightedSVG)
            setImage(svg)
        }

        val buttonPlus: ImageButton = root.findViewById(R.id.button_plus)
        var newSvg : SVG
        buttonPlus.setOnClickListener(View.OnClickListener {
            val indexOfCurrentMap = maps?.indexOf(mapToDisplay)
            if(indexOfCurrentMap != maps?.size?.minus(1)) {
                if (maps != null) if (indexOfCurrentMap != null) {
                    mapToDisplay = maps[indexOfCurrentMap.plus(1)]
                }
                inputStream = requireContext().assets.open(mapToDisplay)
                newSvg = SVG.getFromInputStream(inputStream)
                setImage(newSvg)
            }
        })

        val buttonMinus: ImageButton = root.findViewById(R.id.button_minus)
        buttonMinus.setOnClickListener(View.OnClickListener {
            val indexOfCurrentMap = maps?.indexOf(mapToDisplay)
            if(indexOfCurrentMap != 0) {
                if (maps != null) if (indexOfCurrentMap != null) {
                    mapToDisplay = maps[indexOfCurrentMap.minus(1)]
                }
                inputStream = requireContext().assets.open(mapToDisplay)
                newSvg = SVG.getFromInputStream(inputStream)
                setImage(newSvg)
            }
        })

        return root
    }

    fun setImage(svg: SVG) {
        val imageView: ZoomImageView = root.findViewById(R.id.floormap)
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)

        svg.setDocumentWidth(displayMetrics.widthPixels.toFloat())
        val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.widthPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0, 255, 255, 255)
        svg.renderToCanvas(canvas)
        imageView.setImageDrawable(BitmapDrawable(getResources(), bitmap))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FloorViewModel::class.java)

        initSearchBar()

        val startAndEnd = viewModel.getDirections()
        if (startAndEnd != null) {
            val inputStream: InputStream = requireContext().assets.open(startAndEnd.first.floorMap)
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor: ProcessMap = ProcessMap()
            mapProcessor.readSVGFromString(file)
            val svg: SVG = SVG.getFromString(mapProcessor
                .getSVGStringFromDirections(Pair(startAndEnd.first.lID, startAndEnd.second.lID)))
            setImage(svg)
        }
    }

    private fun initSearchBar() {
        var toggleButton = root.findViewById<ToggleButton>(R.id.toggleButton)
        toggleButton.visibility = View.GONE

        mapFragSearchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{

            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode) {
                    //Open the Nav Bar
                    MaterialSearchBar.BUTTON_NAVIGATION -> requireActivity().
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                }
            }
            override fun onSearchStateChanged(enabled: Boolean) {
                if (enabled) {
                    findNavController().navigate(R.id.search_fragment)
                    mapFragSearchBar.closeSearch()
                }
            }
            override fun onSearchConfirmed(text: CharSequence?) {
            }
        })
    }
}
