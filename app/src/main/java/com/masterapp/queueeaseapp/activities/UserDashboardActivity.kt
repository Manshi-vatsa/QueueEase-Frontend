package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    
    Log.d("UserDashboardActivity", "DEBUG - Using actual CenterListScreen with userId: $userId")
    
    // Use the actual CenterListScreen with real API data
    com.masterapp.queueeaseapp.ui.CenterListScreen(
        userId = userId,
        role = "USER",
        onCenterClick = { centerId ->
            Log.d("UserDashboardActivity", "DEBUG - Center clicked from list, centerId: $centerId")
            val intent = Intent(context, QueueListActivity::class.java)
            intent.putExtra("centerId", centerId)
            intent.putExtra("centerName", "Center $centerId")
            intent.putExtra("role", "USER")
            context.startActivity(intent)
        },
        onAddCenterClick = { /* User cannot add centers */ }
    )
}