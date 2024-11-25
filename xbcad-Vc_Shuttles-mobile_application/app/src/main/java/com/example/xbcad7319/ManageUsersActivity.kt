package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var addUserButton: Button
    private lateinit var viewRequestsButton: Button
    private lateinit var userEmailEditText: EditText
    private lateinit var userPasswordEditText: EditText
    private lateinit var userEmailsListView: ListView
    private lateinit var auth: FirebaseAuth

    private var createUserUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/createUser"
    private var fetchUsersUrl = "https://api-qeqahdzppa-uc.a.run.app/getUsers"
    private var deleteUserUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/deleteUser"

    private val userEmails = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        auth = FirebaseAuth.getInstance()
        addUserButton = findViewById(R.id.addUserButton)
        viewRequestsButton = findViewById(R.id.viewRequestsButton)
        userEmailEditText = findViewById(R.id.userEmailEditText)
        userPasswordEditText = findViewById(R.id.userPasswordEditText)
        userEmailsListView = findViewById(R.id.userEmailsListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userEmails)
        userEmailsListView.adapter = adapter

        fetchUserEmails()

        addUserButton.setOnClickListener {
            val email = userEmailEditText.text.toString().trim()
            val password = userPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                createUser(email, password)
            } else {
                Toast.makeText(this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show()
            }
        }

        userEmailsListView.setOnItemLongClickListener { _, _, position, _ ->
            val emailToDelete = userEmails[position]
            deleteUser(emailToDelete)
            true
        }

        viewRequestsButton.setOnClickListener {
            val intent = Intent(this, SignupRequestsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUser(email: String, password: String) {
        val currentUser = auth.currentUser
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
                        Toast.makeText(this, "User created successfully: ${response.getString("message")}", Toast.LENGTH_SHORT).show()
                        fetchUserEmails()
                    },
                    { error ->
                        Log.e("ManageUsersActivity", "Error creating user", error)
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
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserEmails() {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                val queue = Volley.newRequestQueue(this)

                val jsonArrayRequest = object : JsonArrayRequest(
                    Request.Method.GET, fetchUsersUrl, null,
                    { response ->
                        userEmails.clear()
                        for (i in 0 until response.length()) {
                            val user = response.getJSONObject(i)
                            val email = response.getJSONObject(i).getString("email")
                            val claims = user.optJSONObject("claims") ?: JSONObject()

                            if (!claims.optBoolean("isAdmin", false)) {
                                userEmails.add(email)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    },
                    { error ->
                        Log.e("ManageUsersActivity", "Error fetching users", error)
                        Toast.makeText(this, "Failed to retrieve user list", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return mapOf("Authorization" to "Bearer $idToken")
                    }
                }

                queue.add(jsonArrayRequest)
            } else {
                Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUser(email: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                val queue = Volley.newRequestQueue(this)
                val jsonObject = JSONObject().apply { put("email", email) }

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST, deleteUserUrl, jsonObject,
                    { response ->
                        Toast.makeText(this, "User deleted successfully: ${response.getString("message")}", Toast.LENGTH_SHORT).show()
                        fetchUserEmails()
                    },
                    { error ->
                        Log.e("ManageUsersActivity", "Error deleting user", error)
                        Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
