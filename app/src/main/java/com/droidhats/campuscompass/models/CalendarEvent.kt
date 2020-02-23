package com.droidhats.campuscompass.models

class CalendarEvent(
    event_title: String?,
    event_location: String?,
    event_start_time: String?,
    event_end_time: String?,
    event_description: String?,
    event_color: String?
) {

    var title: String? = event_title
    var location: String? = event_location
    var startTime: String? = event_start_time
    var endTime: String? = event_end_time
    var description: String? = event_description
    var color: String? = event_color

    override fun toString(): String {

        return "Event: $title" +
                "\nLocation: $location" +
                "\nStart Time: $startTime" +
                "\nEnd Time: $endTime" +
                "\nDescription: $description" +
                "\nColor $color"
    }
}