package com.example.xbcad7319

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class AdminReportsActivity : AppCompatActivity() {

    private lateinit var ridershipBarChart: BarChart
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var reportsAdapter: ReportsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reports)

        ridershipBarChart = findViewById(R.id.ridershipBarChart)
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView)
        reportsAdapter = ReportsAdapter()
        reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        reportsRecyclerView.adapter = reportsAdapter

        setupBarChart()
        fetchRidershipData()
        fetchPeakHoursData()
        fetchRouteEfficiencyData()
    }

    private fun setupBarChart() {
        ridershipBarChart.setFitBars(true)
        ridershipBarChart.description = Description().apply { text = "Weekly Ridership Trends" }
        ridershipBarChart.animateY(1000)
    }

    private fun fetchRidershipData() {
        val todayDate = dateFormat.format(Date())
        val lastWeekDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        val lastWeekFormatted = dateFormat.format(lastWeekDate)

        db.collection("schedules")
            .whereGreaterThanOrEqualTo("date", lastWeekFormatted)
            .whereLessThanOrEqualTo("date", todayDate)
            .get()
            .addOnSuccessListener { documents ->
                val dailyRides = mutableMapOf<String, Int>()
                documents.forEach { document ->
                    val date = document.getString("date") ?: return@forEach
                    val seats = document.getLong("totalSeatsBooked")?.toInt() ?: 0
                    dailyRides[date] = dailyRides.getOrDefault(date, 0) + seats
                }
                populateBarChart(dailyRides)
            }
            .addOnFailureListener { e ->
                Log.e("AdminReports", "Error fetching ridership data", e)
            }
    }

    private fun populateBarChart(dailyRides: Map<String, Int>) {
        val sortedDates = dailyRides.keys.sorted() // Ensure dates are in ascending order
        val barEntries = sortedDates.mapIndexed { index, date ->
            BarEntry(index.toFloat(), dailyRides[date]?.toFloat() ?: 0f)
        }
        val dataSet = BarDataSet(barEntries, "Rides").apply {
            setColors(*ColorTemplate.MATERIAL_COLORS)
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        ridershipBarChart.data = barData

        // Configure X-axis to not display dates
        ridershipBarChart.xAxis.apply {
            setDrawLabels(false) // Hide X-axis labels
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
        }

        // Adjust appearance of the chart
        ridershipBarChart.axisLeft.axisMinimum = 0f // Ensure Y-axis starts at 0
        ridershipBarChart.axisRight.isEnabled = false // Disable right Y-axis
        ridershipBarChart.legend.isEnabled = true // Keep legend for context
        ridershipBarChart.description.isEnabled = false // Disable description
        ridershipBarChart.extraBottomOffset = 10f // Add some padding

        ridershipBarChart.invalidate() // Refresh chart

        // Populate custom legend below the chart
        populateCustomLegend(sortedDates, dailyRides, dataSet.colors)
    }

    private fun populateCustomLegend(dates: List<String>, dailyRides: Map<String, Int>, colors: List<Int>) {
        val legendContainer: LinearLayout = findViewById(R.id.legendContainer)
        legendContainer.removeAllViews() // Clear previous legend items

        dates.forEachIndexed { index, date ->
            val rideCount = dailyRides[date] ?: 0
            val color = colors[index % colors.size] // Cycle through colors if fewer than entries

            val legendItem = layoutInflater.inflate(R.layout.legend_item, legendContainer, false)

            // Customize legend item
            val colorBox: View = legendItem.findViewById(R.id.colorBox)
            val dateText: TextView = legendItem.findViewById(R.id.dateText)
            val rideText: TextView = legendItem.findViewById(R.id.rideText)

            colorBox.setBackgroundColor(color)
            dateText.text = date
            rideText.text = "Rides: $rideCount"

            legendContainer.addView(legendItem)
        }
    }








    private fun fetchPeakHoursData() {
        Log.d("AdminReports", "Fetching peak hours data")

        db.collection("schedules")
            .get()
            .addOnSuccessListener { documents ->
                val hoursMap = mutableMapOf<String, Int>()

                documents.forEach { document ->
                    val time = document.getString("time") ?: "Unknown time"
                    Log.d("AdminReports", "Document ID: ${document.id}, Time: $time")
                    hoursMap[time] = hoursMap.getOrDefault(time, 0) + 1
                }

                val peakHour = hoursMap.maxByOrNull { it.value }?.key ?: "No data"
                Log.d("AdminReports", "Peak hour determined: $peakHour with ${hoursMap[peakHour]} rides")
                reportsAdapter.addReportItem("Peak Hour", peakHour)
            }
            .addOnFailureListener { e ->
                Log.e("AdminReports", "Error fetching peak hours data", e)
            }
    }

    private fun fetchRouteEfficiencyData() {
        Log.d("AdminReports", "Fetching route efficiency data")

        db.collection("routes")
            .get()
            .addOnSuccessListener { documents ->
                var totalDistance = 0.0
                var totalTime = 0.0

                documents.forEach { document ->
                    val distance = document.getDouble("distance") ?: 0.0
                    val time = document.getDouble("time") ?: 0.0
                    Log.d("AdminReports", "Document ID: ${document.id}, Distance: $distance, Time: $time")
                    totalDistance += distance
                    totalTime += time
                }

                val averageTimePerRoute = if (documents.size() > 0) totalTime / documents.size() else 0.0
                val averageDistancePerRoute = if (documents.size() > 0) totalDistance / documents.size() else 0.0

                Log.d("AdminReports", "Average route time: $averageTimePerRoute")
                Log.d("AdminReports", "Average route distance: $averageDistancePerRoute")

                reportsAdapter.addReportItem("Avg Time Per Route", "$averageTimePerRoute mins")
                reportsAdapter.addReportItem("Avg Distance Per Route", "$averageDistancePerRoute km")
            }
            .addOnFailureListener { e ->
                Log.e("AdminReports", "Error fetching route efficiency data", e)
            }
    }
}
