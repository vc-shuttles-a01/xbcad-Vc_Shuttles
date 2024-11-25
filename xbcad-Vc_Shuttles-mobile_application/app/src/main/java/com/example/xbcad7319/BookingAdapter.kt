package com.example.xbcad7319

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class BookingAdapter(
    context: Context,
    private val bookings: List<Booking>,
    private val onItemAction: (Booking, Action) -> Unit
) : ArrayAdapter<Booking>(context, 0, bookings) {

    enum class Action {
        COMPLETE, DELETE
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_booking, parent, false)

        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val tvBusNumber = itemView.findViewById<TextView>(R.id.tvBusNumber)
        val tvDirection = itemView.findViewById<TextView>(R.id.tvDirection)
        val tvSeats = itemView.findViewById<TextView>(R.id.tvSeats)
        val tvBookingCode = itemView.findViewById<TextView>(R.id.tvBookingCode)
        val btnComplete = itemView.findViewById<Button>(R.id.btnComplete)
        val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

        val booking = bookings[position]
        tvDate.text = "Date: ${booking.date}"
        tvTime.text = "Time: ${booking.time}"
        tvBusNumber.text = "Bus: ${booking.busNumber}"
        tvDirection.text = "Direction: ${booking.direction}"
        tvSeats.text = "Seats: ${booking.seats}"
        tvBookingCode.text = "Booking Code: ${booking.id}" // Display Booking Code

        btnComplete.setOnClickListener { onItemAction(booking, Action.COMPLETE) }
        btnDelete.setOnClickListener { onItemAction(booking, Action.DELETE) }

        return itemView
    }
}
