package com.masterapp.queueeaseapp.api

import android.util.Log
import com.masterapp.queueeaseapp.model.BookingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun joinQueue(
    userId: Long,
    centerId: Long,
    onSuccess: () -> Unit = {}
) {
    RetrofitClient.api.joinQueue(userId, centerId)
        .enqueue(object : Callback<BookingResponse> {

            override fun onResponse(
                call: Call<BookingResponse>,
                response: Response<BookingResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("JOIN_SUCCESS", response.body().toString())
                    onSuccess()
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                Log.e("JOIN_FAIL", t.message.toString())
            }
        })
}