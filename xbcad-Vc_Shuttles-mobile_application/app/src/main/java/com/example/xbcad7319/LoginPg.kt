package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginPg : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var rememberMeCheckBox: LinearLayout

    //kinda working code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_pg)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpTextView = findViewById(R.id.txtSignUpButton)
        forgotPasswordTextView = findViewById(R.id.r8mmlj4fgezy)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            loginUser(email, password)
        }

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, RequestSignupActivity::class.java))
        }

        /*forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }*/

        rememberMeCheckBox = findViewById(R.id.renpmhuj535r)
        rememberMeCheckBox.setOnClickListener {
            toggleRememberMe()
        }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Get and log the ID token
                auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                    if (tokenTask.isSuccessful) {
                        val idToken = tokenTask.result?.token
                        Log.d("LoginPg", "ID Token: $idToken") // Log the token
                        checkAdminClaims()
                    } else {
                        Toast.makeText(this, "Failed to retrieve token: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkAdminClaims() {
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val claims = task.result?.claims
                val isAdmin = claims?.get("isAdmin") as Boolean? ?: false // Default to false if null
                val isDriver = claims?.get("isDriver") as Boolean? ?: false // Default to false if null

                when {
                    isAdmin -> {
                        Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AdminLandingActivity::class.java))
                    }
                    isDriver -> {
                        Toast.makeText(this, "Driver Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DriverLandingActivity::class.java))
                    }
                    else -> {
                        Toast.makeText(this, "User Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LandingPage::class.java))
                    }
                }
            } else {
                Toast.makeText(this, "Failed to retrieve login information: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun toggleRememberMe() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val isRemembered = prefs.getBoolean("remember_me", false)
        prefs.edit().putBoolean("remember_me", !isRemembered).apply()
        // Implement changes in the UI if necessary
    }
}
