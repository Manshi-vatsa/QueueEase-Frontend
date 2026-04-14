package com.masterapp.queueeaseapp.home

import com.masterapp.queueeaseapp.api.ApiClient
import com.masterapp.queueeaseapp.model.CenterResponse
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRepository {

    suspend fun getCenters(): HomeResult<List<CenterResponse>> {
        return try {
            val response = ApiClient.apiService.getCenters().awaitResponse()
            if (response.isSuccessful) {
                HomeResult.Success(response.body().orEmpty())
            } else {
                HomeResult.Error("Failed to load centers (${response.code()}).")
            }
        } catch (e: Exception) {
            HomeResult.Error("Network error: ${e.message ?: "Please try again."}")
        }
    }
}

sealed class HomeResult<out T> {
    data class Success<out T>(val data: T) : HomeResult<T>()
    data class Error(val message: String) : HomeResult<Nothing>()
}

private suspend fun <T> Call<T>.awaitResponse(): Response<T> =
    suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (continuation.isActive) {
                    continuation.resume(response)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (continuation.isActive) {
                    continuation.resumeWith(Result.failure(t))
                }
            }
        })

        continuation.invokeOnCancellation {
            cancel()
        }
    }
