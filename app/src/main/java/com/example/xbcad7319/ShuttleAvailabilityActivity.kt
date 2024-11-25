package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ShuttleAvailabilityActivity : AppCompatActivity() {
    private lateinit var rvShuttleRides: RecyclerView
    private lateinit var backButton: Button
    private lateinit var adapter: ShuttleRidesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedDate: String = dateFormat.format(Date()) // Default to today's date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuttle_availability)
        Log.d("ShuttleAvailability", "Activity created, initializing components.")

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView and adapter
        rvShuttleRides = findViewById(R.id.rvShuttleRides)
        backButton = findViewById(R.id.backButton)
        adapter = ShuttleRidesAdapter()
        rvShuttleRides.layoutManager = LinearLayoutManager(this)
        rvShuttleRides.adapter = adapter
        Log.d("ShuttleAvailability", "RecyclerView and adapter initialized.")

        // Set up DatePicker listener
        val datePicker: DatePicker = findViewById(R.id.datePicker)
        datePicker.init(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            Log.d("ShuttleAvailability", "Date selected: $selectedDate")
            fetchShuttleRidesForDate(selectedDate)
        }

        // Fetch initial data for today's date
        Log.d("ShuttleAvailability", "Fetching shuttle rides for today's date: $selectedDate")
        fetchShuttleRidesForDate(selectedDate)

        backButton.setOnClickListener(){
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }

    }

    private fun fetchShuttleRidesForDate(date: String) {
        Log.d("ShuttleAvailability", "Fetching shuttle rides for date: $date")

        db.collection("schedules")
            .get()
            .addOnSuccessListener { documents ->
                val shuttleRides = mutableListOf<ShuttleRides>()

                if (documents.isEmpty) {
                    Log.d("ShuttleAvailability", "No documents found.")
                } else {
                    Log.d("ShuttleAvailability", "Documents found: ${documents.size()}")
                }

                // Process each schedule document
                for (doc in documents) {
                    val dateString = doc.getString("date") ?: continue
                    val parsedDate = parseDate(dateString)

                    if (parsedDate == date) {
                        Log.d("ShuttleAvailability", "Processing document ID: ${doc.id}")

                        // Fetch the bookings subcollection to calculate total seats
                        db.collection("schedules").document(doc.id).collection("bookings")
                            .get()
                            .addOnSuccessListener { bookingsSnapshot ->
                                val totalSeatsBooked = bookingsSnapshot.sumOf { it.getLong("seats") ?: 0 }
                                Log.d("ShuttleAvailability", "Total booked seats for schedule ${doc.id}: $totalSeatsBooked")

                                // Create the ShuttleRides object with total seats booked
                                val shuttleRide = ShuttleRides(
                                    id = doc.id,
                                    date = parsedDate ?: "Unknown date",
                                    time = doc.getString("time") ?: "Unknown time",
                                    seats = totalSeatsBooked.toString(),
                                    direction = doc.getString("direction") ?: "No direction"
                                )
                                shuttleRides.add(shuttleRide)
                                adapter.submitList(shuttleRides)
                            }
                            .addOnFailureListener { e ->
                                Log.e("ShuttleAvailability", "Error fetching bookings for schedule ${doc.id}", e)
                            }
                    } else {
                        Log.d("ShuttleAvailability", "Skipping document ID ${doc.id} as its date $parsedDate does not match selected date $date")
                    }
                }

                // Display an empty list if no matching entries were found
                if (shuttleRides.isEmpty()) {
                    adapter.submitList(emptyList())
                    Log.d("ShuttleAvailability", "No matching entries for selected date: $date, displaying empty list.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShuttleAvailability", "Error fetching shuttle rides", e)
            }
    }

    /**
     * Parses a Firestore date string and reformats it to match "dd/MM/yyyy".
     * Supports multiple input formats.
     */
    private fun parseDate(dateString: String): String? {
        val inputFormats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()), // Expected format
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // ISO format
        )

        for (format in inputFormats) {
            try {
                val date = format.parse(dateString)
                return dateFormat.format(date!!) // Reformat to "dd/MM/yyyy"
            } catch (e: ParseException) {
                // Continue to the next format
            }
        }

        Log.e("ShuttleAvailability", "Unparseable date: $dateString")
        return null
    }
}
