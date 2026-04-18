package com.masterapp.queueeaseapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.CenterResponse
import com.masterapp.queueeaseapp.model.CrowdLevel
import com.masterapp.queueeaseapp.model.CrowdDensity
import com.masterapp.queueeaseapp.NotificationHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*

@Composable
fun CenterListScreen(
    userId: Long,
    role: String,
    onCenterClick: (Long) -> Unit,
    onAddCenterClick: () -> Unit
) {

    var centers by remember { mutableStateOf<List<CenterResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasLoadedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    // Mock crowd levels data
    var crowdLevels by remember { mutableStateOf<List<CrowdLevel>>(emptyList()) }
    
    // Load crowd levels
    crowdLevels = generateMockCrowdLevelsForCenters()
    
    // Generate crowd levels for centers (map centers to crowd levels)
    val centerCrowdLevels = centers.mapIndexed { index, center ->
        val crowdLevel = crowdLevels.getOrNull(index % crowdLevels.size) ?: crowdLevels.first()
        center to crowdLevel
    }

    LaunchedEffect(Unit) {
        isLoading = true
        RetrofitClient.api.getCenters()
            .enqueue(object : Callback<List<CenterResponse>> {

                override fun onResponse(
                    call: Call<List<CenterResponse>>,
                    response: Response<List<CenterResponse>>
                ) {
                    hasLoadedOnce = true
                    isLoading = false
                    if (response.isSuccessful) {
                        centers = response.body() ?: emptyList()
                        Log.d("CenterListScreen", "DEBUG - Loaded ${centers.size} centers")
                        centers.forEach { center ->
                            Log.d("CenterListScreen", "DEBUG - Center: id=${center.id}, name=${center.name}")
                        }
                        if (centers.isEmpty()) {
                            Log.w("CenterListScreen", "WARNING - No centers available from API")
                            Toast.makeText(context, "No data available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("CenterListScreen", "ERROR - Failed to load centers: ${response.code()}")
                        Toast.makeText(context, "Failed to load centers", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CenterResponse>>, t: Throwable) {
                    hasLoadedOnce = true
                    isLoading = false
                    Toast.makeText(context, "Error: ${t.message ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F9FF),
                        Color(0xFFE0F2FE),
                        Color(0xFFBAE6FD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // UPDATED UI LOADED - Visible text
            Text(
                text = "UPDATED UI LOADED",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Header with gradient background
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
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Queue Centers",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Find and join queues near you",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ✅ ADMIN BUTTON
            if (role == "ADMIN") {
                Button(
                    onClick = { onAddCenterClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Center",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Google Maps at top (40% height) with center markers and error handling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Approximately 40% of screen height
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                GoogleMapWithFallback(
                    centerCrowdLevels = centerCrowdLevels,
                    onCenterClick = onCenterClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Centers List
            if (isLoading && !hasLoadedOnce) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6366F1),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading centers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6366F1)
                        )
                    }
                }
                return
            }

            if (centers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = "No centers",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Centers Available",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6366F1)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check back later or contact admin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                return
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(
                    items = centers,
                    key = { it.id }
                ) { center ->
                    EnhancedCenterCard(
                        center = center,
                        onCenterClick = { onCenterClick(center.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedCenterCard(
    center: CenterResponse,
    onCenterClick: () -> Unit
) {
    Log.d("EnhancedCenterCard", "DEBUG - Rendering center: id=${center.id}, name=${center.name}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = center.name ?: "No Name",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = center.location ?: "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Type",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = center.type ?: "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            Color(0xFF10B981),
                            RoundedCornerShape(6.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    Log.d("EnhancedCenterCard", "DEBUG - Clicked center: id=${center.id}, name=${center.name}")
                    onCenterClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = "Queue",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Queue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// Mock crowd level prediction function
fun predictCrowdLevel(): String {
    val levels = listOf("LOW", "MEDIUM", "HIGH")
    return levels.random()
}

// Generate mock crowd levels for centers
fun generateMockCrowdLevelsForCenters(): List<CrowdLevel> {
    return listOf(
        CrowdLevel(
            id = "center_1",
            areaName = "Downtown",
            latitude = 28.6139,
            longitude = 77.2090,
            level = when (predictCrowdLevel()) {
                "LOW" -> CrowdDensity.LOW
                "MEDIUM" -> CrowdDensity.MEDIUM
                else -> CrowdDensity.HIGH
            }
        ),
        CrowdLevel(
            id = "center_2",
            areaName = "Airport",
            latitude = 28.5665,
            longitude = 77.1031,
            level = when (predictCrowdLevel()) {
                "LOW" -> CrowdDensity.LOW
                "MEDIUM" -> CrowdDensity.MEDIUM
                else -> CrowdDensity.HIGH
            }
        ),
        CrowdLevel(
            id = "center_3",
            areaName = "Railway Station",
            latitude = 28.6448,
            longitude = 77.2167,
            level = when (predictCrowdLevel()) {
                "LOW" -> CrowdDensity.LOW
                "MEDIUM" -> CrowdDensity.MEDIUM
                else -> CrowdDensity.HIGH
            }
        )
    )
}

@Composable
private fun GoogleMapWithFallback(
    centerCrowdLevels: List<Pair<CenterResponse, CrowdLevel>>,
    onCenterClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapLoadError by remember { mutableStateOf(false) }
    var isMapReady by remember { mutableStateOf(false) }
    
    Log.d("GoogleMapWithFallback", "DEBUG - Initializing Google Map with ${centerCrowdLevels.size} centers")
    
    // Add timeout mechanism - if map doesn't load within 10 seconds, show fallback
    LaunchedEffect(Unit) {
        delay(10000) // 10 second timeout
        if (!isMapReady && !mapLoadError) {
            Log.w("GoogleMapWithFallback", "WARNING - Map loading timeout, switching to fallback")
            mapLoadError = true
        }
    }
    
    Box(modifier = modifier) {
        if (mapLoadError) {
            // Fallback UI when map fails to load
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = "Map Error",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Google Maps unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "API key needs to be configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Use the center list below",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                // Show mini center list as fallback
                Spacer(modifier = Modifier.height(16.dp))
                if (centerCrowdLevels.isNotEmpty()) {
                    Text(
                        text = "Available Centers (${centerCrowdLevels.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        centerCrowdLevels.take(3).forEach { (center, crowdLevel) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            when (crowdLevel.level.value) {
                                                "low" -> Color(0xFF10B981)
                                                "medium" -> Color(0xFFF59E0B) 
                                                "high" -> Color(0xFFEF4444)
                                                else -> Color(0xFF6366F1)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${center.name ?: "Center"} (${crowdLevel.level.value.uppercase()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (centerCrowdLevels.size > 3) {
                            Text(
                                text = "... and ${centerCrowdLevels.size - 3} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        } else {
            // Try to load Google Maps
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(28.6139, 77.2090), // Delhi coordinates
                        12f
                    )
                },
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapLoaded = {
                    Log.d("GoogleMapWithFallback", "DEBUG - Google Map loaded successfully")
                    isMapReady = true
                }
            ) {
                // Add markers for each center with crowd level color coding
                centerCrowdLevels.forEach { (center, crowdLevel) ->
                    Log.d("GoogleMapWithFallback", "DEBUG - Adding marker for center: ${center.name}, id: ${center.id}")
                    
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                crowdLevel.latitude,
                                crowdLevel.longitude
                            )
                        ),
                        title = center.name ?: "Center",
                        snippet = "${crowdLevel.level.value.uppercase()} crowd level",
                        onClick = {
                            // Open existing Join Queue screen when marker is clicked
                            Log.d("GoogleMapWithFallback", "DEBUG - Map marker clicked: centerId=${center.id}, centerName=${center.name}")
                            onCenterClick(center.id)
                            true
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when (crowdLevel.level.value) {
                                "low" -> BitmapDescriptorFactory.HUE_GREEN
                                "medium" -> BitmapDescriptorFactory.HUE_ORANGE
                                "high" -> BitmapDescriptorFactory.HUE_RED
                                else -> BitmapDescriptorFactory.HUE_BLUE
                            }
                        )
                    )
                }
            }
            
            // Loading indicator while map is initializing
            if (!isMapReady) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6366F1),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading map...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
    
    // Handle map loading errors
    LaunchedEffect(Unit) {
        try {
            // Check if we can access Google Maps services
            Log.d("GoogleMapWithFallback", "DEBUG - Attempting to load Google Maps")
            
            // Quick check for common API key issues
            delay(2000) // Give it 2 seconds to start loading
            if (!isMapReady) {
                Log.w("GoogleMapWithFallback", "WARNING - Map taking longer than expected to load")
                // Don't set error immediately, let the timeout handle it
            }
        } catch (e: Exception) {
            Log.e("GoogleMapWithFallback", "ERROR - Failed to load Google Maps: ${e.message}", e)
            mapLoadError = true
        }
    }
}