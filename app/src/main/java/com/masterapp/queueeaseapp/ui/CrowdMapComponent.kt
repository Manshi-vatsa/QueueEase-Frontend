package com.masterapp.queueeaseapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.masterapp.queueeaseapp.model.CrowdDensity
import com.masterapp.queueeaseapp.model.CrowdLevel
import com.masterapp.queueeaseapp.NotificationHelper
import kotlinx.coroutines.delay

@Composable
fun CrowdMapComponent(
    crowdLevels: List<CrowdLevel>,
    onCrowdedAreaClick: (CrowdLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            if (!granted) {
                Toast.makeText(context, "Location permission required for crowd monitoring", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (!hasLocationPermission) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3CD)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFF856404),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable location to view crowd levels on map",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF856404)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF856404)
                    )
                ) {
                    Text("Grant Permission")
                }
            }
        }
    } else {
        GoogleMapContent(
            crowdLevels = crowdLevels,
            onCrowdedAreaClick = onCrowdedAreaClick,
            modifier = modifier
        )
    }
}

@Composable
private fun GoogleMapContent(
    crowdLevels: List<CrowdLevel>,
    onCrowdedAreaClick: (CrowdLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(com.google.android.gms.maps.model.LatLng(28.6139, 77.2090), 12f) // Default to Delhi
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            ) {
                // Add crowd level overlays
                crowdLevels.forEach { crowdLevel ->
                    CrowdLevelOverlay(
                        crowdLevel = crowdLevel,
                        onClick = { onCrowdedAreaClick(crowdLevel) }
                    )
                }
            }

            // Legend
            CrowdLegend(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun CrowdLevelOverlay(
    crowdLevel: CrowdLevel,
    onClick: () -> Unit
) {
    val color = when (crowdLevel.level) {
        CrowdDensity.LOW -> Color(0x444CAF50)    // Green with transparency
        CrowdDensity.MEDIUM -> Color(0x44FF9800) // Orange with transparency
        CrowdDensity.HIGH -> Color(0x44F44336)  // Red with transparency
    }

    // Create a grid overlay for the area
    Polygon(
        points = listOf(
            com.google.android.gms.maps.model.LatLng(crowdLevel.latitude + 0.01, crowdLevel.longitude - 0.01),
            com.google.android.gms.maps.model.LatLng(crowdLevel.latitude + 0.01, crowdLevel.longitude + 0.01),
            com.google.android.gms.maps.model.LatLng(crowdLevel.latitude - 0.01, crowdLevel.longitude + 0.01),
            com.google.android.gms.maps.model.LatLng(crowdLevel.latitude - 0.01, crowdLevel.longitude - 0.01)
        ),
        fillColor = color,
        strokeColor = color.copy(alpha = 0.8f),
        strokeWidth = 2f,
        clickable = true,
        onClick = { polygon -> onClick() }
    )

    // Add marker for area name
    Marker(
        state = MarkerState(position = com.google.android.gms.maps.model.LatLng(crowdLevel.latitude, crowdLevel.longitude)),
        title = crowdLevel.areaName,
        snippet = "Crowd Level: ${crowdLevel.level.value.uppercase()}"
    )
}

@Composable
private fun CrowdLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Crowd Levels",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            CrowdLegendItem("Low", Color(0xFF4CAF50))
            CrowdLegendItem("Medium", Color(0xFFFF9800))
            CrowdLegendItem("High", Color(0xFFF44336))
        }
    }
}

@Composable
private fun CrowdLegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Mock data generator for demonstration
@Composable
fun generateMockCrowdLevels(): List<CrowdLevel> {
    return remember {
        listOf(
            CrowdLevel(
                id = "1",
                latitude = 28.6139,
                longitude = 77.2090,
                level = CrowdDensity.HIGH,
                areaName = "Connaught Place"
            ),
            CrowdLevel(
                id = "2", 
                latitude = 28.6350,
                longitude = 77.2250,
                level = CrowdDensity.MEDIUM,
                areaName = "Karol Bagh"
            ),
            CrowdLevel(
                id = "3",
                latitude = 28.5500,
                longitude = 77.2000,
                level = CrowdDensity.LOW,
                areaName = "Saket"
            ),
            CrowdLevel(
                id = "4",
                latitude = 28.5600,
                longitude = 77.2400,
                level = CrowdDensity.MEDIUM,
                areaName = "Greater Kailash"
            )
        )
    }
}
