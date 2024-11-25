package com.example.xbcad7319

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var manageBookingsButton: Button
    private lateinit var backButton: Button
    private lateinit var uploadProfileImageButton: Button
    private lateinit var upcomingBookingsListView: ListView
    private var upcomingBookingsList = ArrayList<Booking>()
    private lateinit var adapter: BookingAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 71
    private var selectedImageUri: Uri? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize UI components
        profilePicture = findViewById(R.id.profilePicture)
        userNameTextView = findViewById(R.id.userNameTextView)
        manageBookingsButton = findViewById(R.id.manageBookingsButton)
        backButton = findViewById(R.id.backButton)
        uploadProfileImageButton = findViewById(R.id.uploadProfileImageButton)
        upcomingBookingsListView = findViewById(R.id.upcomingBookingsListView)

        //adapter = BookingAdapter(this, upcomingBookingsList) { _, _ -> }

        adapter = BookingAdapter(this, upcomingBookingsList) { booking, action ->
            when (action) {
                BookingAdapter.Action.COMPLETE -> completeBooking(booking)
                BookingAdapter.Action.DELETE -> deleteBooking(booking)
            }
        }
        upcomingBookingsListView.adapter = adapter

        // Fetch user data
        fetchUserProfile()
        fetchUserBookings()

        // Set up listeners
        uploadProfileImageButton.setOnClickListener {
            openImagePicker()
        }

        backButton.setOnClickListener {
            finish()
        }

        manageBookingsButton.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }
    }

    private fun fetchUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("ProfileActivity", "User is not logged in.")
            userNameTextView.text = "Unknown User"
            return
        }

        userNameTextView.text = user.email ?: "Unknown User"
        val userId = user.uid

        // Load profile picture (if stored locally)
        val profileImageFile = File(filesDir, "$userId-profile.jpg")
        if (profileImageFile.exists()) {
            profilePicture.setImageURI(Uri.fromFile(profileImageFile))
            Log.d("ProfileActivity", "Profile picture loaded from local storage.")
        } else {
            Log.d("ProfileActivity", "No profile picture found locally.")
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                Log.d("ProfileActivity", "Image selected: $selectedImageUri")
                saveProfileImageLocally()
            } else {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileImageLocally() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        if (selectedImageUri != null) {
            try {
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                val profileImageFile = File(filesDir, "$userId-profile.jpg")

                val outputStream = FileOutputStream(profileImageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                profilePicture.setImageBitmap(bitmap)
                Toast.makeText(this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error saving image: ${e.message}", e)
                Toast.makeText(this, "Failed to save profile image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserBookings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("ProfileActivity", "User ID is null, cannot fetch bookings.")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ProfileActivity", "Fetching bookings for user ID: $userId")

        db.collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                upcomingBookingsList.clear()

                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    val scheduleDate = scheduleDocument.getString("date")
                    val scheduleTime = scheduleDocument.getString("time")

                    // Skip if date or time is missing
                    if (scheduleDate.isNullOrEmpty() || scheduleTime.isNullOrEmpty()) continue

                    val bookingDateTimeString = "$scheduleDate $scheduleTime"
                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    try {
                        val bookingDateTime = formatter.parse(bookingDateTimeString) ?: continue
                        val currentTime = Calendar.getInstance().time

                        // Only process upcoming bookings
                        if (currentTime.before(bookingDateTime)) {
                            db.collection("schedules")
                                .document(scheduleId)
                                .collection("bookings")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener { bookingsSnapshot ->
                                    if (!bookingsSnapshot.isEmpty) {
                                        for (bookingDocument in bookingsSnapshot.documents) {
                                            val booking = Booking(
                                                id = bookingDocument.id,
                                                scheduleId = scheduleId,
                                                date = scheduleDate,
                                                time = scheduleTime,
                                                seats = bookingDocument.getLong("seats")?.toString() ?: "N/A",
                                                direction = scheduleDocument.getString("direction") ?: "No direction",
                                                busNumber = scheduleDocument.getString("bus") ?: "No bus number"
                                            )
                                            upcomingBookingsList.add(booking)
                                        }
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ProfileActivity", "Error fetching bookings for schedule ID $scheduleId", e)
                                }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileActivity", "Error parsing date/time for schedule ID $scheduleId", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error fetching schedules", e)
            }
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
                upcomingBookingsList.remove(booking)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Booking deleted: Code: ${booking.id}", Toast.LENGTH_SHORT).show()
                Log.d("MyBookingsActivity", "Successfully deleted booking with ID: ${booking.id}")
            }
            .addOnFailureListener { e ->
                Log.e("MyBookingsActivity", "Error deleting booking with ID: ${booking.id}", e)
                Toast.makeText(this, "Error deleting booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun completeBooking(booking: Booking) {
        Log.d("MyBookingsActivity", "Completing booking with ID: ${booking.id}")
        Toast.makeText(this, "Booking completed: Code: ${booking.id}", Toast.LENGTH_SHORT).show()
    }

}
