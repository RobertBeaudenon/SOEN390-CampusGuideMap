package com.droidhats.campuscompass.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.droidhats.campuscompass.MainActivity
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.CalendarAdapter
import com.droidhats.campuscompass.models.CalendarEvent
import com.droidhats.campuscompass.viewmodels.CalendarViewModel
import com.droidhats.campuscompass.viewmodels.CalendarViewModel.Companion.GOOGLE_CALENDAR_COLOR_MAP

class CalendarFragment : DialogFragment(), DialogInterface.OnDismissListener {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private var columnCount = 1

    companion object {
        // This callback could also be private and be set on the host(main) Activity
        var onCalendarEventClickListener: OnCalendarEventClickListener? = null
        var isDialogOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      calendarViewModel = ViewModelProviders.of(this)
          .get(CalendarViewModel::class.java)

        if(!(activity as MainActivity).checkCalendarPermission()) {
            (activity as MainActivity).requestCalendarPermission()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val root = inflater.inflate(R.layout.calendar_fragment, container, false)
        refresh()
        val textView: TextView = root.findViewById(R.id.text_info)
        calendarViewModel.info.observe(viewLifecycleOwner, Observer {
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

        val swipeLayout : SwipeRefreshLayout = root.findViewById(R.id.swipe_container)
        swipeLayout.setOnRefreshListener {
            refresh()
            swipeLayout.isRefreshing = false
        }
      return root
    }

     fun showDialog() {
        if (isDialogOpen) return
        val dialog = CalendarFragment()
        dialog.setTargetFragment(this, targetRequestCode)
        dialog.show(requireFragmentManager(), "select calendars dialog")
         isDialogOpen = true
    }

     fun refresh() {
        calendarViewModel.init()
        loadChecked()
        calendarViewModel.selectCalendars()
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
            val selectedBool = loadChecked()
            val colorArray = GOOGLE_CALENDAR_COLOR_MAP.keys.toTypedArray()

            val builder = AlertDialog.Builder(it)
                .setTitle("Select Your Calendars")
                .setMultiChoiceItems(colorArray, selectedBool
                ) { _, which, isChecked ->
                    selectedBool!![which] = isChecked
                }.setPositiveButton("OK") { _, _ ->
                    saveChecked(selectedBool!!)
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
        loadChecked()
        calendarViewModel.selectCalendars()
    }

    private fun saveChecked(checkedArr: BooleanArray) {
        calendarViewModel.selectedColors = checkedArr
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        for (i in checkedArr.indices) {
            editor?.putBoolean(i.toString(), checkedArr[i])
        }
        editor?.apply()
    }

    private fun loadChecked(): BooleanArray? {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val checkedArr = BooleanArray(GOOGLE_CALENDAR_COLOR_MAP.size)
        for (i in checkedArr.indices) {
            checkedArr[i] = sharedPreferences!!.getBoolean(i.toString(), false)
        }
        calendarViewModel.selectedColors = checkedArr
        return checkedArr
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isDialogOpen = false
    }
}
