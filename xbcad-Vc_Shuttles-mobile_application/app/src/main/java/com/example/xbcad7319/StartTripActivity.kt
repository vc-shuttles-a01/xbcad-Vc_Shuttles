package com.example.xbcad7319

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class StartTripActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var bookingList = ArrayList<Booking>()
    private lateinit var adapter: TripAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val TAG = "StartTripActivity"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_trip)

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // Initialize location client

        Log.d(TAG, "Activity created, initializing ListView")
        listView = findViewById(R.id.tripListView)
        adapter = TripAdapter(this, bookingList)
        listView.adapter = adapter

        Log.d(TAG, "Fetching bookings for today")
        fetchBookingsForToday()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedBooking = bookingList[position]
            Log.d(TAG, "Selected booking: ${selectedBooking.id}")
            checkTripStatusAndStart(selectedBooking)
        }

        val endTripButton = findViewById<Button>(R.id.endTripButton)
        endTripButton.setOnClickListener {
            /*TrackShuttleActivity.resetMapFromOtherActivity()
            DriverTrackShuttleActivity.resetMapFromOtherActivity()*/
            onEndTripClicked(it)
        }
    }

    private fun fetchBookingsForToday() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated.")
            showSnackbar("User is not authenticated.")
            return
        }

        // Get the current date formatted as "dd/MM/yyyy"
        val currentDate = dateFormat.format(Date())
        Log.d(TAG, "Current date for fetching bookings: $currentDate")

        // Check if the user has admin privileges
        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val isAdmin = result.claims["isAdmin"] as Boolean? ?: false
            val isDriver = result.claims["isDriver"] as Boolean? ?: false

            val query = if (isAdmin || isDriver) {
                Log.d(TAG, "User is an admin or driver, fetching all bookings for today.")
                firestore.collection("schedules")
            } else {
                Log.d(TAG, "User is not an admin, fetching only their bookings for today.")
                firestore.collection("users").document(currentUser.uid).collection("bookings")
            }

            query.get()
                .addOnSuccessListener { schedulesSnapshot ->
                    bookingList.clear()
                    for (scheduleDocument in schedulesSnapshot.documents) {
                        val scheduleId = scheduleDocument.id
                        val dateString = scheduleDocument.getString("date")

                        // Parse and reformat the date from Firestore to match the format of `currentDate`
                        val formattedFirestoreDate = dateString?.let {
                            try {
                                val parsedDate = dateFormat.parse(it)
                                dateFormat.format(parsedDate)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing date for document ID $scheduleId: ${e.message}")
                                null
                            }
                        }

                        if (formattedFirestoreDate == currentDate) {
                            // Fetch booking details only if the dates match
                            val time = scheduleDocument.getString("time") ?: "Unknown time"
                            val direction = scheduleDocument.getString("direction") ?: "No direction"
                            val busNumber = scheduleDocument.getString("bus") ?: "No bus"

                            firestore.collection("schedules").document(scheduleId).collection("bookings")
                                .get()
                                .addOnSuccessListener { bookingsSnapshot ->
                                    val totalSeatsBooked = bookingsSnapshot.sumOf { it.getLong("seats") ?: 0 }

                                    bookingList.add(
                                        Booking(
                                            id = scheduleDocument.id,
                                            scheduleId = scheduleId,
                                            date = formattedFirestoreDate ?: "Unknown date",
                                            time = time,
                                            seats = totalSeatsBooked.toString(),
                                            direction = direction,
                                            busNumber = busNumber
                                        )
                                    )

                                    Log.d(TAG, "Added schedule and booking details for schedule ID: $scheduleId")
                                    adapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error fetching bookings for schedule ID: $scheduleId", e)
                                }
                        } else {
                            Log.d(TAG, "Skipping schedule ID $scheduleId as it does not match today's date.")
                        }
                    }
                    if (bookingList.isEmpty()) {
                        Log.d(TAG, "No schedules found for today.")
                        //showSnackbar("No schedules found for today.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch schedules", e)
                    showSnackbar("Failed to fetch schedules.")
                }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to retrieve admin status: ${exception.message}")
            showSnackbar("Failed to retrieve admin status: ${exception.message}")
        }
    }










    private fun checkTripStatusAndStart(booking: Booking) {
        firestore.collection("shuttles").document("shuttle1")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val status = document.getString("status")
                    if (status == "ended") {
                        startTrip(booking)
                        Toast.makeText(this, "Opening Driver", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, DriverTrackShuttleActivity::class.java)
                        Log.d(TAG, "OpeningDriver")
                        startActivity(intent)
                    } else if (status == "available") {
                        Toast.makeText(this, "A trip is currently in progress. Please end the current trip before starting a new one.", Toast.LENGTH_LONG).show()
                    } else {
                        Log.d(TAG, "Unknown trip status.")
                    }
                } else {
                    // If document doesn't exist, start a new trip
                    startTrip(booking)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to check trip status: ${e.message}")
                showSnackbar("Failed to check trip status: ${e.message}")
            }
    }

    private fun startTrip(booking: Booking) {
        Log.d(TAG, "Trip started for booking: ${booking.id}")

        getCurrentLocation { currentLocation ->
            Log.d(TAG, "Current location received: Lat: ${currentLocation.latitude}, Lng: ${currentLocation.longitude}")

            // Determine the destination based on the booking direction
            val isGoingToStation = booking.direction == "To Station from Campus"
            val destination = if (isGoingToStation) "station" else "campus"

            val shuttleData = hashMapOf(
                "latitude" to currentLocation.latitude,
                "longitude" to currentLocation.longitude,
                "direction" to destination,
                "busNumber" to booking.busNumber,
                "status" to "available"
            )

            // Use Firestore to add the shuttle data
            firestore.collection("shuttles")
                .document("shuttle1") // Change this to the appropriate shuttle ID
                .set(shuttleData)
                .addOnSuccessListener {
                    Log.d(TAG, "Shuttle location updated in Firestore to $destination")
                    showSnackbar("Trip started for booking: ${booking.id}")

                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update shuttle location: ${e.message}")
                    showSnackbar("Failed to start trip: ${e.message}")
                }
        }
    }

    private fun endTrip(booking: Booking) {
        Log.d(TAG, "Ending trip for booking: ${booking.id}")

        getCurrentLocation { currentLocation ->
            Log.d(TAG, "Current location at trip end: Lat: ${currentLocation.latitude}, Lng: ${currentLocation.longitude}")

            // Retrieve direction and bus number from booking for consistency
            val isGoingToStation = booking.direction == "To Station from Campus"
            val destination = if (isGoingToStation) "station" else "campus"

            val shuttleData = hashMapOf(
                "latitude" to currentLocation.latitude,
                "longitude" to currentLocation.longitude,
                "direction" to destination,
                "busNumber" to booking.busNumber,
                "status" to "ended"
            )

            firestore.collection("shuttles")
                .document("shuttle1")
                .set(shuttleData)
                .addOnSuccessListener {
                    Log.d(TAG, "Shuttle trip ended in Firestore for booking: ${booking.id}")
                    showSnackbar("Trip ended for booking: ${booking.id}")
                    updateUIPostTrip()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to end shuttle trip: ${e.message}")
                    showSnackbar("Failed to end trip: ${e.message}")
                }
        }
    }

    /*fun onEndTripClicked(view: View) {
        val selectedBooking = bookingList.lastOrNull()
        selectedBooking?.let {
            endTrip(it)
        }
    }*/
    fun onEndTripClicked(view: View) {
        val selectedBooking = bookingList.lastOrNull()
        selectedBooking?.let {
            firestore.collection("shuttles").document("shuttle1")
                .update("status", "ended")
                .addOnSuccessListener {
                    Log.d(TAG, "Trip marked as ended in Firestore.")
                    TrackShuttleActivity.resetMapFromOtherActivity() // Notify map activity
                    showSnackbar("Trip has been successfully ended.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to end trip: ${e.message}")
                    showSnackbar("Failed to end trip: ${e.message}")
                }
        }
    }


    private fun updateUIPostTrip() {
        val endTripButton = findViewById<Button>(R.id.endTripButton)
        endTripButton.isEnabled = false
        Toast.makeText(this, "Trip has been ended", Toast.LENGTH_LONG).show()
    }

    private fun getCurrentLocation(callback: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d(TAG, "Current location fetched: Lat: ${location.latitude}, Lng: ${location.longitude}")
                    callback(location)
                } else {
                    Log.d(TAG, "Location is null")
                    showSnackbar("Unable to fetch current location.")
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.rootLayout), message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "StartTripActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
