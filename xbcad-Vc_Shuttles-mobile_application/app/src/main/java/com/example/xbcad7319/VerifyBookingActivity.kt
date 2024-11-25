package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class VerifyBookingActivity : AppCompatActivity() {

    private lateinit var bookingCodeInput: EditText
    private lateinit var validateBookingButton: Button
    private lateinit var backButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_booking)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        Log.d("VerifyBookingActivity", "Firestore initialized")

        // Initialize Views
        bookingCodeInput = findViewById(R.id.bookingCodeInput)
        validateBookingButton = findViewById(R.id.validateBookingButton)
        backButton = findViewById(R.id.backButton)
        resultTextView = findViewById(R.id.resultTextView)

        // Set click listener for the validate button
        validateBookingButton.setOnClickListener {
            val bookingCode = bookingCodeInput.text.toString().trim()
            if (bookingCode.isNotEmpty()) {
                Log.d("VerifyBookingActivity", "Booking code input received: $bookingCode")
                validateBooking(bookingCode)
            } else {
                Toast.makeText(this, "Please enter a booking code", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for the back button
        backButton.setOnClickListener {
            startActivity(Intent(this, DriverLandingActivity::class.java))
        }
    }

    private fun validateBooking(bookingCode: String) {
        Log.d("VerifyBookingActivity", "Starting validation for booking code: $bookingCode")

        firestore.collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                Log.d("VerifyBookingActivity", "Fetched schedules. Document count: ${schedulesSnapshot.documents.size}")

                var bookingFound = false
                val scheduleCount = schedulesSnapshot.documents.size
                var processedSchedules = 0

                if (scheduleCount == 0) {
                    displayResult("Invalid Booking Code.", android.R.color.holo_red_dark)
                    return@addOnSuccessListener
                }

                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    Log.d("VerifyBookingActivity", "Checking schedule ID: $scheduleId for booking code: $bookingCode")

                    firestore.collection("schedules")
                        .document(scheduleId)
                        .collection("bookings")
                        .document(bookingCode)
                        .get()
                        .addOnSuccessListener { bookingDocument ->
                            processedSchedules++

                            if (bookingDocument.exists()) {
                                bookingFound = true
                                Log.d("VerifyBookingActivity", "Booking found for code: $bookingCode in schedule ID: $scheduleId")

                                val used = bookingDocument.getBoolean("used") ?: false
                                if (used) {
                                    Log.w("VerifyBookingActivity", "Booking code $bookingCode already marked as used")
                                    displayResult("Booking already used.", android.R.color.holo_red_dark)
                                } else {
                                    markBookingAsUsed(scheduleId, bookingCode)
                                    displayResult("Booking is valid.", android.R.color.holo_green_dark)
                                }
                            }

                            if (processedSchedules == scheduleCount && !bookingFound) {
                                Log.w("VerifyBookingActivity", "No matching booking found for code: $bookingCode")
                                displayResult("Invalid Booking Code.", android.R.color.holo_red_dark)
                            }
                        }
                        .addOnFailureListener { e ->
                            processedSchedules++
                            Log.e("VerifyBookingActivity", "Error checking booking: ${e.localizedMessage}", e)

                            if (processedSchedules == scheduleCount && !bookingFound) {
                                displayResult("Invalid Booking Code.", android.R.color.holo_red_dark)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("VerifyBookingActivity", "Error fetching schedules: ${e.localizedMessage}", e)
                displayResult("Error validating booking. Try again.", android.R.color.holo_red_dark)
            }
    }


    private fun markBookingAsUsed(scheduleId: String, bookingCode: String) {
        firestore.collection("schedules")
            .document(scheduleId)
            .collection("bookings")
            .document(bookingCode)
            .update("used", true)
            .addOnSuccessListener {
                Log.d("VerifyBookingActivity", "Booking successfully marked as used: $bookingCode")
            }
            .addOnFailureListener { e ->
                Log.e("VerifyBookingActivity", "Failed to mark booking as used: ${e.localizedMessage}", e)
                displayResult("Failed to mark booking as used.", android.R.color.holo_red_dark)
            }
    }

    private fun displayResult(message: String, colorId: Int) {
        resultTextView.text = message
        resultTextView.setTextColor(ContextCompat.getColor(this, colorId))
        resultTextView.visibility = View.VISIBLE
    }
}
