package com.example.xbcad7319

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportsAdapter : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    private val reports = mutableListOf<Pair<String, String>>()

    fun addReportItem(title: String, value: String) {
        reports.add(title to value)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.reportTitle.text = report.first
        holder.reportValue.text = report.second
    }

    override fun getItemCount(): Int = reports.size

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reportTitle: TextView = view.findViewById(R.id.reportTitle)
        val reportValue: TextView = view.findViewById(R.id.reportValue)
    }
}
