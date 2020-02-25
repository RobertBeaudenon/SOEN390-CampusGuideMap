package com.droidhats.campuscompass.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.viewmodels.CalendarViewModel
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.adapters.CalendarAdapter
import com.droidhats.campuscompass.models.Calendar
import com.droidhats.campuscompass.models.CalendarEvent

class CalendarFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel

    private var columnCount = 1

    companion object {
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 1
        // This callback could also be private and be set on the host(main) Activity
        var onCalendarEventClickListener: OnCalendarEventClickListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCalendarPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        calendarViewModel =
            ViewModelProviders.of(this).get(CalendarViewModel::class.java)
        calendarViewModel.init()

        val root = inflater.inflate(R.layout.calendar_fragment, container, false)

        val textView: TextView = root.findViewById(R.id.text_calendar)
        calendarViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        initRecyclerView(root)
        return root
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

    private fun initRecyclerView(root: View) {

        val recyclerView: RecyclerView = root.findViewById(R.id.calendar_recycler_view)

        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }

            val calendars: List<Calendar> =
                calendarViewModel.getUserCalendars().value!!.values.toList()
            val events: ArrayList<CalendarEvent> = arrayListOf()

            for (cal in calendars) {
                events.addAll(cal.events)
            }
            events.sortWith(compareBy { it.startTime })
            adapter = CalendarAdapter(
                events,
                onCalendarEventClickListener
            )
        }
    }

    interface OnCalendarEventClickListener {
        fun onCalendarEventClick(item: CalendarEvent?)
    }
}
