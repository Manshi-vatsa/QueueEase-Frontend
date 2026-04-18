package com.masterapp.queueeaseapp.ui

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import android.util.Log
import com.masterapp.queueeaseapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AdminScreen(centerId: Long) {
    var currentStep by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    // State for each step
    var centers by remember { mutableStateOf<List<com.masterapp.queueeaseapp.model.CenterResponse>>(emptyList()) }
    var selectedCenter by remember { mutableStateOf<com.masterapp.queueeaseapp.model.CenterResponse?>(null) }
    var queueUsers by remember { mutableStateOf<List<com.masterapp.queueeaseapp.model.QueueUser>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<com.masterapp.queueeaseapp.model.QueueUser?>(null) }
    
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gradient_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF3730A3),
                        Color(0xFF6366F1)
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
            // Header with step indicator
            AdminHeaderFixed(centerId = centerId, currentStep = currentStep)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step content - 9-step flow as specified
            when (currentStep) {
                // STEP 1: Admin clicks "Serve Next"
                1 -> Step1ServeNextFixed(onNext = { currentStep = 2 })
                
                // STEP 2: App opens Center List screen (fetch from API) - GET /centers
                2 -> Step2CenterSelectionFixed(
                    centers = centers,
                    selectedCenter = selectedCenter,
                    isLoading = isLoading,
                    onCenterSelected = { center ->
                        selectedCenter = center
                        currentStep = 3
                    },
                    onLoadCenters = {
                        isLoading = true
                        RetrofitClient.api.getCenters()
                            .enqueue(object : Callback<List<com.masterapp.queueeaseapp.model.CenterResponse>> {
                                override fun onResponse(
                                    call: Call<List<com.masterapp.queueeaseapp.model.CenterResponse>>,
                                    response: Response<List<com.masterapp.queueeaseapp.model.CenterResponse>>
                                ) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        centers = response.body() ?: emptyList()
                                        Log.d("AdminScreenFixed", "STEP 2 - Loaded ${centers.size} centers")
                                    } else {
                                        message = "Failed to load centers"
                                    }
                                }
                                override fun onFailure(call: Call<List<com.masterapp.queueeaseapp.model.CenterResponse>>, t: Throwable) {
                                    isLoading = false
                                    message = "Network error: ${t.message}"
                                }
                            })
                    }
                )
                
                // STEP 3: Admin selects a center
                3 -> Step3CenterConfirmationFixed(
                    selectedCenter = selectedCenter,
                    onConfirm = { currentStep = 4 },
                    onBack = { currentStep = 2 }
                )
                
                // STEP 4: App fetches queue users for that center - GET /queue/users?centerId={id}
                4 -> Step4UserSelectionFixed(
                    queueUsers = queueUsers,
                    selectedUser = selectedUser,
                    isLoading = isLoading,
                    centerName = selectedCenter?.name ?: "Unknown",
                    onUserSelected = { user ->
                        selectedUser = user
                        currentStep = 5
                    },
                    onLoadUsers = {
                        isLoading = true
                        RetrofitClient.api.getQueueUsers(selectedCenter?.id ?: centerId)
                            .enqueue(object : Callback<List<com.masterapp.queueeaseapp.model.QueueUser>> {
                                override fun onResponse(
                                    call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>,
                                    response: Response<List<com.masterapp.queueeaseapp.model.QueueUser>>
                                ) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        queueUsers = response.body() ?: emptyList()
                                        Log.d("AdminScreenFixed", "STEP 4 - Loaded ${queueUsers.size} queue users for center ${selectedCenter?.id}")
                                    } else {
                                        message = "Failed to load queue users"
                                    }
                                }
                                override fun onFailure(call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>, t: Throwable) {
                                    isLoading = false
                                    message = "Network error: ${t.message}"
                                }
                            })
                    }
                )
                
                // STEP 5: Admin selects ONE user
                5 -> Step5UserConfirmationFixed(
                    selectedCenter = selectedCenter,
                    selectedUser = selectedUser,
                    onConfirm = { currentStep = 6 },
                    onBack = { currentStep = 4 }
                )
                
                // STEP 6: Admin clicks "Serve This User"
                6 -> Step6ServeUserFixed(
                    selectedCenter = selectedCenter,
                    selectedUser = selectedUser,
                    isLoading = isLoading,
                    onServeUser = { userId, centerId ->
                        isLoading = true
                        val request = com.masterapp.queueeaseapp.model.ServeUserRequest(userId, centerId)
                        RetrofitClient.api.serveUser(request)
                            .enqueue(object : Callback<com.masterapp.queueeaseapp.model.QueueStatusResponse> {
                                override fun onResponse(
                                    call: Call<com.masterapp.queueeaseapp.model.QueueStatusResponse>,
                                    response: Response<com.masterapp.queueeaseapp.model.QueueStatusResponse>
                                ) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        message = "Successfully served user"
                                        currentStep = 7
                                        Log.d("AdminScreenFixed", "STEP 6 - User served successfully")
                                        
                                        // Immediately refresh queue status after serving user
                                        RetrofitClient.api.getQueueUsers(selectedCenter?.id ?: centerId)
                                            .enqueue(object : Callback<List<com.masterapp.queueeaseapp.model.QueueUser>> {
                                                override fun onResponse(
                                                    call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>,
                                                    response: Response<List<com.masterapp.queueeaseapp.model.QueueUser>>
                                                ) {
                                                    queueUsers = response.body() ?: emptyList()
                                                    Log.d("AdminScreenFixed", "STEP 6 - Queue users refreshed after serving: ${queueUsers.size} users")
                                                }
                                                override fun onFailure(call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>, t: Throwable) {
                                                    Log.e("AdminScreenFixed", "STEP 6 - Failed to refresh queue users: ${t.message}")
                                                }
                                            })
                                    } else {
                                        message = "Failed to serve user: ${response.code()}"
                                    }
                                }
                                override fun onFailure(call: Call<com.masterapp.queueeaseapp.model.QueueStatusResponse>, t: Throwable) {
                                    isLoading = false
                                    message = "Network error: ${t.message}"
                                }
                            })
                    }
                )
                
                // STEP 7: Backend processing complete, show success
                7 -> Step7SuccessFixed(
                    selectedCenter = selectedCenter,
                    selectedUser = selectedUser,
                    onContinue = { currentStep = 8 }
                )
                
                // STEP 8: Frontend must refresh queue list immediately
                8 -> Step8RefreshFixed(
                    selectedCenter = selectedCenter,
                    isLoading = isLoading,
                    onRefresh = {
                        isLoading = true
                        // Refresh queue users for the selected center
                        RetrofitClient.api.getQueueUsers(selectedCenter?.id ?: centerId)
                            .enqueue(object : Callback<List<com.masterapp.queueeaseapp.model.QueueUser>> {
                                override fun onResponse(
                                    call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>,
                                    response: Response<List<com.masterapp.queueeaseapp.model.QueueUser>>
                                ) {
                                    isLoading = false
                                    queueUsers = response.body() ?: emptyList()
                                    currentStep = 9
                                    Log.d("AdminScreenFixed", "STEP 8 - Refreshed queue users, found ${queueUsers.size} users")
                                }
                                override fun onFailure(call: Call<List<com.masterapp.queueeaseapp.model.QueueUser>>, t: Throwable) {
                                    isLoading = false
                                    message = "Failed to refresh queue: ${t.message}"
                                    currentStep = 9
                                }
                            })
                    }
                )
                
                // STEP 9: Frontend must refresh status API and complete
                9 -> Step9CompleteFixed(
                    selectedCenter = selectedCenter,
                    selectedUser = selectedUser,
                    onStartOver = {
                        currentStep = 1
                        selectedCenter = null
                        selectedUser = null
                        queueUsers = emptyList()
                        message = ""
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Message Display
            if (message.isNotEmpty()) {
                AdminMessageCardFixed(message = message, onDismiss = { message = "" })
            }
        }
    }
}

