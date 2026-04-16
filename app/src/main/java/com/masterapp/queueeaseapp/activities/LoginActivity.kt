package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.utils.userFacingNetworkMessage
import com.masterapp.queueeaseapp.model.AuthResponse
import com.masterapp.queueeaseapp.model.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)

            RetrofitClient.api.login(request)
                .enqueue(object : Callback<AuthResponse> {

                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {

                        if (response.isSuccessful && response.body() != null) {

                            val body = response.body()!!

                            val token = body.token
                            val userId = body.userId
                            val role = body.role   // 🔥 VERY IMPORTANT

                            // 🔥 SAVE DATA
                            val pref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
                            pref.edit()
                                .putString("TOKEN", token)
                                .putLong("USER_ID", userId)
                                .putString("ROLE", role)   // 🔥 SAVE ROLE
                                .apply()

                            Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()

                            // 🔥 ROLE BASED NAVIGATION
                            if (role == "ADMIN") {
                                startActivity(
                                    Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                                )
                            } else {
                                startActivity(
                                    Intent(this@LoginActivity, UserDashboardActivity::class.java)
                                )
                            }

                            finish()

                        } else {
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            t.userFacingNetworkMessage(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}