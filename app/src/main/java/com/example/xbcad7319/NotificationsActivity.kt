package com.example.xbcad7319

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
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
    private lateinit var backButton: Button
    private lateinit var checkboxBookingUpdates: CheckBox
    private lateinit var checkboxScheduleChanges: CheckBox
    private lateinit var checkboxGeneralAnnouncements: CheckBox
    private lateinit var savePreferencesButton: Button
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
        backButton = findViewById(R.id.backButton)
        checkboxBookingUpdates = findViewById(R.id.checkbox_booking_updates)
        checkboxScheduleChanges = findViewById(R.id.checkbox_schedule_changes)
        checkboxGeneralAnnouncements = findViewById(R.id.checkbox_general_announcements)
        savePreferencesButton = findViewById(R.id.savePreferencesButton)



        // Initialize notification channel
        createNotificationChannel()

        // Load preferences
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        notificationSwitch.isChecked = notificationsEnabled
        checkboxBookingUpdates.isChecked = sharedPreferences.getBoolean("booking_updates", true)
        checkboxScheduleChanges.isChecked = sharedPreferences.getBoolean("schedule_changes", true)
        checkboxGeneralAnnouncements.isChecked = sharedPreferences.getBoolean("general_announcements", true)

        // Set up switch listener
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            notificationsEnabled = isChecked
            sharedPreferences.edit().putBoolean("notifications_enabled", notificationsEnabled).apply()
            if (notificationsEnabled) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                fetchAndScheduleNotifications()
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show()
                cancelAllNotifications()
            }
        }

        // Save preferences button
        savePreferencesButton.setOnClickListener {
            savePreferences()
        }

        // Back button
        backButton.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }

        // Initial notification fetching if enabled
        if (notificationsEnabled) {
            fetchAndScheduleNotifications()
        }
    }

    private fun savePreferences() {
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("booking_updates", checkboxBookingUpdates.isChecked)
            putBoolean("schedule_changes", checkboxScheduleChanges.isChecked)
            putBoolean("general_announcements", checkboxGeneralAnnouncements.isChecked)
        }.apply()
        Toast.makeText(this, "Preferences Saved", Toast.LENGTH_SHORT).show()
    }

    private fun fetchAndScheduleNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("schedules")
            .get()
            .addOnSuccessListener { schedulesSnapshot ->
                for (scheduleDocument in schedulesSnapshot.documents) {
                    val scheduleId = scheduleDocument.id
                    val scheduleDate = scheduleDocument.getString("date") ?: continue
                    val scheduleTime = scheduleDocument.getString("time") ?: continue

                    FirebaseFirestore.getInstance()
                        .collection("schedules")
                        .document(scheduleId)
                        .collection("bookings")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { bookingsSnapshot ->
                            if (bookingsSnapshot.isEmpty) return@addOnSuccessListener
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

                                if (currentTime.before(oneHourBefore)) {
                                    scheduleNotification("Booking Reminder", "Your booking is in 1 hour.", oneHourBefore.time)
                                }

                                if (currentTime.before(thirtyMinutesBefore)) {
                                    scheduleNotification("Booking Reminder", "Your booking is in 30 minutes.", thirtyMinutesBefore.time)
                                }
                            }
                        }
                }
            }
    }

    private fun scheduleNotification(title: String, content: String, triggerTime: Long) {
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
        }
    }

    private fun cancelAllNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
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
        }
    }
}
