package com.masterapp.queueeaseapp.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient

import com.masterapp.queueeaseapp.model.CenterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CenterListScreen(
    userId: Long,
    role: String,
    onCenterClick: (Long) -> Unit,
    onAddCenterClick: () -> Unit   // ✅ THIS MUST EXIST
) {

    var centers by remember { mutableStateOf<List<CenterResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        RetrofitClient.api.getCenters()
            .enqueue(object : Callback<List<CenterResponse>> {

                override fun onResponse(
                    call: Call<List<CenterResponse>>,
                    response: Response<List<CenterResponse>>
                ) {
                    if (response.isSuccessful) {
                        centers = response.body() ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<List<CenterResponse>>, t: Throwable) {}
            })
    }

    Column {

        // ✅ ADMIN BUTTON
        if (role == "ADMIN") {
            Button(
                onClick = {
                    // TODO: open AddCenter screen
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➕ Add Center")
            }
        }

        LazyColumn {
            items(centers) { center ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {

                        Text("🏥 ${center.name ?: "No Name"}")

                        Button(
                            onClick = {
                                onCenterClick(center.id)   // ✅ ONLY NAVIGATION
                            }
                        ) {
                            Text("View Queue")
                        }
                    }
                }
            }
        }
    }
}