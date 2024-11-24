package com.example.xbcad7319

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class BookingActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookButton: Button
    private lateinit var seatNumberEditText: EditText
    private lateinit var schedulesListView: ListView
    //private lateinit var schedulesAdapter: ArrayAdapter<String>
    //private val schedulesList = mutableListOf<String>()

    private val schedulesList = mutableListOf<DetailedSchedule>()
    private lateinit var detailedSchedulesAdapter: DetailedScheduleAdapter


    //working code

    private var selectedScheduleId: String? = null
    private var currentSeatCount: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        firestore = FirebaseFirestore.getInstance()
        bookButton = findViewById(R.id.bookButton)
        seatNumberEditText = findViewById(R.id.seatNumberEditText)
        schedulesListView = findViewById(R.id.schedulesListView)

        setupSchedulesListView()
        fetchSchedules()

        bookButton.isEnabled = false
        bookButton.setOnClickListener {
            bookShuttle()
        }
    }

   /* private fun setupSchedulesListView() {
        schedulesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, schedulesList)
        schedulesListView.adapter = schedulesAdapter

        schedulesListView.setOnItemClickListener { _, _, position, _ ->
            selectedScheduleId = schedulesList[position].split(", ")[0].substringAfter("ID: ")
            bookButton.isEnabled = true
            Toast.makeText(this, "Selected Schedule ID: $selectedScheduleId", Toast.LENGTH_SHORT).show()

            // Fetch current seat availability for the selected schedule
            monitorSeatAvailability()
        }
    }*/

    private fun setupSchedulesListView() {
        detailedSchedulesAdapter = DetailedScheduleAdapter(this, schedulesList)
        schedulesListView.adapter = detailedSchedulesAdapter

        schedulesListView.setOnItemClickListener { _, _, position, _ ->
            selectedScheduleId = schedulesList[position].id
            bookButton.isEnabled = true
            Toast.makeText(this, "Selected Schedule ID: $selectedScheduleId", Toast.LENGTH_SHORT).show()

            // Fetch current seat availability for the selected schedule
            monitorSeatAvailability()
        }
    }


    private fun monitorSeatAvailability() {
        selectedScheduleId?.let { scheduleId ->
            firestore.collection("schedules").document(scheduleId)
                .collection("bookings")
                .get()
                .addOnSuccessListener { bookingDocuments ->
                    // Calculate total booked seats
                    currentSeatCount = bookingDocuments.sumOf { it.getLong("seats")?.toInt() ?: 0 }
                    Log.d("BookingActivity", "Current seat count for $scheduleId: $currentSeatCount")
                }
                .addOnFailureListener { e ->
                    Log.w("BookingActivity", "Failed to retrieve schedule details", e)
                }
        }
    }

    /*private fun fetchSchedules() {
        firestore.collection("schedules")
            .get()
            .addOnSuccessListener { documents ->
                schedulesList.clear()
                val scheduleDetailsList = mutableListOf<String>()

                // Collect initial schedule details first
                for (document in documents) {
                    val scheduleId = document.id
                    val date = document.getString("date") ?: "Unknown date"
                    val time = document.getString("time") ?: "Unknown time"
                    val bus = document.getString("bus") ?: "Unknown bus"
                    val direction = document.getString("direction") ?: "No direction"

                    val initialDetail = "ID: $scheduleId, Date: $date, Time: $time, Bus: $bus, Direction: $direction"
                    scheduleDetailsList.add(initialDetail)

                    // Fetch booking count asynchronously
                    firestore.collection("schedules").document(scheduleId).collection("bookings")
                        .get()
                        .addOnSuccessListener { bookingDocuments ->
                            val totalSeatsBooked = bookingDocuments.sumOf { it.getLong("seats") ?: 0 }

                            // Update the details with the total seats booked and add it to the schedules list
                            val scheduleDetailWithSeats = "$initialDetail, Booked Seats: $totalSeatsBooked"
                            schedulesList[scheduleDetailsList.indexOf(initialDetail)] = scheduleDetailWithSeats
                            schedulesAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("BookingActivity", "Error fetching bookings for schedule $scheduleId", e)
                        }
                }

                schedulesList.addAll(scheduleDetailsList)
                schedulesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching schedules: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("BookingActivity", "Error fetching schedules", exception)
            }
    }*/

    private fun fetchSchedules() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0) // Start of the day
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val currentDate = today.time
        Log.d("FetchSchedules", "Fetching schedules for today or future dates. Current date: $currentDate")

        firestore.collection("schedules")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FetchSchedules", "Successfully fetched schedules. Document count: ${documents.size()}")
                schedulesList.clear()

                for (document in documents) {
                    val scheduleId = document.id
                    val dateString = document.getString("date") ?: continue
                    val time = document.getString("time") ?: "Unknown time"
                    val bus = document.getString("bus") ?: "Unknown bus"
                    val direction = document.getString("direction") ?: "No direction"

                    Log.d("FetchSchedules", "Processing schedule ID: $scheduleId, Date: $dateString, Time: $time, Bus: $bus, Direction: $direction")

                    try {
                        // Attempt to parse the date using multiple formats
                        val scheduleDate = parseDate(dateString)

                        if (scheduleDate != null && !scheduleDate.before(currentDate)) {
                            firestore.collection("schedules").document(scheduleId).collection("bookings")
                                .get()
                                .addOnSuccessListener { bookingDocuments ->
                                    val totalSeatsBooked = bookingDocuments.sumOf { it.getLong("seats")?.toInt() ?: 0 }
                                    Log.d("FetchSchedules", "Schedule ID: $scheduleId, Total booked seats: $totalSeatsBooked")

                                    // Add to schedules list
                                    schedulesList.add(
                                        DetailedSchedule(
                                            id = scheduleId,
                                            date = dateString, // Keep as a readable string
                                            time = time,
                                            busNumber = bus,
                                            direction = direction,
                                            bookedSeats = totalSeatsBooked
                                        )
                                    )
                                    detailedSchedulesAdapter.notifyDataSetChanged()
                                    Log.d("FetchSchedules", "Schedule ID: $scheduleId added to the list")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FetchSchedules", "Error fetching bookings for schedule ID: $scheduleId", e)
                                }
                        } else {
                            Log.d("FetchSchedules", "Schedule ID: $scheduleId has a past date and will be skipped.")
                        }
                    } catch (e: ParseException) {
                        Log.e("FetchSchedules", "Failed to parse date for schedule ID: $scheduleId, Date: $dateString", e)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching schedules: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("FetchSchedules", "Error fetching schedules", exception)
            }
    }

    /**
     * Parses the date using multiple formats. Returns null if parsing fails for all formats.
     */
    private fun parseDate(dateString: String): Date? {
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()), // Format "19/10/2024"
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // Format "2024-11-21"
        )

        for (format in formats) {
            try {
                return format.parse(dateString)
            } catch (e: ParseException) {
                // Continue to the next format
            }
        }

        Log.e("ParseDate", "Unparseable date: $dateString")
        return null
    }








    private fun bookShuttle() {
        if (selectedScheduleId == null) {
            Toast.makeText(this, "Please select a schedule first.", Toast.LENGTH_SHORT).show()
            return
        }

        val numberOfSeats = seatNumberEditText.text.toString().toIntOrNull()
        if (numberOfSeats == null || numberOfSeats <= 0) {
            Toast.makeText(this, "Please enter a valid number of seats.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if booking would exceed seat capacity
        val totalSeatsAfterBooking = currentSeatCount + numberOfSeats
        if (currentSeatCount >= 16) {
            Toast.makeText(this, "Shuttle is already fully booked.", Toast.LENGTH_SHORT).show()
            return
        } else if (totalSeatsAfterBooking > 16) {
            val availableSeats = 16 - currentSeatCount
            Toast.makeText(this, "Only $availableSeats seats are available.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate unique booking code
        val bookingCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT)


        // Create the booking data including scheduleId
        val bookingData = hashMapOf(
            "userId" to userId,
            "seats" to numberOfSeats,
            "scheduleId" to selectedScheduleId  // Ensure scheduleId is saved
        )

        // Add booking to the selected schedule
        /*firestore.collection("schedules").document(selectedScheduleId!!)
            .collection("bookings").add(bookingData)
            .addOnSuccessListener { bookingRef ->
                val bookingId = bookingRef.id
                val qrBitmap = generateQRCode(bookingId)
                saveQRCodeToFirestore(qrBitmap, bookingId)
                updateTotalSeats(numberOfSeats)
                Toast.makeText(this, "Booking successful!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("BookingActivity", "Failed to save booking: ${e.message}", e)
                Toast.makeText(this, "Booking failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }*/

        // Save the booking
        firestore.collection("schedules").document(selectedScheduleId!!)
            .collection("bookings").document(bookingCode)
            .set(bookingData)
            .addOnSuccessListener {
                updateTotalSeats(numberOfSeats)
                Toast.makeText(this, "Booking successful! Code: $bookingCode", Toast.LENGTH_LONG).show()
                Log.e("BookingActivity", "Code: $bookingCode")
            }
            .addOnFailureListener { e ->
                Log.e("BookingActivity", "Failed to save booking: ${e.message}", e)
                Toast.makeText(this, "Booking failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun updateTotalSeats(seatsToAdd: Int) {
        selectedScheduleId?.let { scheduleId ->
            firestore.collection("schedules").document(scheduleId)
                .update("totalSeatsBooked", currentSeatCount + seatsToAdd)
                .addOnSuccessListener {
                    Log.d("BookingActivity", "Total seats booked updated for schedule $scheduleId")
                    currentSeatCount += seatsToAdd
                }
                .addOnFailureListener { e ->
                    Log.w("BookingActivity", "Failed to update total seats booked", e)
                }
        }
    }


    private fun generateQRCode(bookingId: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(bookingId, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        }
    }


    private fun saveQRCodeToFirestore(qrBitmap: Bitmap, bookingId: String) {
        val baos = ByteArrayOutputStream()
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val qrBytes = baos.toByteArray()
        val qrBase64 = Base64.encodeToString(qrBytes, Base64.DEFAULT)

        selectedScheduleId?.let { scheduleId ->
            firestore.collection("schedules").document(scheduleId)
                .collection("bookings").document(bookingId)
                .update("qrCode", qrBase64)
                .addOnSuccessListener {
                    Log.d("BookingActivity", "QR Code saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("BookingActivity", "Failed to save QR Code", e)
                }
        } ?: run {
            Log.e("BookingActivity", "Failed to save QR Code: Schedule ID is null")
        }
    }





}
