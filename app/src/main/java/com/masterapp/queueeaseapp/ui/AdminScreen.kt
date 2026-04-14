package com.masterapp.queueeaseapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masterapp.queueeaseapp.api.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AdminScreen(centerId: Long) {

    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Button(onClick = {

            RetrofitClient.api.serveNext(centerId)
                .enqueue(object : Callback<Any> {

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {}

                    override fun onFailure(call: Call<Any>, t: Throwable) {}
                })

        }) {
            Text("Serve Next")
        }

        Text(message)
    }
}