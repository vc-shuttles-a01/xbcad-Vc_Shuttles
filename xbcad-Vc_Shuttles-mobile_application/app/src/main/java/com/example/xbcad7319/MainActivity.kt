package com.example.xbcad7319


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.xbcad7319.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*

        // Set up bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set the default fragment
        loadFragment(HomeFragment())

        // Handle bottom navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                /*R.id.navigation_schedule -> {
                    loadFragment(ScheduleFragment())
                    true
                }
                R.id.navigation_bookings -> {
                    loadFragment(BookingsFragment())
                    true
                }
                R.id.navigation_notifications -> {
                    loadFragment(NotificationsFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }*/
                else -> false
            }
        }
    }

    // Function to load a fragment
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }*/
    }
}
