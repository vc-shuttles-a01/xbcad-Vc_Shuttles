
package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var lvBookings: ListView
    private var bookingsList = ArrayList<Booking>()
    private lateinit var adapter: BookingAdapter
    private lateinit var backButton: Button
    private lateinit var sortButton: Button
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        lvBookings = findViewById(R.id.lvBookings)
        adapter = BookingAdapter(this, bookingsList) { booking, action ->
            when (action) {
                BookingAdapter.Action.COMPLETE -> completeBooking(booking)
                BookingAdapter.Action.DELETE -> deleteBooking(booking)
            }
        }
        lvBookings.adapter = adapter

        sortButton = findViewById(R.id.sortButton)
        sortButton.setOnClickListener {
            sortBookings()
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }

        fetchUserBookings()
    }

    private fun fetchUserBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("MyBookingsActivity", "User ID is null, cannot fetch bookings.")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MyBookingsActivity", "Fetching bookings for user ID: $userId")

        FirebaseFirestore.getInstance().collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                bookingsList.clear()

                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    Log.d("MyBookingsActivity", "Checking schedule ID: $scheduleId for user bookings")

                    FirebaseFirestore.getInstance()
                        .collection("schedules")
                        .document(scheduleId)
                        .collection("bookings")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { bookingsSnapshot ->
                            if (bookingsSnapshot.isEmpty) {
                                Log.d("MyBookingsActivity", "No bookings found for schedule ID: $scheduleId and user ID: $userId")
                            } else {
                                for (bookingDocument in bookingsSnapshot.documents) {
                                    val seats = bookingDocument.getLong("seats")?.toString() ?: "N/A"
                                    val bookingCode = bookingDocument.id // Fetch the Booking ID as Booking Code

                                    val booking = Booking(
                                        id = bookingCode, // Assign Booking ID as Booking Code
                                        scheduleId = scheduleId,
                                        date = scheduleDocument.getString("date") ?: "Unknown date",
                                        time = scheduleDocument.getString("time") ?: "Unknown time",
                                        seats = seats,
                                        direction = scheduleDocument.getString("direction") ?: "No direction",
                                        busNumber = scheduleDocument.getString("bus") ?: "No bus number"
                                    )

                                    bookingsList.add(booking)
                                    Log.d("MyBookingsActivity", "Booking added: $booking")
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("MyBookingsActivity", "Error fetching bookings for schedule $scheduleId", e)
                        }
                }

                Log.d("MyBookingsActivity", "Total bookings fetched: ${bookingsList.size}")
            }
            .addOnFailureListener { e ->
                Log.e("MyBookingsActivity", "Error fetching schedules", e)
            }
    }

    private fun sortBookings() {
        Log.d("MyBookingsActivity", "Sorting bookings")
        bookingsList.sortByDescending {
            dateFormat.parse(it.date)
        }
        adapter.notifyDataSetChanged()
    }

    private fun completeBooking(booking: Booking) {
        Log.d("MyBookingsActivity", "Completing booking with ID: ${booking.id}")
        Toast.makeText(this, "Booking completed: Code: ${booking.id}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteBooking(booking: Booking) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val scheduleId = booking.scheduleId

        if (scheduleId == null) {
            Log.e("MyBookingsActivity", "Schedule ID is null for booking ID: ${booking.id}")
            Toast.makeText(this, "Error: Cannot delete booking without a valid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MyBookingsActivity", "Deleting booking with ID: ${booking.id} for schedule ID: $scheduleId")

        FirebaseFirestore.getInstance()
            .collection("schedules")
            .document(scheduleId)
            .collection("bookings")
            .document(booking.id)
            .delete()
            .addOnSuccessListener {
                bookingsList.remove(booking)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Booking deleted: Code: ${booking.id}", Toast.LENGTH_SHORT).show()
                Log.d("MyBookingsActivity", "Successfully deleted booking with ID: ${booking.id}")
            }
            .addOnFailureListener { e ->
                Log.e("MyBookingsActivity", "Error deleting booking with ID: ${booking.id}", e)
                Toast.makeText(this, "Error deleting booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
