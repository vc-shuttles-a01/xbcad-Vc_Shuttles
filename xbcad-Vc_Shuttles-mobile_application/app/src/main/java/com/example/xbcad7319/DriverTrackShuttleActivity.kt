package com.example.xbcad7319

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Handler

@Suppress("DEPRECATION")
class DriverTrackShuttleActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvDistanceToDestination: TextView
    private lateinit var tvDestinationName: TextView
    private lateinit var tvEstimatedTime: TextView

    private val campusLocation = LatLng(-26.088463, 28.047398)
    private val stationLocation = LatLng(-26.10479567926396, 28.057090752215668)
    private lateinit var firestore: FirebaseFirestore

    // Fused Location Provider
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentDirection: String = ""
    private var destinationName: String =""

    private var shuttleMarker: Marker? = null
    private var polyline: Polyline? = null
    private var destinationMarker: Marker? = null

    private var lastFirestoreUpdateTime = 0L

    private lateinit var wakeLock: PowerManager.WakeLock


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_track_shuttle)

        // Acquire a wake lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyApp::TrackShuttleWakeLock")
        wakeLock.acquire()

        // Keep the screen on
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        firestore = Firebase.firestore
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize TextViews
        tvDistanceToDestination = findViewById(R.id.tvDistanceToDestination)
        tvDestinationName = findViewById(R.id.tvDestinationName)
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up location updates
        setupLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the wake lock
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
        instance = null
    }


    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(TAG, "Updated Location: Lat: ${location.latitude}, Lng: ${location.longitude}")
                    updateShuttleLocationInFirestore(location)
                }
            }
        }
    }

    /*private fun updateShuttleLocationInFirestore(location: Location) {
        val shuttleData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "status" to "available"
        )
        firestore.collection("shuttles").document("shuttle1").set(shuttleData, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Location updated in Firestore.") }
            .addOnFailureListener { e -> Log.e(TAG, "Error updating location: ${e.message}") }
    }*/

    private fun updateShuttleLocationInFirestore(location: Location) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFirestoreUpdateTime >= 10_000) { // 10 seconds
            val shuttleData = hashMapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "status" to "available"
            )
            firestore.collection("shuttles").document("shuttle1").set(shuttleData, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "Location updated in Firestore.") }
                .addOnFailureListener { e -> Log.e(TAG, "Error updating location: ${e.message}") }

            lastFirestoreUpdateTime = currentTime
        }
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocation, 15f)) // Default to station location

        // Listen for real-time updates
        listenForUpdates()
    }

    private fun listenForUpdates() {
        val shuttleId = "shuttle1"
        firestore.collection("shuttles").document(shuttleId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val location = snapshot.toObject(ShuttleLocation::class.java)
                    location?.let {
                        // Check if the status is "ended" and reset the map if true
                        if (it.status == "ended") {
                            Log.d(TAG, "Trip has ended. Resetting map.")
                            resetMapAndUI()
                            return@addSnapshotListener // Exit early if the trip has ended
                        }

                        val shuttleLatLng = LatLng(it.latitude, it.longitude)

                        animateMarker(shuttleLatLng)

                        // Determine the destination based on the current direction
                        val isGoingToCampus = it.direction == "campus"
                        destinationName = if (isGoingToCampus) "Campus" else "Station"

                        // Log the destination name and coordinates
                        Log.d("Tracking Destination", "Des: $destinationName, $shuttleLatLng")

                        val destinationLocation = if (isGoingToCampus) campusLocation else stationLocation

                        // Move camera to the destination
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15f))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shuttleLatLng, 15f))


                        val distanceToDestination = calculateDistance(shuttleLatLng, destinationLocation)
                        val etaInSeconds = calculateETA(distanceToDestination)

                        // Calculate the arrival time
                        val arrivalTime = calculateArrivalTime(etaInSeconds)

                        // Update UI with distance, ETA, and destination info
                        tvDistanceToDestination.text = "Distance to $destinationName: ${String.format("%.2f", distanceToDestination)} meters"
                        tvEstimatedTime.text = "Estimated Arrival: $arrivalTime"
                        tvDestinationName.text = "Destination: $destinationName"

                        // Draw the route on the map
                        //getRoute(shuttleLatLng, destinationLocation)

                        // Move or add the shuttle marker
                        if (shuttleMarker == null) {
                            shuttleMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(shuttleLatLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shuttle_icon1))
                                    .title("Shuttle Location")
                            )
                        } else {
                            shuttleMarker?.position = shuttleLatLng
                        }

                        // Manage destination marker and polyline drawing
                        updateDestinationAndRoute(it.direction, shuttleLatLng)
                    }
                } else {
                    Log.d(TAG, "Current location data does not exist.")
                }
            }
    }


    private fun getRoute(shuttleLocation: LatLng, destinationLocation: LatLng) {
        val origin = "${shuttleLocation.latitude},${shuttleLocation.longitude}"
        val destination = "${destinationLocation.latitude},${destinationLocation.longitude}"
        val apiKey = "AIzaSyCcJqVERnrXi4yZwf4HCeSSZz0IdIbePbc"

        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=$apiKey"

        Log.d(TAG, "Fetching route from $origin to $destination")

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            Log.d(TAG, "Received response: $response")
            try {
                val jsonObject = JSONObject(response)
                val routes = jsonObject.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                    val line = PolyUtil.decode(points)

                    // Clear existing polyline
                    polyline?.remove()
                    polyline = mMap.addPolyline(
                        PolylineOptions().addAll(line).width(10f).color(Color.BLUE)
                    )

                    // Get ETA from the response
                    val duration = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("value")
                    val etaInSeconds = JSONObject(duration).getInt("value")

                    // Update ETA TextView
                    tvEstimatedTime.text = "Estimated Arrival: ${calculateArrivalTime(etaInSeconds)}"

                    Log.d(TAG, "Route drawn successfully")
                } else {
                    Log.e(TAG, "No routes found in response")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing directions: ${e.message}")
            }
        }, { error ->
            Log.e(TAG, "Error fetching directions: ${error.message}")
        })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun updateDestinationAndRoute(direction: String, shuttleLatLng: LatLng) {
        val isGoingToCampus = direction == "campus"
        val destinationLocation = if (isGoingToCampus) campusLocation else stationLocation
        destinationName = if (isGoingToCampus) "Campus" else "Station"

        // Place or update the destination marker
        if (destinationMarker == null) {
            destinationMarker = mMap.addMarker(
                MarkerOptions().position(destinationLocation)
                    .title("Destination: $destinationName")
            )
        } else {
            destinationMarker?.position = destinationLocation
        }

        // Update route polyline
        getRoute(shuttleLatLng, destinationLocation)
    }


    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val startLocation = Location("start").apply {
            latitude = start.latitude
            longitude = start.longitude
        }

        val endLocation = Location("end").apply {
            latitude = end.latitude
            longitude = end.longitude
        }

        return startLocation.distanceTo(endLocation)
    }

    private fun calculateETA(distance: Float): Int {
        // Assuming average speed of 30 km/h (which is 8.33 m/s)
        val averageSpeed = 8.33 // m/s
        return (distance / averageSpeed).toInt() // Return ETA in seconds
    }

    private fun calculateArrivalTime(etaInSeconds: Int): String {
        val currentTime = System.currentTimeMillis() // Get current time in milliseconds
        val arrivalTimeMillis = currentTime + etaInSeconds * 1000 // Add ETA in milliseconds
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(arrivalTimeMillis)) // Format to time
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
        val intent = Intent(this, ForegroundLocationService::class.java)
        startService(intent)
    }

    override fun onStop() {
        super.onStop()
        // Remove the keep screen on flag when the activity is stopped
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        stopLocationUpdates()
        val intent = Intent(this, ForegroundLocationService::class.java)
        stopService(intent)
    }

    override fun onResume() {
        super.onResume()
        // Ensure the flag is set when the activity resumes
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }



    fun resetMapAndUI() {
        polyline?.remove()
        shuttleMarker?.remove()
        destinationMarker?.remove()
        polyline = null
        shuttleMarker = null
        destinationMarker = null

        tvDistanceToDestination.text = ""
        tvEstimatedTime.text = ""
        tvDestinationName.text = "No Destination"
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(LocationRequest.create().apply {
            interval = 3000 // Updates every 3 seconds
            fastestInterval = 1000 // Fastest possible update
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }, locationCallback, null)

    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun animateMarker(newLocation: LatLng) {
        if (shuttleMarker == null) {
            shuttleMarker = mMap.addMarker(
                MarkerOptions()
                    .position(newLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shuttle_icon1))
                    .title("Shuttle Location")
            )
        } else {
            val startPosition = shuttleMarker!!.position
            val handler = android.os.Handler()
            val startTime = SystemClock.uptimeMillis()
            val duration = 2000 // Adjust duration for smoother movement

            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = SystemClock.uptimeMillis() - startTime
                    val t = Math.min(1f, elapsed / duration.toFloat())
                    val lat = t * newLocation.latitude + (1 - t) * startPosition.latitude
                    val lng = t * newLocation.longitude + (1 - t) * startPosition.longitude
                    shuttleMarker!!.position = LatLng(lat, lng)
                    if (t < 1f) {
                        handler.postDelayed(this, 16) // Repeat every 16 ms for smooth transition
                    }
                }
            })
        }
    }



    companion object {
        private const val TAG = "TrackShuttleActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private var instance: TrackShuttleActivity? = null

        fun resetMapFromOtherActivity() {
            //instance?.resetMapAndUI()
        }

    }

    /*private fun resetMap() {
        mMap.clear() // Clear all map overlays
        val defaultLocation = LatLng(-34.0, 151.0) // Set to your desired reset location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        //fetchBookingsForToday() // Optionally refetch data from Firestore
    }*/




}
