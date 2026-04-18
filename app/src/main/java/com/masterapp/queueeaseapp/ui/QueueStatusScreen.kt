package com.masterapp.queueeaseapp.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun QueueStatusScreen(
    userId: Long,
    centerId: Long
) {

    var data by remember { mutableStateOf<QueueStatusResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isInQueue by remember { mutableStateOf(false) }
    var joinQueueError by remember { mutableStateOf<String?>(null) }
    
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gradient_offset"
    )
    
    // Pulse animation for live indicator
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_alpha"
    )

    LaunchedEffect(centerId, userId) {
        isLoading = true
        while (true) {
            Log.d("QUEUE_API", "SCREEN OPENED")
            Log.d("QUEUE_API", "Calling API with centerId = $centerId, userId = $userId")
            Log.d("QUEUE_API", "TEST API CALL STARTED")
            RetrofitClient.api.getStatus(userId, centerId)
                .enqueue(object : Callback<QueueStatusResponse> {
                    override fun onResponse(
                        call: Call<QueueStatusResponse>,
                        response: Response<QueueStatusResponse>
                    ) {
                        Log.d("QUEUE_API", "Response code = ${response.code()}")
                        Log.d("QUEUE_API", "Body = ${response.body()}")
                        Log.d("QUEUE_API", "Error = ${response.errorBody()?.string()}")
                        
                        isLoading = false
                        
                        if (response.isSuccessful && response.body() != null) {
                            // User is in queue
                            data = response.body()
                            isInQueue = true
                            joinQueueError = null
                            Log.d("QUEUE_API", "User is in queue: ${data?.queueNumber}")
                        } else if (response.code() == 404) {
                            // User is not in queue (404 = not found)
                            data = null
                            isInQueue = false
                            joinQueueError = null
                            Log.d("QUEUE_API", "User is NOT in queue")
                        } else {
                            // Other error
                            Log.e("QUEUE_API", "Error checking queue status: ${response.code()}")
                            joinQueueError = "Error checking queue status: ${response.code()}"
                        }
                    }
                    override fun onFailure(call: Call<QueueStatusResponse>, t: Throwable) {
                        Log.d("QUEUE_API", "FAILED: ${t.message}")
                        isLoading = false
                        joinQueueError = "Network error: ${t.message}"
                    }
                })
            delay(5000) // refresh every 5 sec
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2),
                        Color(0xFFF093FB)
                    ),
                    startY = gradientOffset,
                    endY = gradientOffset + 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header with live indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue Status",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                Color.Red.copy(alpha = pulseAlpha),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                LoadingQueueStatus()
            } else if (isInQueue && data != null) {
                QueueStatusContent(data = data!!)
            } else if (!isInQueue) {
                NotInQueueContent(userId = userId, centerId = centerId)
            } else if (joinQueueError != null) {
                ErrorContent(error = joinQueueError!!)
            } else {
                LoadingQueueStatus()
            }
        }
    }
}

@Composable
private fun LoadingQueueStatus() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading queue status...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun QueueStatusContent(data: QueueStatusResponse) {
    // Main Token Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Token",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your Token",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "#${data.queueNumber}",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    // Status Cards Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCard(
            title = "Now Serving",
            value = "${data.currentServing}",
            icon = Icons.Default.Person,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        StatusCard(
            title = "People Ahead",
            value = "${data.peopleAhead}",
            icon = Icons.Default.Person,
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    // Wait Time Card
    WaitTimeCard(waitTime = data.estimatedWaitTime)
    
    Spacer(modifier = Modifier.height(20.dp))
    
    // Recommendation Card
    RecommendationCard(recommendation = data.recommendation)
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun WaitTimeCard(waitTime: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Wait Time",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Estimated Wait Time",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "$waitTime minutes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Recommendation",
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recommendation",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF92400E)
                    )
                )
                Text(
                    text = recommendation ?: "No recommendation available",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1F2937)
                    )
                )
            }
        }
    }
}

@Composable
private fun NotInQueueContent(userId: Long, centerId: Long) {
    var isJoining by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Join Queue",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You're not in the queue",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join the queue to get your token",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    Log.d("NotInQueueContent", "DEBUG - Join Queue clicked: userId=$userId, centerId=$centerId")
                    isJoining = true
                    
                    RetrofitClient.api.joinQueue(userId, centerId)
                        .enqueue(object : Callback<com.masterapp.queueeaseapp.model.BookingResponse> {
                            override fun onResponse(
                                call: Call<com.masterapp.queueeaseapp.model.BookingResponse>,
                                response: Response<com.masterapp.queueeaseapp.model.BookingResponse>
                            ) {
                                isJoining = false
                                Log.d("NotInQueueContent", "DEBUG - Join Queue response: ${response.code()}, body: ${response.body()}")
                                
                                if (response.isSuccessful) {
                                    Log.d("NotInQueueContent", "DEBUG - Successfully joined queue")
                                    // The UI will automatically update on the next API poll
                                } else {
                                    Log.e("NotInQueueContent", "ERROR - Failed to join queue: ${response.code()}")
                                }
                            }
                            
                            override fun onFailure(call: Call<com.masterapp.queueeaseapp.model.BookingResponse>, t: Throwable) {
                                isJoining = false
                                Log.e("NotInQueueContent", "ERROR - Join Queue failed: ${t.message}", t)
                            }
                        })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                ),
                enabled = !isJoining
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Join Queue",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Join Queue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Please try again",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
        }
    }
}