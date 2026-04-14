package com.masterapp.queueeaseapp.queue

import com.masterapp.queueeaseapp.api.ApiClient
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import com.masterapp.queueeaseapp.model.QueueUser
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QueueRepository {

    suspend fun getQueueStatus(userId: Long, centerId: Long): QueueResult<QueueStatusResponse> {
        return try {
            val response = ApiClient.apiService.getStatus(userId, centerId).awaitResponse()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    QueueResult.Success(body)
                } else {
                    QueueResult.Error("Queue status is unavailable.")
                }
            } else {
                QueueResult.Error("Failed to load queue status (${response.code()}).")
            }
        } catch (e: Exception) {
            QueueResult.Error("Network error: ${e.message ?: "Please try again."}")
        }
    }

    suspend fun getQueueList(centerId: Long): QueueResult<List<QueueUser>> {
        return try {
            val response = ApiClient.apiService.getQueueList(centerId).awaitResponse()
            if (response.isSuccessful) {
                QueueResult.Success(response.body().orEmpty())
            } else {
                QueueResult.Error("Failed to load queue list (${response.code()}).")
            }
        } catch (e: Exception) {
            QueueResult.Error("Network error: ${e.message ?: "Please try again."}")
        }
    }

    suspend fun joinQueue(userId: Long, centerId: Long): QueueResult<BookingResponse> {
        return try {
            val response = ApiClient.apiService.joinQueue(userId, centerId).awaitResponse()
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        QueueResult.Success(body)
                    } else {
                        QueueResult.Error("Join queue response is empty.")
                    }
                }

                response.code() == 409 -> QueueResult.Error("Already in queue.")
                response.code() == 401 -> QueueResult.Error("Session expired. Please login again.")
                else -> QueueResult.Error("Failed to join queue (${response.code()}).")
            }
        } catch (e: Exception) {
            QueueResult.Error("Network error: ${e.message ?: "Please try again."}")
        }
    }
}

sealed class QueueResult<out T> {
    data class Success<out T>(val data: T) : QueueResult<T>()
    data class Error(val message: String) : QueueResult<Nothing>()
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
