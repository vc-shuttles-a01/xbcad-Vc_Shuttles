package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        // Using a handler to delay loading the MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the next activity
            startActivity(Intent(this, LoginPg::class.java))
            // Close this activity
            finish()
        }, 3000)  // 3000 milliseconds or 3 seconds delay
    }
}