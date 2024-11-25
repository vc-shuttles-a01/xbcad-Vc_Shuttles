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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit

class RequestSignupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var requestSignupButton: Button
    private lateinit var resultTextView: TextView

    //new code

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

    private fun successFul() {
        resultTextView.visibility = View.VISIBLE
        resultTextView.text = "Signup Successful!"
        resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
    }

    private fun sendSignupRequest(name: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val signupRequest = hashMapOf(
            "name" to name,
            "email" to email,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("signupRequests")
            .add(signupRequest)
            .addOnSuccessListener {
                Log.d("RequestSignupActivity", "Signup request sent successfully")
                fetchAdminsAndNotify(name, email)
                Toast.makeText(this, "Signup request sent successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("RequestSignupActivity", "Error sending signup request", e)
                Toast.makeText(this, "Failed to send signup request", Toast.LENGTH_SHORT).show()
            }
    }


        private fun fetchAdminsAndNotify(requesterName: String, requesterEmail: String) {
            val url = "https://api-qeqahdzppa-uc.a.run.app/getAdmins"
            val auth = FirebaseAuth.getInstance()

            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result?.token
                    if (idToken != null) {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build()

                        val request = Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer $idToken") // Properly formatted authorization header
                            .get()
                            .build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e("RequestSignupActivity", "Error fetching admins", e)
                                runOnUiThread {
                                    Toast.makeText(this@RequestSignupActivity, "Failed to fetch admins", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    response.body?.use { responseBody ->
                                        val responseString = responseBody.string()
                                        val adminEmails = parseAdminEmails(responseString)
                                        if (adminEmails.isNotEmpty()) {
                                            sendEmailToAdmins(adminEmails, requesterName, requesterEmail)
                                        } else {
                                            Log.d("RequestSignupActivity", "No admins found")
                                        }
                                    }
                                } else {
                                    Log.e("RequestSignupActivity", "Failed to fetch admins: ${response.code}")
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@RequestSignupActivity,
                                            "Failed to fetch admins: ${response.code}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        })
                    } else {
                        Log.e("RequestSignupActivity", "ID Token is null")
                    }
                } else {
                    Log.e("RequestSignupActivity", "Failed to retrieve ID Token", task.exception)
                }
            }
        }



        private fun parseAdminEmails(responseBody: String?): List<String> {
        val adminEmails = mutableListOf<String>()
        if (responseBody != null) {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val email = jsonObject.optString("email")
                if (email.isNotEmpty()) {
                    adminEmails.add(email)
                }
            }
        }
        return adminEmails
    }

    private fun sendEmailToAdmins(adminEmails: List<String>, requesterName: String, requesterEmail: String) {
        val subject = "New Signup Request"
        val message = """
        A new signup request has been received.
        
        Name: $requesterName
        Email: $requesterEmail
        
        Please review and approve this request in the admin panel.
        """.trimIndent()

        var emailsSent = 0
        var emailsFailed = 0

        adminEmails.forEach { adminEmail ->
            try {
                sendEmail(adminEmail, subject, message)
                emailsSent++
                Log.d("RequestSignupActivity", "Email sent to $adminEmail")
            } catch (e: Exception) {
                emailsFailed++
                Log.e("RequestSignupActivity", "Failed to send email to $adminEmail", e)
            }
        }

        if (emailsFailed == 0) {
            Log.d("RequestSignupActivity", "All emails sent successfully")
        } else {
            Log.e("RequestSignupActivity", "Some emails failed to send")
        }
    }


    private fun sendEmail(to: String, subject: String, message: String) {
        val smtpHost = "smtp.gmail.com"
        val smtpPort = "587" // Or "465" for SSL
        val senderEmail = "group7.vs.shuttles@gmail.com"
        //val senderPassword = "Vc_shuttles@1925"
        val senderPassword = "dmrx vprb rxsv clle"


        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true") // Enable STARTTLS
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
            put("mail.smtp.ssl.trust", smtpHost) // Trust Gmail's certificate
        }


        try {
            val session = javax.mail.Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(senderEmail, senderPassword)
                }
            }).apply {
                debug = true // Enable debug logs
            }

            val mimeMessage = javax.mail.internet.MimeMessage(session).apply {
                setFrom(javax.mail.internet.InternetAddress(senderEmail))
                addRecipient(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress(to))
                this.subject = subject
                setText(message)
            }

            javax.mail.Transport.send(mimeMessage)
            Log.d("RequestSignupActivity", "Email sent successfully to: $to")
        } catch (e: javax.mail.AuthenticationFailedException) {
            Log.e("RequestSignupActivity", "Authentication failed. Check email or app password.", e)

        } catch (e: Exception) {
            Log.e("RequestSignupActivity", "Failed to send email to: $to", e)
        }
    }


    /*private fun sendEmail(to: String, subject: String, message: String) {
        val smtpHost = "smtp.gmail.com"
        val smtpPort = "587"
        val senderEmail = "group7.vs.shuttles@gmail.com"
        val senderPassword = "Vc_shuttles@1925"

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.ssl.trust", "smtp.gmail.com")
            put("mail.smtp.connectiontimeout", "5000")
            put("mail.smtp.timeout", "5000")
        }

        try {
            val session = javax.mail.Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            val mimeMessage = javax.mail.internet.MimeMessage(session)
            mimeMessage.setFrom(javax.mail.internet.InternetAddress(senderEmail))
            mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress(to))
            mimeMessage.subject = subject
            mimeMessage.setText(message)

            javax.mail.Transport.send(mimeMessage)
            Log.d("RequestSignupActivity", "Email sent successfully to: $to")
        } catch (e: Exception) {
            Log.e("RequestSignupActivity", "Failed to send email to: $to", e)
            throw e // Re-throw for the calling method to handle
        }
    }*/






}
