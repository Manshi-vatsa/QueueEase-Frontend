package com.masterapp.queueeaseapp.api
import com.masterapp.queueeaseapp.App
import com.masterapp.queueeaseapp.api.ApiClient
import com.masterapp.queueeaseapp.utils.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://192.168.29.53:8080/"

    val apiService: ApiService by lazy {

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(App.context)) // ✅ ENABLE THIS
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}