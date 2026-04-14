package com.masterapp.queueeaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(centerId, userId) {
        while (true) {
            Log.d("QUEUE_API", "SCREEN OPENED")
            Log.d("QUEUE_API", "Calling API with centerId = $centerId")
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

                        if (response.isSuccessful) {
                            data = response.body()   // 🔥 FIXED (THIS WAS MISSING)
                        }
                    }

                    override fun onFailure(call: Call<QueueStatusResponse>, t: Throwable) {
                        Log.d("QUEUE_API", "FAILED: ${t.message}")
                    }
                })

            delay(5000) // refresh every 5 sec
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("🎟 Token: ${data?.queueNumber}")
        Text("👨‍⚕️ Serving: ${data?.currentServing}")
        Text("⏳ People Ahead: ${data?.peopleAhead}")
        Text("⌛ Wait: ${data?.estimatedWaitTime} mins")
        Text("💡 ${data?.recommendation}")
    }
}