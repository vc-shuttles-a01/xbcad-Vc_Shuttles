package com.example.xbcad7319

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private var notificationsEnabled = false
    private val notificationChannelId = "booking_notifications_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        notificationSwitch = findViewById(R.id.switch_notifications)

        // Initialize notification channel
        createNotificationChannel()

        // Load saved notification preference
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        notificationSwitch.isChecked = notificationsEnabled

        // Log the initial state
        Log.d("NotificationsActivity", "Notifications enabled: $notificationsEnabled")

        // Set up listener for the switch
        notificationSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            notificationsEnabled = isChecked
            sharedPreferences.edit().putBoolean("notifications_enabled", notificationsEnabled).apply()
            if (notificationsEnabled) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                Log.d("NotificationsActivity", "Notifications enabled by user")
                fetchAndScheduleNotifications()
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
                Log.d("NotificationsActivity", "Notifications disabled by user")
                cancelAllNotifications()
            }
        }

        if (notificationsEnabled) {
            fetchAndScheduleNotifications()
        }
    }

    private fun fetchAndScheduleNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("NotificationsActivity", "Fetching bookings for user ID: $userId")
        Toast.makeText(this, "Fetching bookings...", Toast.LENGTH_SHORT).show()

        FirebaseFirestore.getInstance().collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    val scheduleDate = scheduleDocument.getString("date") ?: continue
                    val scheduleTime = scheduleDocument.getString("time") ?: continue

                    Log.d("NotificationsActivity", "Processing schedule ID: $scheduleId")

                    FirebaseFirestore.getInstance()
                        .collection("schedules")
                        .document(scheduleId)
                        .collection("bookings")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { bookingsSnapshot ->
                            if (bookingsSnapshot.isEmpty) {
                                Log.d("NotificationsActivity", "No bookings found for schedule ID: $scheduleId")
                                Toast.makeText(this, "No bookings found for schedule: $scheduleId", Toast.LENGTH_SHORT).show()
                            } else {
                                for (bookingDocument in bookingsSnapshot.documents) {
                                    val bookingTime = "$scheduleDate $scheduleTime"
                                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    val bookingDateTime = formatter.parse(bookingTime) ?: continue
                                    val currentTime = Calendar.getInstance().time

                                    val oneHourBefore = Calendar.getInstance().apply {
                                        time = bookingDateTime
                                        add(Calendar.HOUR, -1)
                                    }.time

                                    val thirtyMinutesBefore = Calendar.getInstance().apply {
                                        time = bookingDateTime
                                        add(Calendar.MINUTE, -30)
                                    }.time

                                    Log.d("NotificationsActivity", "Booking found: ${bookingDocument.id}")
                                    Toast.makeText(this, "Booking found for schedule: $scheduleId", Toast.LENGTH_SHORT).show()

                                    if (currentTime.before(oneHourBefore)) {
                                        Log.d("NotificationsActivity", "Scheduling notification for 1 hour before booking.")
                                        scheduleNotification(
                                            "Booking Reminder",
                                            "Your booking is in 1 hour.",
                                            oneHourBefore.time
                                        )
                                    }

                                    if (currentTime.before(thirtyMinutesBefore)) {
                                        Log.d("NotificationsActivity", "Scheduling notification for 30 minutes before booking.")
                                        scheduleNotification(
                                            "Booking Reminder",
                                            "Your booking is in 30 minutes.",
                                            thirtyMinutesBefore.time
                                        )
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("NotificationsActivity", "Error fetching bookings for schedule $scheduleId", e)
                            Toast.makeText(this, "Error fetching bookings for schedule: $scheduleId", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationsActivity", "Error fetching schedules", e)
                Toast.makeText(this, "Error fetching schedules", Toast.LENGTH_SHORT).show()
            }
    }

    private fun scheduleNotification(title: String, content: String, triggerTime: Long) {
        Log.d("NotificationsActivity", "Scheduling notification: $title - $content")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, NotificationsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.shuttle_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val delay = triggerTime - System.currentTimeMillis()
        if (delay > 0) {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
                }
            }, delay)
            Toast.makeText(this, "Notification scheduled: $title", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("NotificationsActivity", "Notification time already passed, not scheduling.")
        }
    }

    private fun cancelAllNotifications() {
        Log.d("NotificationsActivity", "Canceling all notifications")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        Toast.makeText(this, "All notifications canceled", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Booking Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming bookings"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationsActivity", "Notification channel created.")
        }
    }
}
