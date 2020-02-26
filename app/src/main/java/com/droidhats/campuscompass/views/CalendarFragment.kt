package com.droidhats.campuscompass.views

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.CalendarAdapter
import com.droidhats.campuscompass.models.CalendarEvent
import com.droidhats.campuscompass.viewmodels.CalendarViewModel

class CalendarFragment : DialogFragment() {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private var columnCount = 1

    companion object {
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 1

        // This callback could also be private and be set on the host(main) Activity
        var onCalendarEventClickListener: OnCalendarEventClickListener? = null

    //The colors available on the google calendar app with their corresponding int value
         val googleCalendarColorMap : Map<String, String> = mapOf(
            "Default" to "-6299161",
            "Tomato" to "-2350809",
            "Tangerine" to "-18312",
            "Banana" to "-272549",
            "Basil" to "-11421879",
            "Sage" to "-8722497",
            "Peacock" to "-12134693",
            "Blueberry" to "-11238163",
            "Lavender" to "-5980676",
            "Grape" to "-2380289",
            "Flamingo" to "-30596",
            "Graphite" to "-1973791"
        )
        var selectedColors = BooleanArray(googleCalendarColorMap.size)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCalendarPermission()
      calendarViewModel = ViewModelProviders.of(this)
          .get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       val root = inflater.inflate(R.layout.calendar_fragment, container, false)

        calendarViewModel.init()
        val textView: TextView = root.findViewById(R.id.text_calendar)
        calendarViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        recyclerView = root.findViewById(R.id.calendar_recycler_view)

      val selectCalendarButton: Button = root.findViewById(R.id.select_calendar_button)
        selectCalendarButton.setOnClickListener {
            showDialog()
        }

        calendarViewModel.getCalendars().observe(viewLifecycleOwner , Observer {

            //On Change here. Whenever the data changes the recycler view will be updated
            updateRecyclerView()
            recyclerView.adapter!!.notifyDataSetChanged()

        })
      return root
    }

    private fun showDialog() {

        val dialog = CalendarFragment()
        dialog.setTargetFragment(this, targetRequestCode)
        dialog.show(fragmentManager!!, "select calendars dialog")

    }

    private fun requestCalendarPermission() {

        if (checkSelfPermission(this.context!!, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR),
                READ_CALENDAR_PERMISSION_REQUEST_CODE
            )
    }

    private fun updateRecyclerView() {

        val events: ArrayList<CalendarEvent> = arrayListOf()
        for (cal in calendarViewModel.getCalendars().value!!)
            events.addAll(cal.events)
        events.sortWith(compareBy { it.startTime })

        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = CalendarAdapter(events, onCalendarEventClickListener)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->

            val selectedBool = loadDialog()
            val colorArray = googleCalendarColorMap.keys.toTypedArray()

            val builder = AlertDialog.Builder(it)
                .setTitle("Select Your Calendars")
                .setMultiChoiceItems(colorArray, selectedBool
                ) { _, which, isChecked ->
                    selectedBool!![which] = isChecked
                }.setPositiveButton(R.string.ok) { _, _ ->

                    saveDialog(selectedBool!!)
                    targetFragment!!.onActivityResult(targetRequestCode, 1, null)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface OnCalendarEventClickListener {
        fun onCalendarEventClick(item: CalendarEvent?)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        calendarViewModel.selectCalendars(selectedColors)

    }

    private fun saveDialog(checkedArr: BooleanArray) {
        selectedColors = checkedArr
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        for (i in checkedArr.indices) {
            editor?.putBoolean(i.toString(), checkedArr[i])
        }
        editor?.apply()
    }

    private fun loadDialog(): BooleanArray? {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val checkedArr = BooleanArray(googleCalendarColorMap.size)
        for (i in checkedArr.indices) {
            checkedArr[i] = sharedPreferences!!.getBoolean(i.toString(), false)
        }
        selectedColors = checkedArr
        return checkedArr
    }
}
