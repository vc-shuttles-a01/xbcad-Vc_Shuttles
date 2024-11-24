package com.example.xbcad7319

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xbcad7319.databinding.ActivityScheduleBinding
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView



@Suppress("DEPRECATION")
class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private var shuttleTimes: List<ShuttleSchedule> = listOf() // Initialize as an empty list

    // Map for shuttle schedules, includes both time and direction
    private val allShuttleTimes = mapOf(
        "2024-09-12" to listOf(
            ShuttleSchedule("10:30 AM", "To Campus"),
            ShuttleSchedule("11:00 AM", "From Campus"),
            ShuttleSchedule("12:00 PM", "To Campus")
        ),
        "2024-09-13" to listOf(
            ShuttleSchedule("9:00 AM", "To Campus"),
            ShuttleSchedule("9:30 AM", "From Campus"),
            ShuttleSchedule("11:00 AM", "To Campus"),
            ShuttleSchedule("12:30 PM", "From Campus")
        ),
        "2024-09-14" to listOf(
            ShuttleSchedule("11:00 AM", "To Campus"),
            ShuttleSchedule("12:00 PM", "From Campus"),
            ShuttleSchedule("2:00 PM", "To Campus")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView for displaying the shuttle schedule
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set onClickListener to open DatePickerDialog
        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        // BottomNavigationView setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Redirect to LandingPage (current activity)
                    val intent = Intent(this, LandingPage::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_schedule -> {
                    // Redirect to ScheduleActivity
                    val intent = Intent(this, ScheduleActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_settings -> {
                    // Redirect to SettingsActivity - FIX HERE
                    val intent = Intent(this, LandingPage::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

    }

    private fun showDatePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "${selectedYear}-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}"
            displayShuttleScheduleForDate(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun displayShuttleScheduleForDate(date: String) {
        // Get shuttle times for the selected date, if available
        shuttleTimes = allShuttleTimes[date] ?: listOf() // If no times, empty list

        // Show a message if no shuttles are available
        if (shuttleTimes.isEmpty()) {
            Toast.makeText(this, "No shuttle rides available for $date", Toast.LENGTH_SHORT).show()
        } else {
            val ridesAvailable = shuttleTimes.size
            Toast.makeText(this, "$ridesAvailable shuttle rides available for $date", Toast.LENGTH_SHORT).show()
        }

        // Update RecyclerView to show the shuttle times with directions
        val adapter = ShuttleScheduleAdapter(shuttleTimes)
        binding.scheduleRecyclerView.adapter = adapter
    }



}
