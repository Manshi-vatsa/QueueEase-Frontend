package com.masterapp.queueeaseapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterapp.queueeaseapp.queue.JoinQueueState
import com.masterapp.queueeaseapp.queue.QueueUiState
import com.masterapp.queueeaseapp.queue.QueueViewModel
import com.masterapp.queueeaseapp.utils.SessionManager
import com.masterapp.queueeaseapp.ui.QueueStatusScreen
import com.masterapp.queueeaseapp.ui.AdminScreen
import kotlinx.coroutines.launch

class QueueListActivity : ComponentActivity() {

    private lateinit var viewModel: QueueViewModel
    private var centerId: Long = -1L
    private var userId: Long = -1L
    private var role: String = "USER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        centerId = intent.getLongExtra("centerId", -1L)
        userId = SessionManager.getUserId() ?: -1L
        role = intent.getStringExtra("role") ?: "USER"
        
        // DEBUG: Log the values being used
        Log.d("QueueListActivity", "DEBUG - centerId: $centerId, userId: $userId, role: $role")
        Log.d("QueueListActivity", "DEBUG - SessionManager token: ${SessionManager.getToken()}")
        Log.d("QueueListActivity", "DEBUG - SessionManager isLoggedIn: ${SessionManager.isLoggedIn()}")
        
        if (centerId == -1L || userId == -1L) {
            Log.e("QueueListActivity", "ERROR - Invalid centerId: $centerId or userId: $userId")
            Toast.makeText(this, "Invalid center or user session. centerId=$centerId, userId=$userId", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            // UI UPDATED - Temporary visible change
            Text(
                text = "🎉 UI UPDATED 🎉",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color.Yellow,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Show appropriate screen based on role
            if (role == "ADMIN") {
                // Show Admin Screen for admin users
                AdminScreen(centerId = centerId)
            } else {
                // Show Queue Status Screen for regular users
                QueueStatusScreen(
                    userId = userId,
                    centerId = centerId
                )
            }
        }

        viewModel = ViewModelProvider(this)[QueueViewModel::class.java]
        observeUiState()
        observeJoinState()
        viewModel.loadQueue(userId, centerId)
    }

    override fun onStart() {
        super.onStart()
        if (centerId != -1L && userId != -1L) {
            viewModel.startAutoRefresh(userId, centerId)
        }
    }

    override fun onStop() {
        viewModel.stopAutoRefresh()
        super.onStop()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Handle UI state updates
                }
            }
        }
    }

    private fun observeJoinState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.joinState.collect { state ->
                    when (state) {
                        is JoinQueueState.Success -> {
                            Toast.makeText(
                                this@QueueListActivity,
                                "Joined queue. Your number: ${state.booking.queueNumber}",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetJoinState()
                        }
                        is JoinQueueState.Error -> {
                            Toast.makeText(this@QueueListActivity, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetJoinState()
                        }
                        else -> { /* Idle or Loading states */ }
                    }
                }
            }
        }
    }
}