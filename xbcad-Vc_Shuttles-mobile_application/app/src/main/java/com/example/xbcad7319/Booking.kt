package com.example.xbcad7319

/*data class Booking(
    val id: String,
    val scheduleId: String?,  // Add scheduleId to reference the specific schedule
    val date: String,
    val time: String,
    val seats: String,
    val direction: String,
    val busNumber: String
)*/

data class Booking(
    val id: String,
    val scheduleId: String,
    val date: String,
    val time: String,
    val seats: String,
    val direction: String,
    val busNumber: String,
    val qrCode: String? = null
)

