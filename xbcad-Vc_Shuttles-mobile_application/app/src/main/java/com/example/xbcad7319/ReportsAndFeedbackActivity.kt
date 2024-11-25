package com.example.xbcad7319

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportsAndFeedbackActivity : AppCompatActivity() {

    private lateinit var lvReports: ListView
    private var reportsList = ArrayList<Report>()  // Report model class
    private lateinit var adapter: ReportAdapter  // Adapter for displaying reports in the ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_and_feedback)

        lvReports = findViewById(R.id.lvReports)
        adapter = ReportAdapter(this, reportsList)
        lvReports.adapter = adapter

        fetchUserReports()
    }

    private fun fetchUserReports() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ReportsAndFeedbackActivity", "User ID is null, cannot fetch reports.")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ReportsAndFeedbackActivity", "Fetching reports for user ID: $userId")

        FirebaseFirestore.getInstance().collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                reportsList.clear()

                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    Log.d("ReportsAndFeedbackActivity", "Checking schedule ID: $scheduleId for user reports")

                    FirebaseFirestore.getInstance()
                        .collection("schedules")
                        .document(scheduleId)
                        .collection("reports")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { reportsSnapshot ->
                            for (reportDocument in reportsSnapshot.documents) {
                                val report = Report(
                                    id = reportDocument.id,
                                    date = reportDocument.getString("date") ?: "Unknown date",
                                    category = reportDocument.getString("category") ?: "General",
                                    content = reportDocument.getString("content") ?: "No content",
                                    rating = reportDocument.getLong("rating")?.toInt() ?: 0
                                )

                                reportsList.add(report)
                                Log.d("ReportsAndFeedbackActivity", "Added report for schedule ID: $scheduleId with content: ${report.content}")
                            }
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ReportsAndFeedbackActivity", "Error fetching reports for schedule $scheduleId", e)
                        }
                }

                Log.d("ReportsAndFeedbackActivity", "Total reports fetched: ${reportsList.size}")
            }
            .addOnFailureListener { e ->
                Log.e("ReportsAndFeedbackActivity", "Error fetching schedules", e)
            }
    }
}
