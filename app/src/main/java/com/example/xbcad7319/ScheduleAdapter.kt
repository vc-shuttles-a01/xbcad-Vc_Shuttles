package com.example.xbcad7319

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ScheduleAdapter(context: Context, schedules: List<String>) :
    ArrayAdapter<String>(context, 0, schedules) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_schedule, parent, false)
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvDetails = itemView.findViewById<TextView>(R.id.tvDetails)

        val schedule = getItem(position)
        val parts = schedule!!.split(", ")
        if (parts.size > 1) {
            tvDate.text = parts[0]  // Assumes the date is the first part
            tvDetails.text = parts.drop(1).joinToString(", ")  // Join remaining parts for other details
        }

        return itemView
    }
}
