package com.masterapp.queueeaseapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.QueueUser
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CenterDetailScreen(
    userId: Long,
    centerId: Long,
    role: String,
    onJoinSuccess: () -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    var queueList by remember { mutableStateOf<List<QueueUser>>(emptyList()) }

    LaunchedEffect(centerId) {
        while (true) {

            Log.d("QUEUE_API", "Calling for centerId: $centerId")

            RetrofitClient.api.getQueueList(centerId)
                .enqueue(object : Callback<List<QueueUser>> {

                    override fun onResponse(
                        call: Call<List<QueueUser>>,
                        response: Response<List<QueueUser>>
                    ) {
                        Log.d("QUEUE_RESPONSE", "Code: ${response.code()}")

                        if (response.isSuccessful) {
                            queueList = response.body() ?: emptyList()
                        } else {
                            Log.e("QUEUE_ERROR", "Failed: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<List<QueueUser>>, t: Throwable) {
                        Log.e("QUEUE_FAIL", t.message.toString())
                    }
                })

            delay(5000)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("👥 Total Users: ${queueList.size}")

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(queueList) { user ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("👤 User ID: ${user.userId}")
                        Text("🎟 Token: ${user.queueNumber}")
                    }
                }
            }
        }

        // ✅ ONLY USER CAN JOIN
        if (role == "USER") {

            Button(
                onClick = {

                    RetrofitClient.api.joinQueue(userId, centerId)
                        .enqueue(object : Callback<BookingResponse> {

                            override fun onResponse(
                                call: Call<BookingResponse>,
                                response: Response<BookingResponse>
                            ) {

                                if (response.isSuccessful) {
                                    onJoinSuccess()
                                } else if (response.code() == 409) {
                                    Toast.makeText(context, "Already in queue", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Join Queue")
            }
        }

        TextButton(onClick = { onBack() }) {
            Text("⬅ Back")
        }
    }
}