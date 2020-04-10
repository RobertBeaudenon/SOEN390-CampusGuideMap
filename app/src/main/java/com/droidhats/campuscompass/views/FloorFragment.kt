package com.droidhats.campuscompass.views

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.droidhats.mapprocessor.ProcessMap
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

        val inputStream: InputStream = requireContext().assets.open("hall8.svg")

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
        viewModel = ViewModelProvider(this).get(FloorViewModel::class.java)

        initSearchBar()

        val startAndEnd = viewModel.getDirections()
        if (startAndEnd != null) {
            val inputStream: InputStream = requireContext().assets.open("hall8.svg")
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor: ProcessMap = ProcessMap()
            mapProcessor.readSVGFromString(file)
            val svg: SVG = SVG.getFromString(mapProcessor.getSVGStringFromDirections(startAndEnd))
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

    /*
        Might not even need this function, TWEAK it to your need or remove it.
        In this function I am basically showing how you can view the status
        of each switch and based on that you can perform action. In this case I just output
        a toast message. So the big picture is wherever in your code you want to check whether
        whether the switch is on or off you can do so by following the strategy in this code example
         below. Please remove this if you will not use this function.
     */
     fun preferenceChanged() {
        val stairsSwitch: Switch = root.findViewById(R.id.switch_settings_stairs)
        val escalatorsSwitch: Switch = root.findViewById(R.id.switch_settings_escalators)
        val elevatorsSwitch: Switch = root.findViewById(R.id.switch_settings_elevators)

        if(stairsSwitch.isChecked) {
            Toast.makeText(context, "The Stairs is ON", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "The Stairs is OFF", Toast.LENGTH_LONG).show()
        }

        if(escalatorsSwitch.isChecked) {
            Toast.makeText(context, "The Escalators is ON", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "The Escalators is OFF", Toast.LENGTH_LONG).show()
        }

        if(elevatorsSwitch.isChecked) {
            Toast.makeText(context, "The Elevators is ON", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "The Elevators is OFF", Toast.LENGTH_LONG).show()
        }
    }
}
