package com.masterapp.queueeaseapp

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

// 🔐 Login Screen
@Composable
fun LoginScreen(onLoginSuccess: (Long) -> Unit) {
    Button(onClick = { onLoginSuccess(1L) }) {
        Text("Login")
    }
}

// 🏢 Center Screen
@Composable
fun CenterListScreen(userId: Long, onCenterSelected: () -> Unit) {
    Button(onClick = { onCenterSelected() }) {
        Text("Select Center")
    }
}

// 📊 Status Screen
@Composable
fun StatusScreen(userId: Long) {
    Text("Status Screen for userId: $userId")
}