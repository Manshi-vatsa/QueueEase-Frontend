package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class RoleSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Simple gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2),
                                Color(0xFFF093FB)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // UI UPDATED - Temporary visible change
                    Text(
                        text = " UI UPDATED ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Main card
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
                                Icons.Default.AccountCircle,
                                contentDescription = "Welcome",
                                tint = Color(0xFF667EEA),
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Welcome to QueueEase",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color(0xFF1F2937),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Choose your role to continue",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF6B7280)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // User button
                            Button(
                                onClick = {
                                    openRegister("USER")
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
                                    containerColor = Color(0xFF667EEA),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue as User",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Admin button
                            Button(
                                onClick = {
                                    openRegister("ADMIN")
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
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF667EEA)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Admin",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue as Admin",
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
    }

    private fun openRegister(role: String) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }
}