@Composable
private fun AdminHeaderFixed(centerId: Long, currentStep: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF10B981),
                            Color(0xFF059669)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Admin",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Admin Control Panel",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Center ID: $centerId | Step $currentStep/9",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

// STEP 1: Admin clicks "Serve Next"
@Composable
private fun Step1ServeNextFixed(onNext: () -> Unit) {
    AdminActionCardFixed(
        icon = Icons.Default.PlayArrow,
        title = "Serve Next Customer",
        description = "Start the process to serve the next person in queue",
        buttonText = "Start Process",
        onButtonClick = onNext,
        iconColor = Color(0xFF10B981)
    )
}

// STEP 2: App opens Center List screen (fetch from API) - GET /centers
@Composable
private fun Step2CenterSelectionFixed(
    centers: List<com.masterapp.queueeaseapp.model.CenterResponse>,
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    isLoading: Boolean,
    onCenterSelected: (com.masterapp.queueeaseapp.model.CenterResponse) -> Unit,
    onLoadCenters: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadCenters()
    }
    
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
                .padding(24.dp)
        ) {
            Text(
                text = "Select Service Center",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            } else if (centers.isEmpty()) {
                Text(
                    text = "No centers available",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(centers) { center ->
                        CenterSelectionItemFixed(
                            center = center,
                            isSelected = selectedCenter?.id == center.id,
                            onSelect = { onCenterSelected(center) }
                        )
                    }
                }
            }
        }
    }
}

