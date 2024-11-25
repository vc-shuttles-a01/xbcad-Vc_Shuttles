package com.example.xbcad7319

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ShuttleRide(
    val id: String,
    val date: String,
    val time: String,
    val seats: String,
    val direction: String
)

class ShuttleRidesAdapter : RecyclerView.Adapter<ShuttleRidesAdapter.ShuttleRideViewHolder>() {
    private val shuttleRides = mutableListOf<ShuttleRides>()

    fun submitList(rides: List<ShuttleRides>) {
        shuttleRides.clear()
        shuttleRides.addAll(rides)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleRideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shuttle_ride, parent, false)
        return ShuttleRideViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShuttleRideViewHolder, position: Int) {
        holder.bind(shuttleRides[position])
    }

    override fun getItemCount(): Int = shuttleRides.size

    class ShuttleRideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvSeats: TextView = itemView.findViewById(R.id.tvSeats)
        private val tvDirection: TextView = itemView.findViewById(R.id.tvDirection)

        fun bind(ride: ShuttleRides) {
            tvTime.text = "Time: ${ride.time}"
            tvSeats.text = "Seats: ${ride.seats}"
            tvDirection.text = "Direction: ${ride.direction}"
        }
    }
}
