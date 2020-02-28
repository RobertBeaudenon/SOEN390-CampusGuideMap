package com.droidhats.campuscompass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.droidhats.campuscompass.views.CalendarFragment
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.CalendarEvent
import kotlinx.android.synthetic.main.calendar_recycler_item.view.*
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarAdapter(
    private val items: ArrayList<CalendarEvent>,
    private val listener: CalendarFragment.OnCalendarEventClickListener?
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { view ->
            val item = view.tag as CalendarEvent
            // Notify the activity/fragment that an item has been clicked
            listener?.onCalendarEventClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.titleView.text = item.title

        val date = formatDateTime(item.startTime!!, "E dd MMMM yyyy")
        val startTime = formatDateTime(item.startTime!!, "hh:mm")
        val endTime = formatDateTime(item.endTime!!, "hh:mm a")
        val time = "$startTime - $endTime"
        val dateTime = "$date  $time"
        holder.dateView.text = dateTime

        holder.locationView.text = if (item.location.isNullOrBlank()) "None" else item.location

        holder.cardView.setCardBackgroundColor(item.color!!.toInt())

        with(holder.view) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    private fun formatDateTime(dateTime: String, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.CANADA)
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = dateTime.toLong()
        return formatter.format(calendar.time)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView: TextView = view.event_title_item
        val dateView: TextView = view.event_date_item
        val locationView: TextView = view.event_location_item
        var cardView: CardView = view.findViewById(R.id.calendar_card_view)

    }

}