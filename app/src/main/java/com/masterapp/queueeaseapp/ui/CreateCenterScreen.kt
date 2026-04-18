package com.masterapp.queueeaseapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.util.Log
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.CenterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CreateCenterScreen(
    onCenterCreated: () -> Unit,
    onBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var createdCenter by remember { mutableStateOf<com.masterapp.queueeaseapp.model.CenterResponse?>(null) }
    
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header with step indicator
            CreateCenterHeader(currentStep = currentStep)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step content - 6-step flow as specified
            when (currentStep) {
                // STEP 1: Admin clicks "Create Center"
                1 -> Step1CreateCenterStart(onNext = { currentStep = 2 })
                
                // STEP 2: Open form screen/modal with fields
                2 -> Step2CenterNameForm(
                    name = name,
                    onNameChange = { name = it },
                    onNext = { currentStep = 3 },
                    onBack = onBack
                )
                
                // STEP 3: Admin fills form and clicks "Submit" - Location
                3 -> Step3LocationForm(
                    location = location,
                    onLocationChange = { location = it },
                    onNext = { currentStep = 4 },
                    onBack = { currentStep = 2 }
                )
                
                // STEP 4: Admin fills form and clicks "Submit" - Type
                4 -> Step4TypeForm(
                    type = type,
                    onTypeChange = { type = it },
                    onNext = { currentStep = 5 },
                    onBack = { currentStep = 3 }
                )
                
                // STEP 5: Call backend API - POST /centers
                5 -> Step5SubmitCenter(
                    name = name,
                    location = location,
                    type = type,
                    isLoading = isLoading,
                    onSubmit = {
                        if (name.isEmpty() || location.isEmpty() || type.isEmpty()) {
                            errorMessage = "Please fill all fields"
                            return@Step5SubmitCenter
                        }
                        
                        isLoading = true
                        errorMessage = ""
                        
                        val center = CenterResponse(0, name, location, type)
                        
                        RetrofitClient.api.addCenter(center)
                            .enqueue(object : Callback<CenterResponse> {
                                override fun onResponse(
                                    call: Call<CenterResponse>,
                                    response: Response<CenterResponse>
                                ) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        createdCenter = response.body()
                                        currentStep = 6
                                        Log.d("CreateCenterScreen", "STEP 5 - Center created successfully")
                                    } else {
                                        errorMessage = "Failed to create center: ${response.code()}"
                                        Log.e("CreateCenterScreen", "ERROR - Failed to create center: ${response.code()}")
                                    }
                                }
                                
                                override fun onFailure(call: Call<CenterResponse>, t: Throwable) {
                                    isLoading = false
                                    errorMessage = "Network error: ${t.message}"
                                    Log.e("CreateCenterScreen", "ERROR - Network error: ${t.message}")
                                }
                            })
                    },
                    onBack = { currentStep = 4 }
                )
                
                // STEP 6: Show success message and refresh center list
                6 -> Step6Success(
                    createdCenter = createdCenter,
                    onComplete = {
                        // Refresh center list and complete
                        onCenterCreated()
                    },
                    onStartOver = {
                        // Reset and start over
                        currentStep = 1
                        name = ""
                        location = ""
                        type = ""
                        createdCenter = null
                        errorMessage = ""
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Error Message
            if (errorMessage.isNotEmpty()) {
                CreateCenterErrorMessage(message = errorMessage, onDismiss = { errorMessage = "" })
            }
        }
    }
}

// STEP 1: Admin clicks "Create Center"
@Composable
private fun Step1CreateCenterStart(onNext: () -> Unit) {
    CreateCenterActionCard(
        icon = Icons.Default.Add,
        title = "Create New Center",
        description = "Start the process to add a new service center to the system",
        buttonText = "Start Process",
        onButtonClick = onNext,
        iconColor = Color(0xFF10B981)
    )
}

// STEP 2: Open form screen/modal with fields - Center Name
@Composable
private fun Step2CenterNameForm(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
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
                .padding(24.dp)
        ) {
            Text(
                text = "Center Name",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter the name of the new service center",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {
                    Text(
                        text = "Center Name",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Center Name",
                        tint = Color(0xFF6366F1)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    enabled = name.isNotEmpty()
                ) {
                    Text("Next")
                }
            }
        }
    }
}

// STEP 3: Admin fills form and clicks "Submit" - Location
@Composable
private fun Step3LocationForm(
    location: String,
    onLocationChange: (String) -> Unit,
    onNext: () -> Unit,
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
                .padding(24.dp)
        ) {
            Text(
                text = "Center Location",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter the location address of the service center",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF6366F1)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    enabled = location.isNotEmpty()
                ) {
                    Text("Next")
                }
            }
        }
    }
}

// STEP 4: Admin fills form and clicks "Submit" - Type
@Composable
private fun Step4TypeForm(
    type: String,
    onTypeChange: (String) -> Unit,
    onNext: () -> Unit,
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
                .padding(24.dp)
        ) {
            Text(
                text = "Center Type",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Specify the type of service center (Hospital, Bank, Office, etc.)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = type,
                onValueChange = onTypeChange,
                label = {
                    Text(
                        text = "Center Type",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Center Type",
                        tint = Color(0xFF6366F1)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ),
                    enabled = type.isNotEmpty()
                ) {
                    Text("Next")
                }
            }
        }
    }
}

// STEP 5: Call backend API - POST /centers
@Composable
private fun Step5SubmitCenter(
    name: String,
    location: String,
    type: String,
    isLoading: Boolean,
    onSubmit: () -> Unit,
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
                Icons.Default.CheckCircle,
                contentDescription = "Confirm",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Confirm Center Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Name: $name",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: $location",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Type: $type",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Create Center")
                    }
                }
            }
        }
    }
}

// STEP 6: Show success message and refresh center list
@Composable
private fun Step6Success(
    createdCenter: com.masterapp.queueeaseapp.model.CenterResponse?,
    onComplete: () -> Unit,
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
                contentDescription = "Success",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Center Created Successfully!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            createdCenter?.let { center ->
                Text(
                    text = "${center.name} has been added to the system",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF065F46)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: ${center.id}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF065F46)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onStartOver,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF065F46)
                    )
                ) {
                    Text("Create Another")
                }
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun CreateCenterHeader(currentStep: Int) {
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
                    Icons.Default.Add,
                    contentDescription = "Create Center",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Create Service Center",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Step $currentStep/6",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun CreateCenterActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    iconColor: Color
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
                )
            ) {
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

@Composable
private fun CreateCenterErrorMessage(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEE2E2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}
