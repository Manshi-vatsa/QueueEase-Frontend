package com.masterapp.queueeaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn   // ✅ ADD
import androidx.compose.foundation.lazy.items       // ✅ ADD
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.CenterResponse
import com.masterapp.queueeaseapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CenterListScreen(
    userId: Long,
    onJoinSuccess: (Long) -> Unit
) {

    var centers by remember { mutableStateOf<List<CenterResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        RetrofitClient.api.getCenters(SessionManager.token)
            .enqueue(object : Callback<List<CenterResponse>> {

                override fun onResponse(
                    call: Call<List<CenterResponse>>,
                    response: Response<List<CenterResponse>>
                ) {
                    if (response.isSuccessful) {
                        centers = response.body() ?: emptyList()
                    }
                    isLoading = false
                }

                override fun onFailure(
                    call: Call<List<CenterResponse>>,
                    t: Throwable
                ) {
                    Log.e("API", "Error: ${t.message}")
                    isLoading = false
                }
            })
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Select Center", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {

            // ✅ REPLACED forEach WITH LazyColumn
            LazyColumn {

                items(centers) { center ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)   // ❌ removed verticalScroll from here
                        ) {

                            Text(center.name ?: "Unknown Center")
                            Text(center.location ?: "No Location")
                            Text(center.type ?: "No Type")

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {

                                RetrofitClient.api.joinQueue(
                                    SessionManager.token,
                                    userId,
                                    center.id
                                )
                                    .enqueue(object : Callback<BookingResponse> {

                                        override fun onResponse(
                                            call: Call<BookingResponse>,
                                            response: Response<BookingResponse>
                                        ) {
                                            onJoinSuccess(center.id)
                                        }

                                        override fun onFailure(
                                            call: Call<BookingResponse>,
                                            t: Throwable
                                        ) {
                                        }
                                    })

                            }) {
                                Text("Join Queue")
                            }
                        }
                    }
                }
            }
        }
    }
}