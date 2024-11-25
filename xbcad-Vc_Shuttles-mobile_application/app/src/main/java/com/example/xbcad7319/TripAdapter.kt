package com.example.xbcad7319

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TripAdapter(
    private val context: Context,
    private val bookingList: ArrayList<Booking>
) : ArrayAdapter<Booking>(context, 0, bookingList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val booking = getItem(position)!!
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_trip, parent, false)

        //val tvBookingId = view.findViewById<TextView>(R.id.tvBookingId)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvBusNumber = view.findViewById<TextView>(R.id.tvBusNumber)
        val tvDirection = view.findViewById<TextView>(R.id.tvDirection)
        val tvSeats = view.findViewById<TextView>(R.id.tvSeats)

        // Set data to views
        //tvBookingId.text = "Booking ID: ${booking.id}"
        tvDate.text = "Date: ${booking.date}"
        tvTime.text = "Time: ${booking.time}"
        tvBusNumber.text = "Bus Number: ${booking.busNumber}"
        tvDirection.text = "Direction: ${booking.direction}"
        tvSeats.text = "Seats: ${booking.seats}"

        return view
    }
}
