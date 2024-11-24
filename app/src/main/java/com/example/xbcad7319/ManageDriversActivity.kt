package com.example.xbcad7319

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

class ManageDriversActivity : AppCompatActivity() {

    private lateinit var addDriverButton: Button
    private lateinit var driverEmailEditText: EditText
    private lateinit var driverEmailsListView: ListView
    private lateinit var auth: FirebaseAuth
    //private var serverUrl = "http://10.0.2.2:3012/admin/setDriver"
    //private var fetchDriversUrl = "http://10.0.2.2:3012/getDrivers"
    //private var deleteDriverUrl = "http://10.0.2.2:3012/admin/deleteUser"


    private var serverUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/setDriver" // Adjust this endpoint if necessary
    private var fetchDriversUrl = "https://api-qeqahdzppa-uc.a.run.app/getDrivers" // Endpoint to fetch regular users
    private var deleteDriverUrl = "https://api-qeqahdzppa-uc.a.run.app/admin/deleteUser" // Endpoint to delete a user


    private val driverEmails = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_drivers)

        auth = FirebaseAuth.getInstance()
        addDriverButton = findViewById(R.id.addDriverButton)
        driverEmailEditText = findViewById(R.id.driverEmailEditText)
        driverEmailsListView = findViewById(R.id.driverEmailsListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, driverEmails)
        driverEmailsListView.adapter = adapter

        fetchDriverEmails()

        addDriverButton.setOnClickListener {
            val email = driverEmailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                assignDriverRole(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        driverEmailsListView.setOnItemLongClickListener { _, _, position, _ ->
            val emailToDelete = driverEmails[position]
            deleteDriver(emailToDelete)
            true
        }
    }

    private fun assignDriverRole(email: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                sendDriverRequest(idToken, email)
            } else {
                Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Log.e("ManageDriversActivity", "Error fetching token", it)
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDriverRequest(idToken: String, email: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("email", email)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, serverUrl, jsonObject,
            { response ->
                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                fetchDriverEmails()
            },
            { error ->
                Log.e("ManageDriversActivity", "Error setting driver role", error)
                Toast.makeText(this, "Failed to grant driver privileges", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mapOf("Authorization" to "Bearer $idToken")
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun fetchDriverEmails() {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                val queue = Volley.newRequestQueue(this)

                val jsonArrayRequest = object : JsonArrayRequest(
                    Request.Method.GET, fetchDriversUrl, null,
                    { response ->
                        driverEmails.clear()
                        for (i in 0 until response.length()) {
                            val email = response.getJSONObject(i).getString("email")
                            driverEmails.add(email)
                        }
                        adapter.notifyDataSetChanged()
                    },
                    { error ->
                        Log.e("ManageDriversActivity", "Error fetching drivers", error)
                        Toast.makeText(this, "Failed to retrieve driver list", Toast.LENGTH_SHORT).show()
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
            Log.e("ManageDriversActivity", "Error fetching token", it)
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDriver(email: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                val queue = Volley.newRequestQueue(this)
                val jsonObject = JSONObject().apply {
                    put("email", email)
                }

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.DELETE, deleteDriverUrl, jsonObject,
                    { response ->
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                        fetchDriverEmails() // Refresh list after deletion
                    },
                    { error ->
                        Log.e("ManageDriversActivity", "Error deleting driver", error)
                        Toast.makeText(this, "Failed to delete driver", Toast.LENGTH_SHORT).show()
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
            Log.e("ManageDriversActivity", "Error fetching token", it)
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
