package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    lateinit var role: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        role = intent.getStringExtra("ROLE") ?: "USER"

        val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {

            val name = findViewById<EditText>(R.id.etName).text.toString()
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(name, email, password, role)

            RetrofitClient.api.register(request)
                .enqueue(object : Callback<Map<String, Any>> {

                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Registered", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
                })
        }
    }
}