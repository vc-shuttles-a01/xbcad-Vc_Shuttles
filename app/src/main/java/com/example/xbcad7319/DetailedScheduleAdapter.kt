package com.example.xbcad7319

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class DetailedSchedule(
    val id: String,
    val date: String,
    val time: String,
    val busNumber: String,
    val direction: String,
    val bookedSeats: Int
)



class DetailedScheduleAdapter(
    context: Context,
    private val schedules: List<DetailedSchedule>
) : ArrayAdapter<DetailedSchedule>(context, 0, schedules) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false)

        // Bind each schedule's details to the views in `detailed_schedule_item.xml`
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val tvBusNumber = itemView.findViewById<TextView>(R.id.tvBusNumber)
        val tvDirection = itemView.findViewById<TextView>(R.id.tvDirection)
        val tvBookedSeats = itemView.findViewById<TextView>(R.id.tvSeats)

        val schedule = schedules[position]
        tvDate.text = "Date: ${schedule.date}"
        tvTime.text = "Time: ${schedule.time}"
        tvBusNumber.text = "Bus: ${schedule.busNumber}"
        tvDirection.text = "Direction: ${schedule.direction}"
        tvBookedSeats.text = "Booked Seats: ${schedule.bookedSeats}"

        return itemView
    }
}
