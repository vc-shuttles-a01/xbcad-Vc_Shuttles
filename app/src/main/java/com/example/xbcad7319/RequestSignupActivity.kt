package com.example.xbcad7319

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RequestSignupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var requestSignupButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_signup)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        requestSignupButton = findViewById(R.id.requestSignupButton)
        resultTextView = findViewById(R.id.resultTextView)




        requestSignupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter your name and email", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                successFul()
                sendSignupRequest(name, email)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun successFul(){
        // Show success or error message dynamically
        resultTextView.visibility = View.VISIBLE
        resultTextView.text = "Signup Successful!"
        resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
    }

    private fun sendSignupRequest(name: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val signupRequest = hashMapOf(
            "name" to name,
            "email" to email,
            "timestamp" to FieldValue.serverTimestamp() // Optional: For sorting by request time
        )

        // Add signup request to Firestore
        db.collection("signupRequests")
            .add(signupRequest)
            .addOnSuccessListener {
                Log.d("RequestSignupActivity", "Signup request sent successfully")
                notifyAdmins(name, email) // Notify admins about the signup request
                Toast.makeText(this, "Signup request sent successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after sending the request
            }
            .addOnFailureListener { e ->
                Log.e("RequestSignupActivity", "Error sending signup request", e)
                Toast.makeText(this, "Failed to send signup request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun notifyAdmins(requesterName: String, requesterEmail: String) {
        val db = FirebaseFirestore.getInstance()

        Log.d("RequestSignupActivity", "Fetching admin emails from Firestore.")

        // Query Firestore for admin users
        db.collection("users")
            .whereEqualTo("isAdmin", true)
            .get()
            .addOnSuccessListener { documents ->
                val adminEmails = documents.mapNotNull { it.getString("email") }

                if (adminEmails.isNotEmpty()) {
                    Log.d("RequestSignupActivity", "Admin emails fetched: $adminEmails")
                    sendEmailToAdmins(adminEmails, requesterName, requesterEmail)
                } else {
                    Log.d("RequestSignupActivity", "No admin emails found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("RequestSignupActivity", "Error fetching admin emails", e)
            }
    }


    private fun sendEmailToAdmins(adminEmails: List<String>, requesterName: String, requesterEmail: String) {
        val subject = "New Signup Request"
        val message = """
        A new signup request has been received.
        
        Name: $requesterName
        Email: $requesterEmail
        
        Please review and approve this request in the admin panel.
    """.trimIndent()

        // Counter to track success/failure of emails
        var emailsSent = 0
        var emailsFailed = 0

        // Use an email-sending library (e.g., JavaMail, or an external API like SendGrid)
        adminEmails.forEach { adminEmail ->
            try {
                // Replace this with actual email-sending code
                Log.d("RequestSignupActivity", "Attempting to send email to: $adminEmail")

                // Simulated email sending (replace this with actual implementation)
                sendEmail(adminEmail, subject, message)

                // If successful
                emailsSent++
                Log.d("RequestSignupActivity", "Email sent successfully to: $adminEmail")
            } catch (e: Exception) {
                // If sending fails
                emailsFailed++
                Log.e("RequestSignupActivity", "Failed to send email to: $adminEmail", e)
            }
        }

        // Provide feedback to the user
        if (emailsFailed == 0) {
            Toast.makeText(this, "All admin notifications sent successfully.", Toast.LENGTH_SHORT).show()
            Log.d("RequestSignupActivity", "All emails sent successfully.")
        } else {
            Toast.makeText(
                this,
                "Notifications sent to $emailsSent admins. $emailsFailed failed.",
                Toast.LENGTH_LONG
            ).show()
            Log.e("RequestSignupActivity", "Some emails failed to send. Sent: $emailsSent, Failed: $emailsFailed.")
        }
    }


    private fun sendEmail(to: String, subject: String, message: String) {
        // Email configuration
        val smtpHost = "smtp.gmail.com" // Change based on your email provider
        val smtpPort = "587" // Port for TLS
        val senderEmail = "group7.vs.shuttles@gmail.com" // Your email address
        val senderPassword = "Vc_shuttles@1925" // Your email password or app-specific password

        val properties = System.getProperties()
        properties["mail.smtp.host"] = smtpHost
        properties["mail.smtp.port"] = smtpPort
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true" // Enable STARTTLS

        try {
            // Create session with authentication
            val session = javax.mail.Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            // Create email message
            val mimeMessage = javax.mail.internet.MimeMessage(session)
            mimeMessage.setFrom(javax.mail.internet.InternetAddress(senderEmail))
            mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress(to))
            mimeMessage.subject = subject
            mimeMessage.setText(message)

            // Send email
            javax.mail.Transport.send(mimeMessage)
            Log.d("RequestSignupActivity", "Email sent successfully to: $to")
        } catch (e: Exception) {
            Log.e("RequestSignupActivity", "Failed to send email to: $to", e)
            throw e // Re-throw the exception for the calling function to handle
        }
    }

    



}