// STEP 3: Admin selects a center
@Composable
private fun Step3CenterConfirmationFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    AdminActionCardFixed(
        icon = Icons.Default.CheckCircle,
        title = "Center Selected",
        description = "Center: ${selectedCenter?.name ?: "Unknown"}",
        buttonText = "Continue",
        onButtonClick = onConfirm,
        iconColor = Color(0xFF10B981)
    )
}

// STEP 4: App fetches queue users for that center - GET /queue/users?centerId={id}
@Composable
private fun Step4UserSelectionFixed(
    queueUsers: List<com.masterapp.queueeaseapp.model.QueueUser>,
    selectedUser: com.masterapp.queueeaseapp.model.QueueUser?,
    isLoading: Boolean,
    centerName: String,
    onUserSelected: (com.masterapp.queueeaseapp.model.QueueUser) -> Unit,
    onLoadUsers: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadUsers()
    }
    
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
                .padding(24.dp)
        ) {
            Text(
                text = "Select Customer to Serve",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Queue for: $centerName",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            } else if (queueUsers.isEmpty()) {
                Text(
                    text = "No customers in queue",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(queueUsers) { user ->
                        UserSelectionItemFixed(
                            user = user,
                            isSelected = selectedUser?.userId == user.userId,
                            onSelect = { onUserSelected(user) }
                        )
                    }
                }
            }
        }
    }
}

// STEP 5: Admin selects ONE user
@Composable
private fun Step5UserConfirmationFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    selectedUser: com.masterapp.queueeaseapp.model.QueueUser?,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
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
                Icons.Default.Person,
                contentDescription = "User Selected",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "User Selected",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Center: ${selectedCenter?.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "User ID: ${selectedUser?.userId ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Queue Number: ${selectedUser?.queueNumber ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Back")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

// STEP 6: Admin clicks "Serve This User"
@Composable
private fun Step6ServeUserFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    selectedUser: com.masterapp.queueeaseapp.model.QueueUser?,
    isLoading: Boolean,
    onServeUser: (Long, Long) -> Unit
) {
    AdminActionCardFixed(
        icon = Icons.Default.PlayArrow,
        title = "Serve This User",
        description = "User ID: ${selectedUser?.userId ?: "Unknown"} - Queue #${selectedUser?.queueNumber ?: "?"}",
        buttonText = "Serve Now",
        onButtonClick = {
            selectedUser?.userId?.let { userId ->
                selectedCenter?.id?.let { centerId ->
                    onServeUser(userId, centerId)
                }
            }
        },
        iconColor = Color(0xFF10B981),
        isLoading = isLoading
    )
}

// STEP 7: Backend processing complete, show success
@Composable
private fun Step7SuccessFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    selectedUser: com.masterapp.queueeaseapp.model.QueueUser?,
    onContinue: () -> Unit
) {
    AdminActionCardFixed(
        icon = Icons.Default.CheckCircle,
        title = "User Served Successfully",
        description = "User ID: ${selectedUser?.userId ?: "Unknown"} has been served",
        buttonText = "Continue",
        onButtonClick = onContinue,
        iconColor = Color(0xFF10B981)
    )
}

// STEP 8: Frontend must refresh queue list immediately
@Composable
private fun Step8RefreshFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    AdminActionCardFixed(
        icon = Icons.Default.Refresh,
        title = "Refresh Queue Data",
        description = "Updating queue list for ${selectedCenter?.name ?: "Unknown"}",
        buttonText = "Refresh Now",
        onButtonClick = onRefresh,
        iconColor = Color(0xFF6366F1),
        isLoading = isLoading
    )
}

// STEP 9: Frontend must refresh status API and complete
@Composable
private fun Step9CompleteFixed(
    selectedCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    selectedUser: com.masterapp.queueeaseapp.model.QueueUser?,
    onStartOver: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD1FAE5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Complete",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Process Complete!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Successfully served user ID: ${selectedUser?.userId ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF065F46)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStartOver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Start Over",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Serve Next Customer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun AdminActionCardFixed(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    iconColor: Color,
    isLoading: Boolean = false
) {
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
                icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        icon,
                        contentDescription = buttonText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonText,
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
private fun CenterSelectionItemFixed(
    center: com.masterapp.queueeaseapp.model.CenterResponse,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onSelect() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF6366F1)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = center.name ?: "Unknown Center",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "ID: ${center.id}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
private fun UserSelectionItemFixed(
    user: com.masterapp.queueeaseapp.model.QueueUser,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onSelect() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF6366F1)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "User ID: ${user.userId}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Queue #${user.queueNumber} | User ID: ${user.userId}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
private fun AdminMessageCardFixed(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (message.startsWith("Successfully")) 
                Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (message.startsWith("Successfully")) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = "Status",
                tint = if (message.startsWith("Successfully")) 
                    Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (message.startsWith("Successfully")) 
                        Color(0xFF065F46) else Color(0xFF991B1B),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (message.startsWith("Successfully")) 
                        Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}
