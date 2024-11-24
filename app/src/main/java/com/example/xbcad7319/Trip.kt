package com.example.xbcad7319

data class Trip(
    val id: String = "", // Unique identifier for the trip
    val date: String = "", // Date of the trip
    val time: String = "", // Time of the trip
    val destination: String = "",
    val shuttleId: String = "",
    var status: String = "" // Current status of the trip
)
