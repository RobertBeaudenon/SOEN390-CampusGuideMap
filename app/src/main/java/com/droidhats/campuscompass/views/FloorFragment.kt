package com.droidhats.campuscompass.views

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ToggleButton
import android.widget.ProgressBar
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.droidhats.mapprocessor.ProcessMap
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.OutdoorNavigationRoute
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.mancj.materialsearchbar.MaterialSearchBar
import com.otaliastudios.zoom.ZoomImageView
import kotlinx.android.synthetic.main.search_bar_layout.mapFragSearchBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream

class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel
    private lateinit var viewModelMapViewModel: MapViewModel
    private lateinit var root: View
    private lateinit var progressBar: ProgressBar
    private var canConsume: Boolean = true
    private var intermediateTransportID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.floor_fragment, container, false)
        return root
    }

    companion object{
        var OnFloorFragmentBackClicked: OnFloorFragmentBackClicked? = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(FloorViewModel::class.java)
        viewModelMapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        progressBar = root.findViewById(R.id.progressFloor)

        val startAndEnd = viewModel.getDirections()
        if (startAndEnd != null) {
            handleNavigation(startAndEnd)
        } else {
            handleView()
        }

        initSearchBar()
        viewModel.navigationRepository?.getNavigationRoute()?.observe(viewLifecycleOwner, Observer {
            if (it is OutdoorNavigationRoute) {
                findNavController().navigate(R.id.map_fragment)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            displayAlertMsg()
        }
    }

    private fun displayAlertMsg(){
        val builder = AlertDialog.Builder(requireContext())

        val alertTitle: String
        val alertMsg: String
        val exitMsg: String

        // Use directions to check if the floor fragment is used to highlight a room or
        // show directions between two rooms, and change the alert message accordingly.
        val directions = viewModel.getDirections()

        if (directions != null) {
            alertTitle = "Close Navigation"
            alertMsg = "Do you want to cancel the navigation?"
            exitMsg = "Exiting Navigation"
        } else {
            alertTitle = "Return to Map"
            alertMsg = "Do you want to return to the map?"
            exitMsg = "Returning to Map"
        }

        //set title for alert dialog
        builder.setTitle(alertTitle)

        //set message for alert dialog
        builder.setMessage(alertMsg)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            Toast.makeText(requireContext(), exitMsg, Toast.LENGTH_LONG).show()
            
            // Clear current navigation
            viewModel.navigationRepository?.cancelNavigation()
            OnFloorFragmentBackClicked?.onFloorFragmentBackClicked()
        }

        //performing cancel action
        builder.setNeutralButton("Cancel"){dialogInterface , which ->
            Toast.makeText(requireContext(),"Clicked Cancel\nOperation Canceled", Toast.LENGTH_LONG).show()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun handleView() {
        var floorNum: String? = arguments?.getString("floornum")
        var mapToDisplay: String = "hall8.svg" // default value
        val building : Building = arguments?.getParcelable("building")!!
        var floormap : String? = arguments?.getString("floormap")

        val floorPickerLayout: LinearLayout = root.findViewById(R.id.floorPickerLayout)
        floorPickerLayout.visibility = View.VISIBLE

        // handle case that you only want to view indoor map
        if (floorNum == null) {
            floorNum = building.getIndoorInfo().second.keys.first()
        }
        if (floormap == null) {
            floormap = building.getIndoorInfo().second[floorNum]
        }
        if(!floormap.isNullOrBlank()) {
            mapToDisplay = floormap
        }

        val inputStream: InputStream = requireContext().assets.open(mapToDisplay)
        val mapProcessor: ProcessMap = ProcessMap()
        var file: String = inputStream.bufferedReader().use { it.readText() }
        file = mapProcessor.automateSVG(file, floorNum)

        val buildingToHighlight: String? = arguments?.getString("id")

        if (buildingToHighlight == null) {
            val svg: SVG = SVG.getFromString(file)
            setImage(svg)
        } else {
            val highlightedSVG = mapProcessor.highlightClassroom(file, buildingToHighlight)
            val svg: SVG = SVG.getFromString(highlightedSVG)
            setImage(svg)
        }
        setNumberPicker(building, floorNum)
    }

    fun setNumberPicker(building: Building, floorNum: String?) {
        val mapProcessor = ProcessMap()
        // number picker stuff
        val maps : MutableList<String> = mutableListOf()
        val keys: Array<String> = Array(building.getIndoorInfo().second.size) { "" }
        var index = 0
        for (map in building.getIndoorInfo().second) {
            keys[index] = map.key
            maps.add(map.value)
            index++
        }

        val numberPicker: NumberPicker = root.findViewById(R.id.floorPicker)
        numberPicker.minValue = 0
        numberPicker.maxValue = keys.size - 1
        numberPicker.wrapSelectorWheel = false

        numberPicker.displayedValues = keys

        if (floorNum != null) {
            numberPicker.value = keys.indexOf(floorNum)
        } else {
            numberPicker.value = 0
        }

        numberPicker.setOnScrollListener(NumberPicker.OnScrollListener { picker, scrollState ->
            if(scrollState == 0){
                val newVal = numberPicker.value
                val newFloorNum = building!!.getIndoorInfo().second.keys.elementAt(newVal)

                val inputStream = requireContext().assets.open(maps[newVal])
                var file = inputStream.bufferedReader().use { it.readText() }
                file = mapProcessor.automateSVG(file, newFloorNum)
                val svg = SVG.getFromString(file)
                setImage(svg)
            }
        })
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

    fun handleNavigation(startToEnd: Pair<IndoorLocation, IndoorLocation>) {

        val indoorInstructionsLayout: LinearLayout = root.findViewById(R.id.indoorInstructionsLayout)
        indoorInstructionsLayout.visibility = View.VISIBLE

        var startAndEnd = startToEnd
        var building: Building
        if (startAndEnd.first.lID == "") {
            building = viewModelMapViewModel.getBuildings()[startAndEnd.second.buildingIndex]
        } else {
            building = viewModelMapViewModel.getBuildings()[startAndEnd.first.buildingIndex]
        }
        if ((startAndEnd.first.lID == "" && startAndEnd.second.floorNum != "1")
            || startAndEnd.second.lID == "" && startAndEnd.first.floorNum != "1"
            || startAndEnd.first.floorNum != startAndEnd.second.floorNum) {
            canConsume = false
        }

        val goingUp: Boolean = when(true) {
            (startAndEnd.first.lID != "" && startAndEnd.second.lID != "") -> {
                startAndEnd.first.getFloorNumber() < startAndEnd.second.getFloorNumber()
            }
            startAndEnd.first.lID == "" -> {
                1 < startAndEnd.second.getFloorNumber()
            }
            else -> { // second.lID is an empty string
                startAndEnd.first.getFloorNumber() < 1
            }
        }


        val doneButton: Button = requireActivity().findViewById(R.id.doneButtonFloor)
        doneButton.setOnClickListener {
            if (canConsume) {
                viewModel.consumeNavHandler()
            } else {
                canConsume = true
                if (intermediateTransportID != null) {
                    if (startAndEnd.first.lID == "") {
                        generateDirectionsOnFloor(
                            intermediateTransportID!!,
                            startAndEnd.second.lID,
                            startAndEnd.second.floorMap,
                            startAndEnd.second.floorNum,
                            goingUp
                        )
                    } else if (startAndEnd.second.lID == "") {
                        generateDirectionsOnFloor(
                            intermediateTransportID!!,
                            "entrance",
                            building.getIndoorInfo().second["1"]!!,
                            "1",
                            goingUp
                        )
                    } else {
                        generateDirectionsOnFloor(
                            intermediateTransportID!!,
                            startAndEnd.second.lID,
                            startAndEnd.second.floorMap,
                            startAndEnd.second.floorNum,
                            goingUp
                        )
                    }
                }
                intermediateTransportID = null
            }
        }
        doneButton.visibility = View.VISIBLE
        if (startAndEnd.first.lID == "") {
            val end: String = when {
                (startAndEnd.second.floorNum != "1") -> ""// intentionally left blank to find the nearest transportation method
                else -> startAndEnd.second.lID
            }
            generateDirectionsOnFloor(
                "entrance",
                end,
                building.getIndoorInfo().second["1"]!!,
                "1",
                goingUp
            )
        } else if (startAndEnd.first.floorNum != startAndEnd.second.floorNum) {
            val end: String = when {
                (startAndEnd.first.floorNum == "1") -> "entrance"
                else -> ""
            }
            generateDirectionsOnFloor(
                startAndEnd.first.lID,
                end,
                startAndEnd.first.floorMap,
                startAndEnd.first.floorNum,
                goingUp
            )
        } else {
            generateDirectionsOnFloor(
                startAndEnd.first.lID,
                startAndEnd.second.lID,
                startAndEnd.first.floorMap,
                startAndEnd.first.floorNum,
                goingUp
            )
        }

    }

    fun generateDirectionsOnFloor(start: String, end: String, floorMap: String, floorNum: String, goingUp: Boolean) {
        val inputStream: InputStream = requireContext().assets.open(floorMap)
        val file: String = inputStream.bufferedReader().use { it.readText() }
        val mapProcessor: ProcessMap = ProcessMap()
        val newFile = mapProcessor.automateSVG(file, floorNum)
        mapProcessor.readSVGFromString(newFile)
        var startPos = start
        var endPos = end
        if (start == "") {
            val pos = mapProcessor.getPositionWithId(endPos)
            if (pos != null) {
                startPos = mapProcessor.findNearestIndoorTransportation(pos, goingUp)
                if (goingUp) {
                    intermediateTransportID = startPos.replace("up", "down")
                } else {
                    intermediateTransportID = startPos.replace("down", "up")
                }

            } else {
                Toast.makeText(
                    context,
                    "FAILED TO GENERATE DIRECTIONS, NO END POSITION WAS FOUND",
                    Toast.LENGTH_LONG
                )
            }
        }
        if (end == "") {
            val pos = mapProcessor.getPositionWithId(startPos)
            if (pos != null) {
                endPos = mapProcessor.findNearestIndoorTransportation(pos, goingUp)
                if (goingUp) {
                    intermediateTransportID = endPos.replace("up", "down")
                } else {
                    intermediateTransportID = endPos.replace("down", "up")
                }
            } else {
                Toast.makeText(
                    context,
                    "FAILED TO GENERATE DIRECTIONS, NO START POSITION WAS FOUND",
                    Toast.LENGTH_LONG
                )
            }
        }
        progressBar.visibility = View.VISIBLE
        GlobalScope.launch {
            val svg: SVG = SVG.getFromString(
                mapProcessor
                    .getSVGStringFromDirections(Pair(startPos, endPos))
            )
            requireActivity().runOnUiThread {
                setImage(svg)
                progressBar.visibility = View.GONE
            }
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

    interface OnFloorFragmentBackClicked {
        fun onFloorFragmentBackClicked()
    }
}
