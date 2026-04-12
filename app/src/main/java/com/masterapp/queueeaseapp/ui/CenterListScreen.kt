package com.masterapp.queueeaseapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun CenterListScreen(
    userId: Long,
    onJoinSuccess: (Long) -> Unit   // ✅ return centerId
) {

    val centers = listOf(
        Pair(1L, "Hospital A"),
        Pair(2L, "Bank B"),
        Pair(3L, "Office C")
    )

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Select Center", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        centers.forEach { center ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(center.second)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {

                        RetrofitClient.api.joinQueue(
                            SessionManager.token,
                            userId,
                            center.first
                        )
                            .enqueue(object : Callback<BookingResponse> {

                                override fun onResponse(
                                    call: Call<BookingResponse>,
                                    response: Response<BookingResponse>
                                ) {
                                    onJoinSuccess(center.first)  // ✅ pass centerId
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