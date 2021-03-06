package com.droidhats.campuscompass.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ToggleButton
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.OutdoorNavigationRoute
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.google.android.material.navigation.NavigationView
import com.droidhats.mapprocessor.SVG as internalSVG
import com.droidhats.mapprocessor.ProcessMap
import com.mancj.materialsearchbar.MaterialSearchBar
import com.otaliastudios.zoom.ZoomImageView
import kotlinx.android.synthetic.main.search_bar_layout.mapFragSearchBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * This is the fragment that handles everything relating to displaying the maps on the indoors
 */
class FloorFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: FloorViewModel
    private lateinit var viewModelMapViewModel: MapViewModel
    private lateinit var root: View
    private lateinit var progressBar: ProgressBar
    private var canConsume: Boolean = true
    private var intermediateTransportID: String? = null
    private var hasTwoSteps: Boolean = false
    private var cachedSVG: SVG? = null
    private var prevSVG: SVG? = null
    private var prevInstruction: String? = null
    private lateinit var navView: NavigationView

    private var floorNum: String? = null
    var building: Building? = null
    private var floorMap: String? = null

    /**
     * On creation of the view, this method is called
     * @param inflater for inflating the proper layout
     * @param savedInstanceState
     * @param container
     * @return inflater with proper layout inflated
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.floor_fragment, container, false)
        navView = requireActivity().findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        settingsSharedPrefRetrieve()
        return root
    }

    companion object {
        var OnFloorFragmentBackClicked: OnFloorFragmentBackClicked? = null
        val settingsOff = ArrayList<String>()
    }

    /**
     * Method that gets called when the activity for the view is created
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(FloorViewModel::class.java)
        viewModelMapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        progressBar = root.findViewById(R.id.progressFloor)

        floorNum = arguments?.getString("floornum")
        building = arguments?.getParcelable("building")
        floorMap = arguments?.getString("floormap")

        val startAndEnd = viewModel.getDirections()
        if (startAndEnd != null) {
            handleNavigation(startAndEnd)
        } else {
            handleView(floorNum, building!!, floorMap)
        }

        initSearchBar()
        viewModel.navigationRepository?.getNavigationRoute()?.observe(viewLifecycleOwner, Observer {
            if (it is OutdoorNavigationRoute) {
                findNavController().navigate(R.id.map_fragment)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(this) {

            if (viewModel.navigationRepository != null
                && viewModel.navigationRepository?.getPrev() == null
            ) {
                displayAlertMsg(0)
            } else {
                viewModel.navigationRepository?.stepBack()
            }
            if(viewModel.navigationRepository?.navHandler == null)
                findNavController().popBackStack(R.id.map_fragment, false)
        }
    }

    /**
     * Method that gets called when the view is paused
     */
    override fun onPause() {
        super.onPause()
        navView.setNavigationItemSelectedListener(activity as MainActivity)
    }

    /**
     * Method that gets called when the view is resumed
     */
    override fun onResume() {
        super.onResume()
        navView.setNavigationItemSelectedListener(this)
    }

    /**
     * This method displays the alert message appropriate for the selected layout
     *
     * @param resourceID of the given layout
     */
    private fun displayAlertMsg(resourceID: Int) {
        val builder = AlertDialog.Builder(requireContext())

        val alertTitle = "Close Navigation"
        val alertMsg = "Do you want to cancel the navigation?"
        val exitMsg = "Exiting Navigation"

        // Use directions to check if the floor fragment is used to highlight a room or
        // show directions between two rooms, and change the alert message accordingly.
        val directions = viewModel.getDirections()

        if (directions == null) {
            navigateOut(resourceID)
            return
        }

        //set title for alert dialog
        builder.setTitle(alertTitle)

        //set message for alert dialog
        builder.setMessage(alertMsg)
        builder.setIcon(R.drawable.ic_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            Toast.makeText(requireContext(), exitMsg, Toast.LENGTH_LONG).show()

            // Clear current navigation
            viewModel.navigationRepository?.cancelNavigation()

            if (resourceID == 0) {
                OnFloorFragmentBackClicked?.onFloorFragmentBackClicked()
            } else {
                navigateOut(resourceID)
            }
            navView.setNavigationItemSelectedListener(activity as MainActivity)
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            Toast.makeText(
                requireContext(),
                "Clicked Cancel\nOperation Canceled",
                Toast.LENGTH_LONG
            ).show()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Navigate out method that navigates to the proper fragment given the id of the given layout
     *
     * @param resId of the layout
     */
    private fun navigateOut(resId: Int) {
        when (resId) {
            R.id.my_places_fragment -> {
                findNavController().popBackStack(R.id.map_fragment, false)
                findNavController().navigate(R.id.my_places_fragment)
            }
            R.id.nav_schedule -> {
                findNavController().popBackStack(R.id.map_fragment, false)
                findNavController().navigate(R.id.nav_schedule)
            }
            R.id.nav_explore -> {
                findNavController().popBackStack(R.id.map_fragment, false)
                findNavController().navigate(R.id.nav_explore)
            }
            R.id.nav_shuttle -> {
                findNavController().popBackStack(R.id.map_fragment, false)
                findNavController().navigate(R.id.nav_shuttle)
            }
            R.id.nav_settings -> {
                findNavController().popBackStack(R.id.map_fragment, false)
                findNavController().navigate(R.id.nav_settings)
            }
            R.id.map_fragment -> findNavController().popBackStack(R.id.map_fragment, false)
        }
    }

    /**
     * Handles the functionality related to looking at the floor maps
     *
     * @param floorNum string of the floor number
     * @param building building to who's floor maps to look at
     * @param floorMap to look at
     */
    private fun handleView(floorNum: String?, building: Building, floorMap: String?) {
        var mapToDisplay = "hall8.svg" // default value
        var floorNum = floorNum
        var floormap = floorMap

        val floorPickerLayout: LinearLayout = root.findViewById(R.id.floorPickerLayout)
        floorPickerLayout.visibility = View.VISIBLE

        // handle case that you only want to view indoor map
        if (floorNum == null) {
            floorNum = building.getIndoorInfo().second.keys.first()
        }
        if (floormap == null) {
            floormap = building.getIndoorInfo().second[floorNum]
        }
        if (!floormap.isNullOrBlank()) {
            mapToDisplay = floormap
        }

        val inputStream: InputStream = requireContext().assets.open(mapToDisplay)
        val mapProcessor = ProcessMap()
        var file: String = inputStream.bufferedReader().use { it.readText() }
        file = mapProcessor.automateSVG(file, floorNum)

        val classRoomToHighlight: String? = arguments?.getString("id")
        if (classRoomToHighlight == null) {
            val svg: SVG = SVG.getFromString(file)
            setImage(svg)
        } else {
            val highlightedSVG = mapProcessor.highlightClassroom(file, classRoomToHighlight)
            val svg: SVG = SVG.getFromString(highlightedSVG)
            setImage(svg)
        }
        setNumberPicker(building, floorNum)
    }

    /**
     * This does all the proper initialization of the number picker given the following parameters
     *
     * @param building building object to examine
     * @param floorNum number of current floor number
     */
    private fun setNumberPicker(building: Building, floorNum: String?) {
        val mapProcessor = ProcessMap()
        // number picker stuff
        val maps: MutableList<String> = mutableListOf()
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
            if (scrollState == 0) {
                val newVal = numberPicker.value
                val newFloorNum = building.getIndoorInfo().second.keys.elementAt(newVal)

                val inputStream = requireContext().assets.open(maps[newVal])
                var file = inputStream.bufferedReader().use { it.readText() }
                file = mapProcessor.automateSVG(file, newFloorNum)
                val svg = SVG.getFromString(file)
                setImage(svg)
            }
        })
    }

    /**
     * Sets the given svg object to the Image View
     *
     * @param svg object
     */
    private fun setImage(svg: SVG) {
        val imageView: ZoomImageView = root.findViewById(R.id.floormap)
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)

        svg.documentWidth = displayMetrics.widthPixels.toFloat()
        val bitmap = Bitmap.createBitmap(
            displayMetrics.widthPixels,
            displayMetrics.widthPixels,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0, 255, 255, 255)
        svg.renderToCanvas(canvas)
        if (cachedSVG != null) prevSVG = cachedSVG
        cachedSVG = svg
        imageView.visibility = View.VISIBLE
        imageView.setImageDrawable(BitmapDrawable(resources, bitmap))
    }

    /**
     * Handle the navigation between a start location to an end
     *
     * @param startToEnd pair of start to end locations
     */
    private fun handleNavigation(startAndEnd: Pair<IndoorLocation, IndoorLocation>) {

        var building: Building = if (startAndEnd.first.lID == "") {
            viewModelMapViewModel.getBuildings()[startAndEnd.second.buildingIndex]
        } else {
            viewModelMapViewModel.getBuildings()[startAndEnd.first.buildingIndex]
        }

        if (twoStepsInPath(startAndEnd)) {
            canConsume = false
            hasTwoSteps = true
        }

        // determining whether the user will be going up or down the building during the nav
        val goingUp: Boolean = isGoingUp(startAndEnd)

        // initialize navigation buttons
        val prevArrowButton: ImageView = requireActivity().findViewById(R.id.prevArrowFloor)
        val doneButton = initializeDoneButton(startAndEnd)
        val nextArrowButton = initializeNextArrowButton(
            startAndEnd, goingUp, building, doneButton, prevArrowButton
        )
        initializePrevButton(prevArrowButton, doneButton, nextArrowButton)

        // Generate the first set of directions
        generateFirstStep(startAndEnd, goingUp, building)
    }

    /**
     * Given the start and end points, returns whether the path will need 2 floors to reach their
     * destination
     *
     * @param startAndEnd start and end indoor location
     * @return whether there will be 2 steps
     */
    private fun twoStepsInPath(startAndEnd: Pair<IndoorLocation, IndoorLocation>): Boolean {
        return (startAndEnd.first.lID == "" && startAndEnd.second.floorNum != "1")
                || (startAndEnd.second.lID == "" && startAndEnd.first.floorNum != "1")
                || (startAndEnd.first.floorNum != startAndEnd.second.floorNum
                && startAndEnd.second.lID != "" && startAndEnd.first.lID != "")
    }

    /**
     * Determine given the start and end whether the user will have to go up or down the floors
     *
     * @param startAndEnd start and end indoor location
     * @return whether it is going up (true if yes)
     */
    private fun isGoingUp(startAndEnd: Pair<IndoorLocation, IndoorLocation>): Boolean {
        return when (true) {
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
    }

    /**
     * Initialize the functionality of the done button
     *
     * @param startAndEnd
     */
    private fun initializeDoneButton(startAndEnd: Pair<IndoorLocation, IndoorLocation>): Button {
        val indoorInstructionsLayout: LinearLayout = root.findViewById(R.id.indoorInstructionsLayout)
        indoorInstructionsLayout.visibility = View.VISIBLE
        // adding the appropriate functionality to the done button
        val doneButton: Button = requireActivity().findViewById(R.id.doneButtonFloor)
        doneButton.setOnClickListener {
            if (viewModel.navigationRepository != null
                && viewModel.navigationRepository!!.isLastStep()
            ) {
                indoorInstructionsLayout.visibility = View.GONE
                handleView(
                    startAndEnd.second.floorNum,
                    viewModelMapViewModel.getBuildings()[startAndEnd.second.buildingIndex],
                    startAndEnd.second.floorMap
                )
                viewModel.navigationRepository?.cancelNavigation()
            }
            viewModel.consumeNavHandler()
        }
        // setting the proper text for the done button.
        if (startAndEnd.second.lID == "") {
            requireActivity().findViewById<Button>(R.id.doneButtonFloor).text = "Outside"
        } else {
            requireActivity().findViewById<Button>(R.id.doneButtonFloor).text = "Finished!"
        }
        return doneButton
    }

    /**
     * The functionality for this is to launch the appropriate directions to generate the next
     * step.
     *
     * @param startAndEnd start and end indoor locations
     * @param goingUp whether the user is going up
     * @param building the building that the user is navigating in
     * @param doneButton the reference to the done button
     * @param prevArrowButton the reference to the previous arrow button
     */
    private fun initializeNextArrowButton(
        startAndEnd: Pair<IndoorLocation, IndoorLocation>,
        goingUp: Boolean,
        building: Building,
        doneButton: Button,
        prevArrowButton: ImageView
    ): ImageView {
        val nextArrowButton: ImageView = requireActivity().findViewById(R.id.nextArrowFloor)
        nextArrowButton.setOnClickListener {
            canConsume = true
            if (prevSVG != null) {
                setImage(prevSVG!!)
                val tmp = prevInstruction
                prevInstruction =
                    requireActivity().findViewById<TextView>(R.id.instructions_text).text.toString()
                requireActivity().findViewById<TextView>(R.id.instructions_text).text = tmp
            } else if (intermediateTransportID != null && prevSVG == null) {
                generateSecondStep(startAndEnd, goingUp, building)
            }
            intermediateTransportID = null
            it.visibility = View.GONE
            doneButton.visibility = View.VISIBLE
            prevArrowButton.visibility = View.VISIBLE
        }
        return nextArrowButton
    }

    /**
     * The previous arrow button is to bring back the old svg if it's cached and set the
     * proper visibility of the buttons
     *
     * @param prevArrowButton to initialize it with having to get the same reference twice
     * @param doneButton to set its visibility
     * @param nextArrowButton to set its visibility
     */
    fun initializePrevButton(
        prevArrowButton: ImageView,
        doneButton: Button,
        nextArrowButton: ImageView
    ) {
        prevArrowButton.setOnClickListener {
            nextArrowButton.visibility = View.VISIBLE
            doneButton.visibility = View.GONE
            it.visibility = View.GONE
            if (prevSVG != null) {
                setImage(prevSVG!!)
                val tmp = prevInstruction
                prevInstruction =
                    requireActivity().findViewById<TextView>(R.id.instructions_text).text.toString()
                requireActivity().findViewById<TextView>(R.id.instructions_text).text = tmp
            }
        }
    }

    /**
     * Generate the directions for the first step.
     *
     * @param startAndEnd the start and end indoor locations
     * @param goingUp whether the user is navigating up or down
     * @param building the building for which the user is within
     */
    private fun generateFirstStep(
        startAndEnd: Pair<IndoorLocation, IndoorLocation>,
        goingUp: Boolean,
        building: Building
    ) {
        when {
            startAndEnd.first.lID == "" -> {
                val end: String = when {
                    // intentionally left blank to find the nearest transportation method
                    (startAndEnd.second.floorNum != "1") -> ""
                    else -> startAndEnd.second.lID
                }
                handleDirections(
                    "entrance",
                    end,
                    building.getIndoorInfo().second["1"]!!,
                    "1",
                    goingUp,
                    Pair(startAndEnd.second.name, startAndEnd.second.floorNum)
                )
            }
            startAndEnd.first.floorNum != startAndEnd.second.floorNum -> {
                // to bring to the entrance or the transportation method
                val end: String = when {
                    (startAndEnd.second.lID == "" && startAndEnd.first.floorNum == "1") -> "entrance"
                    else -> ""
                }

                val floorNum: String =
                    if (startAndEnd.second.lID == "") {
                        "1"
                    } else {
                        startAndEnd.second.floorNum
                    }

                handleDirections(
                    startAndEnd.first.lID,
                    end,
                    startAndEnd.first.floorMap,
                    startAndEnd.first.floorNum,
                    goingUp,
                    Pair(startAndEnd.second.name, floorNum)
                )
            }
            else -> {
                handleDirections(
                    startAndEnd.first.lID,
                    startAndEnd.second.lID,
                    startAndEnd.first.floorMap,
                    startAndEnd.first.floorNum,
                    goingUp,
                    Pair(startAndEnd.second.name, startAndEnd.second.floorNum)
                )
            }
        }
    }

    /**
     * Generate the directions for the second step.
     *
     * @param startAndEnd the start and end indoor locations
     * @param goingUp whether the user is going up or down in the building
     * @param building the building for which the user is navigating in
     */
    private fun generateSecondStep(
        startAndEnd: Pair<IndoorLocation, IndoorLocation>,
        goingUp: Boolean,
        building: Building
    ) {
        if (startAndEnd.second.lID == "") {
            handleDirections(
                intermediateTransportID!!,
                "entrance",
                building.getIndoorInfo().second["1"]!!,
                "1",
                goingUp,
                Pair(startAndEnd.second.name, startAndEnd.second.floorNum)
            )
        } else {
            handleDirections(
                intermediateTransportID!!,
                startAndEnd.second.lID,
                startAndEnd.second.floorMap,
                startAndEnd.second.floorNum,
                goingUp,
                Pair(startAndEnd.second.name, startAndEnd.second.floorNum)
            )
        }
    }

    /**
     * Given the parameters, generate a floor map with directions on it and display it to the view
     *
     * @param start id of the start element
     * @param end id of the end element
     * @param floorMap to display
     * @param floorNum to give the classrooms proper room numbers
     * @param goingUp whether the user will be going up
     * @param classAndFloorDest class and floor destinations for the instructions
     */
    private fun handleDirections(
        start: String,
        end: String,
        floorMap: String,
        floorNum: String,
        goingUp: Boolean,
        classAndFloorDest: Pair<String, String>
    ) {
        // initializing variables for the whole method to use
        val inputStream: InputStream = requireContext().assets.open(floorMap)
        val file: String = inputStream.bufferedReader().use { it.readText() }
        val mapProcessor = ProcessMap()
        val newFile = mapProcessor.automateSVG(file, floorNum)
        mapProcessor.readSVGFromString(newFile)

        // in order to change them if they're empty
        var startPos = start
        var endPos = end

        val pos = mapProcessor.getPositionWithId(startPos)
        val transportationMethod = assignIntermediateTransport(pos, mapProcessor, goingUp)

        when {
            // handle when there are is no start position
            start == "" -> startPos = transportationMethod.id
            // handle when there are is no end position
            end == "" -> endPos = transportationMethod.id
        }

        handleInstructions(end, transportationMethod, classAndFloorDest)

        setDirections(startPos, endPos, mapProcessor)
    }

    /**
     * Assign the intermediate mode of transportation
     * @param pos position to find nearest transport
     * @param mapProcessor
     * @param goingUp whether the user has to go up (important for the escalators)
     */
    private fun assignIntermediateTransport(
        pos: Pair<Double, Double>?,
        mapProcessor: ProcessMap,
        goingUp: Boolean
    ): internalSVG {
        var transportationMethod: internalSVG = internalSVG("", "", 0.0, 0.0)
        if (pos != null) {
            try {
                transportationMethod = mapProcessor.findNearestIndoorTransportation(pos, goingUp)
                val position = transportationMethod.id
                intermediateTransportID = if (goingUp) {
                    position.replace("up", "down")
                } else {
                    position.replace("down", "up")
                }
            }
            catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to generate directions, could not find transportation method " +
                            "to reach next floor",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Failed to generate directions, could not find transportation method " +
                        "to reach next floor",
                Toast.LENGTH_LONG
            ).show()
        }
        return transportationMethod
    }

    /**
     * Handle the stuff related to the instructions (messages and displaying arrow buttons)
     */
    private fun handleInstructions(
        end: String,
        transportationMethod: internalSVG,
        classAndFloorDest: Pair<String, String>
    ) {
        // setting proper instruction
        val message: String = when (end) {
            "" -> "Take the ${transportationMethod.transportationType} until you reach floor " +
                    classAndFloorDest.second
            "entrance" -> "Make your way to the entrance and go outside"
            else -> "Make your way to class ${classAndFloorDest.first}"
        }

        // setting current instruction to the previous if there were no previous instructions
        if (prevInstruction == null) {
            prevInstruction = message
        }

        // Set the message
        requireActivity().findViewById<TextView>(R.id.instructions_text).text = message

        // Handle visibility of appropriate buttons depending on which step we're at
        if (!canConsume) {
            requireActivity().findViewById<Button>(R.id.doneButtonFloor).visibility = View.GONE
            requireActivity().findViewById<ImageView>(R.id.nextArrowFloor).visibility = View.VISIBLE
        } else {
            requireActivity().findViewById<Button>(R.id.doneButtonFloor).visibility = View.VISIBLE
            requireActivity().findViewById<ImageView>(R.id.nextArrowFloor).visibility = View.GONE
        }
    }

    /**
     * Setting the directions on the image view
     *
     * @param start id of the start location
     * @param end id of the end location
     * @param mapProcessor the reference to the map processor to run the get directions method
     */
    private fun setDirections(start: String, end: String, mapProcessor: ProcessMap) {
        progressBar.visibility = View.VISIBLE
        val imageView: ZoomImageView = root.findViewById(R.id.floormap)
        imageView.visibility = View.GONE

        // launching the directions in a coroutine so that it does not freeze the screen while
        // the progress bar animates and the user can still interact with the app.
        GlobalScope.launch {
            val svg: SVG = SVG.getFromString(
                mapProcessor
                    .getSVGStringFromDirections(Pair(start, end))
            )
            requireActivity().runOnUiThread {
                setImage(svg)
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Initialize the search bar functionality
     */
    private fun initSearchBar() {
        var toggleButton = root.findViewById<ToggleButton>(R.id.toggleButton)
        toggleButton.visibility = View.GONE

        mapFragSearchBar.setOnSearchActionListener(object :
            MaterialSearchBar.OnSearchActionListener {

            /**
             * Method to be called when the search bar's hamburger button is clicked
             * @param buttonCode
             */
            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    //Open the Nav Bar
                    MaterialSearchBar.BUTTON_NAVIGATION -> requireActivity().findViewById<DrawerLayout>(
                        R.id.drawer_layout
                    ).openDrawer(GravityCompat.START)
                }
            }

            /**
             * Method to be called when the search state has changed
             * @param enabled
             */
            override fun onSearchStateChanged(enabled: Boolean) {
                if (enabled) {
                    findNavController().navigate(R.id.search_fragment)
                    mapFragSearchBar.closeSearch()
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                // unused implementation, do nothing
            }
        })
    }

    interface OnFloorFragmentBackClicked {
        fun onFloorFragmentBackClicked()
    }

    /**
     * When a navigation item is selected, this method gets called to close the drawer
     * and display an alert message to stop the user from navigating away.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        displayAlertMsg(item.itemId)
        return true
    }

    private fun settingsSharedPrefRetrieve() {
        settingsOff.clear()
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val set = sharedPref.getStringSet("settingOffArray", null)

        for (element in set!!) {
            settingsOff.add(element)
        }
    }
}
