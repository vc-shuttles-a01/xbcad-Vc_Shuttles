package com.example.xbcad7319

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
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

class SignupRequestsActivity : AppCompatActivity() {

    private lateinit var signupRequestsListView: ListView
    private val signupRequests = ArrayList<String>()
    private lateinit var requestsAdapter: ArrayAdapter<String>
    private val db = FirebaseFirestore.getInstance()
    private var createUserUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/createUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_requests)

        signupRequestsListView = findViewById(R.id.signupRequestsListView)
        requestsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, signupRequests)
        signupRequestsListView.adapter = requestsAdapter

        fetchSignupRequests()

        signupRequestsListView.setOnItemClickListener { _, _, position, _ ->
            val email = signupRequests[position]
            showPasswordDialog(email, position)
        }
    }

    private fun fetchSignupRequests() {
        db.collection("signupRequests")
            .get()
            .addOnSuccessListener { documents ->
                signupRequests.clear()
                for (doc in documents) {
                    val email = doc.getString("email") ?: "Unknown Email"
                    signupRequests.add(email)
                }
                requestsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("SignupRequestsActivity", "Error fetching signup requests", e)
                Toast.makeText(this, "Failed to retrieve signup requests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPasswordDialog(email: String, position: Int) {
        val input = EditText(this)
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
                    { response ->
                        Toast.makeText(this, "User $email created successfully", Toast.LENGTH_SHORT).show()
                        removeSignupRequest(email, position)
                    },
                    { error ->
                        Log.e("SignupRequestsActivity", "Error creating user", error)
                        if (error.networkResponse?.statusCode == 401) {
                            Toast.makeText(this, "Authentication failed. Check your token.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                        }
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

    private fun removeSignupRequest(email: String, position: Int) {
        db.collection("signupRequests")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    db.collection("signupRequests").document(doc.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signup request for $email removed", Toast.LENGTH_SHORT).show()
                            signupRequests.removeAt(position)
                            requestsAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignupRequestsActivity", "Error removing signup request", e)
                            Toast.makeText(this, "Failed to remove signup request", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SignupRequestsActivity", "Error fetching signup request to delete", e)
                Toast.makeText(this, "Failed to remove signup request", Toast.LENGTH_SHORT).show()
            }
    }
}
