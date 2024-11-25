package com.example.xbcad7319

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ArrayAdapter

class ReportAdapter(context: Context, private val reports: List<Report>) :
    ArrayAdapter<Report>(context, R.layout.report_list_item, reports) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.report_list_item, parent, false)

        val report = reports[position]
        val reportDate = view.findViewById<TextView>(R.id.reportDate)
        val reportCategory = view.findViewById<TextView>(R.id.reportCategory)
        val reportContent = view.findViewById<TextView>(R.id.reportContent)
        val reportRating = view.findViewById<RatingBar>(R.id.reportRating)

        reportDate.text = report.date
        reportCategory.text = report.category
        reportContent.text = report.content
        reportRating.rating = report.rating.toFloat()

        return view
    }
}
