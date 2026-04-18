package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.masterapp.queueeaseapp.home.HomeUiState
import com.masterapp.queueeaseapp.home.HomeViewModel
import com.masterapp.queueeaseapp.utils.SessionManager

class UserDashboardActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setContent {
            // Use our enhanced CenterListScreen Composable
            EnhancedCenterListScreen(viewModel)
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Handle state updates in our enhanced screen
                }
            }
        }
    }
}

@Composable
private fun EnhancedCenterListScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val userId = SessionManager.getUserId() ?: -1L
    
    // Check if we're in admin mode for queue management
    val adminMode = remember { 
        (context as? UserDashboardActivity)?.intent?.getBooleanExtra("adminMode", false) ?: false 
    }
    val purpose = remember { 
        (context as? UserDashboardActivity)?.intent?.getStringExtra("purpose") ?: "" 
    }
    
    Log.d("UserDashboardActivity", "DEBUG - EnhancedCenterListScreen started")
    Log.d("UserDashboardActivity", "DEBUG - Session token: ${SessionManager.getToken()}")
    Log.d("UserDashboardActivity", "DEBUG - Session userId: $userId")
    Log.d("UserDashboardActivity", "DEBUG - Session isLoggedIn: ${SessionManager.isLoggedIn()}")
    Log.d("UserDashboardActivity", "DEBUG - Admin mode: $adminMode, Purpose: $purpose")
    
    // Determine role based on admin mode
    val role = if (adminMode) "ADMIN" else "USER"
    
    // Use the actual CenterListScreen with real API data
    com.masterapp.queueeaseapp.ui.CenterListScreen(
        userId = userId,
        role = role,
        onCenterClick = { centerId ->
            Log.d("UserDashboardActivity", "=================================================")
            Log.d("UserDashboardActivity", "DEBUG - CENTER CLICKED - centerId: $centerId")
            Log.d("UserDashboardActivity", "DEBUG - About to start QueueListActivity with centerId: $centerId")
            Log.d("UserDashboardActivity", "DEBUG - Session state before navigation: token=${SessionManager.getToken()}, userId=${SessionManager.getUserId()}, isLoggedIn=${SessionManager.isLoggedIn()}")
            
            if (centerId != -1L) {
                Log.d("NAV", "Sending centerId=$centerId, role=$role")
                
                val intent = Intent(context, QueueListActivity::class.java)
                intent.putExtra("centerId", centerId)
                intent.putExtra("centerName", "Center $centerId")
                intent.putExtra("role", role)
                intent.putExtra("userId", userId)
                
                Log.d("UserDashboardActivity", "DEBUG - Creating intent with: centerId=$centerId, role=$role")
                Log.d("UserDashboardActivity", "DEBUG - Intent extras: ${intent.extras}")
                Log.d("UserDashboardActivity", "DEBUG - Starting QueueListActivity")
                context.startActivity(intent)
                Log.d("UserDashboardActivity", "DEBUG - QueueListActivity started successfully")
            } else {
                Log.e("NAV", "INVALID CENTER ID: centerId=$centerId")
                Log.e("UserDashboardActivity", "ERROR - INVALID CENTER ID: centerId=$centerId")
                Log.e("UserDashboardActivity", "ERROR - This means the center data has invalid ID or center is not properly loaded")
                Toast.makeText(context, "Invalid center selected (ID: $centerId). Please try again.", Toast.LENGTH_LONG).show()
            }
            Log.d("UserDashboardActivity", "=================================================")
        },
        onAddCenterClick = { /* User cannot add centers */ }
    )
}