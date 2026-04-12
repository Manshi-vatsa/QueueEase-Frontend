package com.masterapp.queueeaseapp.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {

    private const val BASE_URL =  "http://192.168.29.53:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.29.53:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}