package com.example.xbcad7319

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ScheduleManagementActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var datePickerButton: Button
    private lateinit var timePickerButton: Button
    private lateinit var busSpinner: Spinner
    private lateinit var directionSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var backButton: Button
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_management)

        firestore = FirebaseFirestore.getInstance()
        datePickerButton = findViewById(R.id.datePickerButton)
        timePickerButton = findViewById(R.id.timePickerButton)
        busSpinner = findViewById(R.id.busSpinner)
        directionSpinner = findViewById(R.id.directionSpinner)
        submitButton = findViewById(R.id.submitButton)
        backButton = findViewById(R.id.backButton)

        setupBusSpinner()
        setupDirectionSpinner()
        setupDatePicker()
        setupTimePicker()

        submitButton.setOnClickListener {
            submitSchedule()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, AdminLandingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBusSpinner() {
        val busOptions = arrayOf("Bus 1", "Bus 2", "Bus 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, busOptions)
        busSpinner.adapter = adapter
    }

    private fun setupDirectionSpinner() {
        val directions = arrayOf("To Campus from Station", "To Station from Campus")
        val directionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, directions)
        directionSpinner.adapter = directionAdapter
    }

    private fun setupDatePicker() {
        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = "${dayOfMonth}/${month + 1}/$year"
                datePickerButton.text = "Date: $selectedDate"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupTimePicker() {
        timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedTime = "${hourOfDay}:${minute.toString().padStart(2, '0')}"
                timePickerButton.text = "Time: $selectedTime"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun submitSchedule() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select both date and time.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "No authenticated user found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val bus = busSpinner.selectedItem.toString()
        val direction = directionSpinner.selectedItem.toString()
        val scheduleData = hashMapOf(
            "date" to selectedDate,
            "time" to selectedTime,
            "bus" to bus,
            "direction" to direction,
            "totalSeatsBooked" to 0, // initialize with 0 seats booked
            "adminId" to user.uid
        )

        firestore.collection("schedules")
            .add(scheduleData)
            .addOnSuccessListener { documentRef ->
                val scheduleId = documentRef.id
                Toast.makeText(this, "Schedule submitted successfully with ID: $scheduleId", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit schedule: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
