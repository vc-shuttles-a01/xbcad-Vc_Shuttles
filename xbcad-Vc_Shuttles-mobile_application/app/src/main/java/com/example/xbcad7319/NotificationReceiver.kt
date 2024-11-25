package com.example.xbcad7319

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Booking Reminder"
        val message = intent.getStringExtra("message") ?: "Your booking is coming up."
        NotificationHelper.sendNotification(context, title, message, System.currentTimeMillis().toInt())
    }
}
