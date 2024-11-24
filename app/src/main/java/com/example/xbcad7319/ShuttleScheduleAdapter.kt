package com.example.xbcad7319

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319.databinding.ItemShuttleScheduleBinding

class ShuttleScheduleAdapter(private val shuttleTimes: List<ShuttleSchedule>) :
    RecyclerView.Adapter<ShuttleScheduleAdapter.ShuttleScheduleViewHolder>() {

    inner class ShuttleScheduleViewHolder(val binding: ItemShuttleScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShuttleScheduleViewHolder {
        val binding = ItemShuttleScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShuttleScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShuttleScheduleViewHolder, position: Int) {
        val shuttleSchedule = shuttleTimes[position]
        holder.binding.shuttleTimeTextView.text = shuttleSchedule.time
        holder.binding.shuttleDirectionTextView.text = shuttleSchedule.direction
    }

    override fun getItemCount(): Int {
        return shuttleTimes.size
    }
}
