package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
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
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.utils.userFacingNetworkMessage
import com.masterapp.queueeaseapp.model.CenterResponse
import com.masterapp.queueeaseapp.ui.CenterListScreen
import com.masterapp.queueeaseapp.ui.AdminScreen
import com.masterapp.queueeaseapp.ui.CreateCenterScreen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showCreateCenter by remember { mutableStateOf(false) }
            
            if (showCreateCenter) {
                CreateCenterScreen(
                    onCenterCreated = {
                        showCreateCenter = false
                    },
                    onBack = {
                        showCreateCenter = false
                    }
                )
            } else {
                AdminDashboardContent(
                    onCreateCenterClick = { showCreateCenter = true }
                )
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    onCreateCenterClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Simple gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF3730A3),
                        Color(0xFF6366F1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Functions Card
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
                        Icons.Default.Settings,
                        contentDescription = "Admin",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Admin Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // View All Centers Button
                    Button(
                        onClick = {
                            context.startActivity(Intent(context, UserDashboardActivity::class.java))
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
                        )
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "View Centers",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View All Centers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // View Queue Button
                    Button(
                        onClick = {
                            val intent = Intent(context, QueueListActivity::class.java)
                            intent.putExtra("centerId", 1)
                            intent.putExtra("role", "ADMIN")
                            intent.putExtra("centerName", "Center 1")
                            context.startActivity(intent)
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
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "View Queue",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manage Queue",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Add Center Button
                    Button(
                        onClick = onCreateCenterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4ADE7B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Center",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Center",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}