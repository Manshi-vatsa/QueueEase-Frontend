package com.masterapp.queueeaseapp.api

import android.content.Context
import com.masterapp.queueeaseapp.App
import com.masterapp.queueeaseapp.utils.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.29.53:8080/"

    private val client = okhttp3.OkHttpClient.Builder()
        .addInterceptor { chain ->

            val pref = App.context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            val token = pref.getString("TOKEN", null)

            val requestBuilder = chain.request().newBuilder()

            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    val api: ApiService = retrofit2.Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}