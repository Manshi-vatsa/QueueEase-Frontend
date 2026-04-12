package com.masterapp.queueeaseapp.ui
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import com.masterapp.queueeaseapp.utils.SessionManager


@Composable
fun StatusScreen(userId: Long, centerId: Long) {

    var queueNumber by remember { mutableStateOf("Loading...") }
    var waitTimeText by remember { mutableStateOf("") }
    var waitTimeValue by remember { mutableStateOf(0) }
    var recommendation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {

        RetrofitClient.api.getStatus(SessionManager.token, userId, centerId)
            .enqueue(object : Callback<QueueStatusResponse> {

                override fun onResponse(
                    call: Call<QueueStatusResponse>,
                    response: Response<QueueStatusResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val data = response.body()!!

                        queueNumber = "Queue No: ${data.queueNumber}"
                        waitTimeText = "Wait Time: ${data.estimatedWaitTime} mins"
                        waitTimeValue = data.estimatedWaitTime
                        recommendation = data.recommendation

                    } else {
                        queueNumber = "Error fetching data"
                        waitTimeText = ""
                        waitTimeValue = 0
                        recommendation = ""
                    }
                }

                override fun onFailure(call: Call<QueueStatusResponse>, t: Throwable) {
                    queueNumber = "Network Error"
                }
            })
    }

    val color = when {
        waitTimeValue < 10 -> androidx.compose.ui.graphics.Color.Green
        waitTimeValue < 30 -> androidx.compose.ui.graphics.Color.Yellow
        else -> androidx.compose.ui.graphics.Color.Red
    }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            elevation = CardDefaults.cardElevation(6.dp)

    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(queueNumber, style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                waitTimeText,
                color = color,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(recommendation)
        }
    }
}
