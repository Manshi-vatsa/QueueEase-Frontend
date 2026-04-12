package com.masterapp.queueeaseapp.api
import com.masterapp.queueeaseapp.model.LoginRequest
import com.masterapp.queueeaseapp.model.LoginResponse
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.QueueStatusResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @POST("api/queue/joinQueue")
    fun joinQueue(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long,
        @Query("centerId") centerId: Long
    ): Call<BookingResponse>


    @GET("api/queue/status")
    fun getStatus(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long,
        @Query("centerId") centerId: Long
    ): Call<QueueStatusResponse>
}