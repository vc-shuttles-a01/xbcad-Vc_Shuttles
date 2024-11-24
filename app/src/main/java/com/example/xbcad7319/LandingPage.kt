package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xbcad7319.databinding.ActivityLandingPageBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LandingPage : AppCompatActivity() {

    private lateinit var binding: ActivityLandingPageBinding
    private val shuttleTimes = listOf("10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM")
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        // Set button actions
        setButtonActions()

        // Display the next available shuttle
        showNextAvailableShuttle()

        // Book shuttle on button click
        binding.btnAvailability.setOnClickListener {
            val intent = Intent(this, ShuttleAvailabilityActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyBookings.setOnClickListener(){
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }

        binding.btnBooking.setOnClickListener{
            val intent = Intent(this, BookingActivity::class.java)
            startActivity(intent)
        }

        binding.btnReports.setOnClickListener(){
            val intent = Intent(this, ReportsAndFeedbackActivity::class.java)
            startActivity(intent)
        }

        binding.btnProfile.setOnClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnNotifications.setOnClickListener(){
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        // Logout functionality
        binding.btnLogout.setOnClickListener {
            // Clear user data from SharedPreferences
            sharedPreferencesHelper.clearUser()

            // Show a logout confirmation message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to the LoginPg activity
            val intent = Intent(this, LoginPg::class.java)
            startActivity(intent)

            // Finish the current activity to prevent back navigation
            finish()
        }
    }

    private fun setButtonActions() {
        binding.btnSchedule.setOnClickListener {
            Toast.makeText(this, "Opening shuttle schedule...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyBookings.setOnClickListener {
            Toast.makeText(this, "Opening your bookings...", Toast.LENGTH_SHORT).show()
        }

        binding.btnTrackShuttle.setOnClickListener {
            Toast.makeText(this, "Tracking shuttle...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TrackShuttleActivity::class.java)
            startActivity(intent)
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Opening notifications...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNextAvailableShuttle() {
        // Get current time
        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        for (time in shuttleTimes) {
            val shuttleTime = sdf.parse(time)
            if (shuttleTime.after(currentTime)) {
                binding.nextShuttleTextView.text = time
                return
            }
        }

        // If no shuttle is available
        binding.nextShuttleTextView.text = "No Shuttles Available"
    }
}
