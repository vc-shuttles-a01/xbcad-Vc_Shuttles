package com.example.xbcad7319

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class AddAdminActivity : AppCompatActivity() {

    private lateinit var addAdminButton: Button
    private lateinit var adminEmailEditText: EditText
    private lateinit var auth: FirebaseAuth
    //private var serverUrl = "http://10.0.2.2:3012/setAdmin" // or the IP address of the server
    //private var serverUrl = "http://192.168.5.206:3012/setAdmin"  // Replace <YOUR_SERVER_IP> with your server's IP
    private var serverUrl = "https://api-qeqahdzppa-uc.a.run.app/setAdmin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_admin)

        auth = FirebaseAuth.getInstance()
        addAdminButton = findViewById(R.id.addAdminButton)
        adminEmailEditText = findViewById(R.id.adminEmailEditText)

        addAdminButton.setOnClickListener {
            val email = adminEmailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                assignAdminRole(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun assignAdminRole(email: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                sendAdminRequest(idToken, email)
            } else {
                Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Log.e("AddAdminActivity", "Error fetching token", it)
            Toast.makeText(this, "Error fetching token: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendAdminRequest(idToken: String, email: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject()
        jsonObject.put("email", email)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, serverUrl, jsonObject,
            { response ->
                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Log.e("AddAdminActivity", "Error setting admin role", error)
                Toast.makeText(this, "Failed to grant admin privileges", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $idToken"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }
}
