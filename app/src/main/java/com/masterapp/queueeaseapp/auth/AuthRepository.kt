package com.masterapp.queueeaseapp.auth

import com.masterapp.queueeaseapp.api.ApiClient
import com.masterapp.queueeaseapp.model.AuthResponse
import com.masterapp.queueeaseapp.model.LoginRequest
import com.masterapp.queueeaseapp.model.RegisterRequest
import com.masterapp.queueeaseapp.utils.SessionManager
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): AuthResult<LoginSuccessData> {
        val response = safeAuthCall {
            ApiClient.apiService.login(LoginRequest(email = email, password = password))
        }

        return when (response) {
            is ApiCallResult.Success -> {
                val body = response.body
                val token = body.token
                if (token.isNullOrBlank()) {
                    AuthResult.Error("Login failed: missing token.")
                } else {
                    SessionManager.saveSession(
                        token = token,
                        userId = body.userId,
                        role = body.role
                    )
                    AuthResult.Success(
                        LoginSuccessData(
                            role = body.role,
                            userId = body.userId
                        )
                    )
                }
            }

            is ApiCallResult.HttpError -> {
                AuthResult.Error(messageForHttpCode(response.code))
            }

            is ApiCallResult.NetworkError -> AuthResult.Error("Network error. Please check your connection.")
            is ApiCallResult.UnknownError -> AuthResult.Error("Something went wrong. Please try again.")
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult<Unit> {
        val response = safeRegisterCall {
            ApiClient.apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    role = DEFAULT_REGISTER_ROLE
                )
            )
        }

        return when (response) {
            is ApiCallResult.Success -> AuthResult.Success(Unit)
            is ApiCallResult.HttpError -> AuthResult.Error(messageForHttpCode(response.code))
            is ApiCallResult.NetworkError -> AuthResult.Error("Network error. Please check your connection.")
            is ApiCallResult.UnknownError -> AuthResult.Error("Something went wrong. Please try again.")
        }
    }

    private fun messageForHttpCode(code: Int): String {
        return when (code) {
            401 -> "Unauthorized. Invalid credentials."
            else -> "Request failed with code $code."
        }
    }

    private suspend fun safeAuthCall(
        request: () -> Call<AuthResponse>
    ): ApiCallResult<AuthResponse> {
        return try {
            val response = request().awaitResponse()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiCallResult.Success(body)
                } else {
                    ApiCallResult.UnknownError
                }
            } else {
                ApiCallResult.HttpError(response.code())
            }
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("Failed to connect", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true)
            ) {
                ApiCallResult.NetworkError
            } else {
                ApiCallResult.UnknownError
            }
        }
    }

    private suspend fun safeRegisterCall(
        request: () -> Call<Map<String, Any>>
    ): ApiCallResult<Map<String, Any>> {
        return try {
            val response = request().awaitResponse()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiCallResult.Success(body)
                } else {
                    ApiCallResult.UnknownError
                }
            } else {
                ApiCallResult.HttpError(response.code())
            }
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("Failed to connect", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true)
            ) {
                ApiCallResult.NetworkError
            } else {
                ApiCallResult.UnknownError
            }
        }
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

    private companion object {
        const val DEFAULT_REGISTER_ROLE = "USER"
    }
}

data class LoginSuccessData(
    val role: String,
    val userId: Long
)

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

private sealed class ApiCallResult<out T> {
    data class Success<out T>(val body: T) : ApiCallResult<T>()
    data class HttpError(val code: Int) : ApiCallResult<Nothing>()
    data object NetworkError : ApiCallResult<Nothing>()
    data object UnknownError : ApiCallResult<Nothing>()
}
