package com.masterapp.queueeaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.AuthResponse
import com.masterapp.queueeaseapp.model.LoginRequest
import com.masterapp.queueeaseapp.utils.SessionManager
import com.masterapp.queueeaseapp.utils.userFacingNetworkMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(onLogin: (Long) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Log.d("LOGIN_REQUEST", "Email: $email Password: $password")
        Text("QueueEase", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {

            Button(
                onClick = {

                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter email and password"
                        return@Button
                    }

                    isLoading = true

                    // ✅ FIXED: Use LoginRequest
                    val request = LoginRequest(email, password)

                    RetrofitClient.api.login(request)
                        .enqueue(object : Callback<AuthResponse> {

                            override fun onResponse(
                                call: Call<AuthResponse>,
                                response: Response<AuthResponse>
                            ) {

                                isLoading = false

                                if (response.isSuccessful) {

                                    val body = response.body()

                                    if (body != null) {

                                        Log.d("LOGIN_SUCCESS", "Token: ${body.token}")
                                        Log.d("LOGIN_SUCCESS", "UserId: ${body.userId}")

                                        // ✅ FIX: token nullable
                                        SessionManager.saveToken(body.token ?: "")

                                        onLogin(body.userId)

                                    } else {
                                        errorMessage = "Empty response"
                                    }

                                } else {
                                    errorMessage = "Login failed: ${response.code()}"
                                }
                            }

                            override fun onFailure(
                                call: Call<AuthResponse>,
                                t: Throwable
                            ) {
                                isLoading = false
                                errorMessage = t.userFacingNetworkMessage()
                                Log.e("LOGIN_FAILURE", t.message.toString())
                            }
                        })
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }
}