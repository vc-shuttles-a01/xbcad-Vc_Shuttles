package com.example.xbcad7319

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page) // Make sure the layout name matches your XML file name

        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val emailEditText = findViewById<EditText>(R.id.txtEmailSign)
        val passwordEditText = findViewById<EditText>(R.id.txtPasswordSign)
        val passwordConfirmEditText = findViewById<EditText>(R.id.txtPasswordConfirmSign)
        val signInButton = findViewById<TextView>(R.id.rfaymaec7gc)

        btnBack.setOnClickListener {
            // Go back to the previous Activity or close the current one
            finish()
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = passwordConfirmEditText.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                registerUser(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show()
                // Redirect to login activity or main app activity
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
