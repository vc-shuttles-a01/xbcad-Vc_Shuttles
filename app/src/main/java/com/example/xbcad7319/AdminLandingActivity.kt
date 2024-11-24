package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.functions.FirebaseFunctions

class AdminLandingActivity : AppCompatActivity() {

    private lateinit var addAdminButton: Button
    private lateinit var adminUserEmailEditText: EditText
    private lateinit var manageScheduleButton : Button
    private lateinit var logoutButton: Button
    private lateinit var startTripButton : Button
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewReportsButton: Button
    private lateinit var manageUsers: Button
    private lateinit var manageDrivers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_landing)

        addAdminButton = findViewById(R.id.addAdminButton)
        manageScheduleButton = findViewById(R.id.manageScheduleButton)
        logoutButton = findViewById(R.id.logoutButton)
        startTripButton = findViewById(R.id.startTripButton)
        viewReportsButton = findViewById(R.id.viewReportsButton)
        //adminUserEmailEditText = findViewById(R.id.adminUserIdEditText)
        manageUsers = findViewById(R.id.manageUsers)
        manageDrivers = findViewById(R.id.manageDrivers)


        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(this)

        addAdminButton.setOnClickListener {
            val intent = Intent(this, AddAdminActivity::class.java)
            startActivity(intent)
        }

        manageScheduleButton.setOnClickListener(){
            val intent = Intent(this, ScheduleManagementActivity::class.java)
            startActivity(intent)
        }

        manageUsers.setOnClickListener(){
            val intent = Intent(this, ManageUsersActivity::class.java)
            startActivity(intent)
        }

        manageDrivers.setOnClickListener(){
            val intent = Intent(this, ManageDriversActivity::class.java)
            startActivity(intent)
        }

        viewReportsButton.setOnClickListener(){
            val intent = Intent(this, AdminReportsActivity::class.java)
            startActivity(intent)
        }

        // Logout functionality
        logoutButton.setOnClickListener {
            // Clear user data from SharedPreferences
            sharedPreferencesHelper.clearUser()

            // Show a logout confirmation message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to the LoginPg activity
            val intent = Intent(this, LoginPg::class.java)
            startActivity(intent)

            // Finish the current activity to prevent back navigation
            finish()
        }

        startTripButton.setOnClickListener(){
            val intent = Intent(this, StartTripActivity::class.java)
            startActivity(intent)
        }
        

    }

    /*private fun setAdminClaim(email: String) {
        FirebaseFunctions.getInstance()
            .getHttpsCallable("addAdminRole")
            .call(hashMapOf("email" to email))
            .addOnSuccessListener {
                Toast.makeText(this, "Admin privileges granted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to grant admin privileges: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }*/
}
