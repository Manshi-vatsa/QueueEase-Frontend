package com.masterapp.queueeaseapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.LoginRequest
import com.masterapp.queueeaseapp.model.LoginResponse
import com.masterapp.queueeaseapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(onLogin: (Long) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "QueueEase",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 📧 Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔒 Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ❌ Error Message
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 🔘 Login Button
        Button(
            onClick = {

                RetrofitClient.api.login(LoginRequest(email, password))
                    .enqueue(object : Callback<LoginResponse> {

                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful && response.body() != null) {

                                // ✅ Save token
                                SessionManager.token =
                                    "Bearer ${response.body()!!.token}"

                                // ✅ Move to next screen
                                onLogin(1L)

                            } else {
                                errorMessage = "Invalid credentials"
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            errorMessage = "Network Error"
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}