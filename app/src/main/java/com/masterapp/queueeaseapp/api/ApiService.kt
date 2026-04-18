package com.masterapp.queueeaseapp.api

import com.masterapp.queueeaseapp.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<AuthResponse>

    @POST("api/auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<Map<String, Any>>

    @GET("centers")
    fun getCenters(): Call<List<CenterResponse>>

    @POST("api/queue/joinQueue")
    fun joinQueue(
        @Query("userId") userId: Long,
        @Query("centerId") centerId: Long
    ): Call<BookingResponse>

    @GET("api/queue/status")
    fun getStatus(
        @Query("userId") userId: Long,
        @Query("centerId") centerId: Long
    ): Call<QueueStatusResponse>

    @GET("api/queue/list")
    fun getQueueList(
        @Query("centerId") centerId: Long
    ): Call<List<QueueUser>>

    @PUT("api/queue/serveNext/{centerId}")
    fun serveNext(
        @Path("centerId") centerId: Long
    ): Call<Any>

    @POST("centers")
    fun addCenter(
        @Body center: CenterResponse
    ): Call<CenterResponse>

    @GET("queue/users")
    fun getQueueUsers(
        @Query("centerId") centerId: Long
    ): Call<List<QueueUser>>

    @POST("queue/serveUser")
    fun serveUser(
        @Body request: ServeUserRequest
    ): Call<QueueStatusResponse>
}