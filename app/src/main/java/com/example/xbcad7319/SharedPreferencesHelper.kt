package com.example.xbcad7319

import android.content.Context

class SharedPreferencesHelper(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
        editor.putString("username", user.username)
        editor.putString("password", user.password)
        editor.apply()
    }

    fun getUser(): User? {
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)

        return if (username != null && password != null) {
            User(username, password)
        } else {
            null
        }
    }

    fun clearUser() {
        sharedPreferences.edit().clear().apply()
    }
}
