package com.droidhats.campuscompass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class CalendarFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel

    companion object{
        private const val READ_CALENDAR_PERMISSION_REQUEST_CODE = 1
        private const val WRITE_CALENDAR_PERMISSION_REQUEST_CODE = 2
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

        val root = inflater.inflate(R.layout.calendar_fragement, container, false)

        val textView: TextView = root.findViewById(R.id.text_calendar)
        calendarViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        //Testing the calendar results
        val availableCalenders : TextView = root.findViewById(R.id.available_calendars)
        calendarViewModel.getUserCalendars().observe(viewLifecycleOwner, Observer {
            var calendarsText = ""
            for ( i in it )
            {
                calendarsText += i
                calendarsText += "\n\n"
                availableCalenders.text = calendarsText
            }
        })
        return root
    }

    private fun requestCalendarPermission(){

        if (checkSelfPermission(this.context!!, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), READ_CALENDAR_PERMISSION_REQUEST_CODE)
    }
}
