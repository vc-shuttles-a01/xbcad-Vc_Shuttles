package com.example.xbcad7319

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SignupRequestsActivity : AppCompatActivity() {

    private lateinit var signupRequestsListView: ListView
    private val signupRequests = ArrayList<Pair<String, String>>() // List of email and document ID
    private lateinit var requestsAdapter: ArrayAdapter<String>
    private val db = FirebaseFirestore.getInstance()
    private val createUserUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/createUser"
    private val senderEmail = "group7.vs.shuttles@gmail.com"
    private val senderPassword = "dmrx vprb rxsv clle" // App-specific password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_requests)

        signupRequestsListView = findViewById(R.id.signupRequestsListView)
        requestsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            signupRequests.map { it.first } // Display only email
        )
        signupRequestsListView.adapter = requestsAdapter

        fetchSignupRequests()

        signupRequestsListView.setOnItemClickListener { _, _, position, _ ->
            val (email, docId) = signupRequests[position]
            showOptionsDialog(email, docId, position)
        }
    }

    private fun fetchSignupRequests() {
        db.collection("signupRequests")
            .get()
            .addOnSuccessListener { documents ->
                signupRequests.clear()
                for (doc in documents) {
                    val email = doc.getString("email") ?: "Unknown Email"
                    val docId = doc.id // Store document ID for deletion
                    signupRequests.add(Pair(email, docId))
                }
                updateAdapter()
            }
            .addOnFailureListener { e ->
                Log.e("SignupRequestsActivity", "Error fetching signup requests", e)
                Toast.makeText(this, "Failed to retrieve signup requests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAdapter() {
        requestsAdapter.clear()
        requestsAdapter.addAll(signupRequests.map { it.first }) // Update adapter with email only
        requestsAdapter.notifyDataSetChanged()
    }

    private fun showOptionsDialog(email: String, docId: String, position: Int) {
        val options = arrayOf("Approve", "Deny")
        val dialog = AlertDialog.Builder(this)
            .setTitle("Manage Signup Request")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPasswordDialog(email, position) // Approve
                    1 -> denyRequest(docId, position) // Deny/Delete
                }
            }
            .create()

        dialog.show()
    }

    private fun showPasswordDialog(email: String, position: Int) {
        val input = android.widget.EditText(this)
        input.hint = "Enter password"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Create User")
            .setMessage("Enter a password for $email:")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val password = input.text.toString().trim()
                if (password.isNotEmpty()) {
                    createUser(email, password, position)
                } else {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun denyRequest(docId: String, position: Int) {
        db.collection("signupRequests").document(docId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Request denied and removed", Toast.LENGTH_SHORT).show()
                signupRequests.removeAt(position)
                updateAdapter()
            }
            .addOnFailureListener { e ->
                Log.e("SignupRequestsActivity", "Error deleting signup request", e)
                Toast.makeText(this, "Failed to delete request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createUser(email: String, password: String, position: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                val queue = Volley.newRequestQueue(this)
                val jsonObject = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST, createUserUrl, jsonObject,
                    {
                        Toast.makeText(this, "User $email created successfully", Toast.LENGTH_SHORT).show()
                        sendEmailToUser(email, password) // Send email with credentials
                        denyRequest(signupRequests[position].second, position) // Remove request after approval
                    },
                    { error ->
                        Log.e("SignupRequestsActivity", "Error creating user", error)
                        Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return mapOf("Authorization" to "Bearer $idToken")
                    }
                }

                queue.add(jsonObjectRequest)
            } else {
                Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Log.e("SignupRequestsActivity", "Error fetching token", it)
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmailToUser(email: String, password: String) {
        val subject = "Your Account Credentials"
        val message = """
            Your account has been created successfully.
            
            Email: $email
            Password: $password
            
            Please log in using these credentials.
        """.trimIndent()

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true") // Enable STARTTLS
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.ssl.trust", "smtp.gmail.com") // Trust Gmail's certificate
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(email))
                this.subject = subject
                setText(message)
            }

            Thread {
                try {
                    Transport.send(mimeMessage)
                    Log.d("SignupRequestsActivity", "Email sent to $email")
                } catch (e: Exception) {
                    Log.e("SignupRequestsActivity", "Failed to send email to $email", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("SignupRequestsActivity", "Error creating email message", e)
        }
    }
}
