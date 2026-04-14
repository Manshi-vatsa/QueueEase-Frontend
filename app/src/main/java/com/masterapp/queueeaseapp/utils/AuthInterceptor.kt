package com.masterapp.queueeaseapp.utils



import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        // 🔐 Get token from SharedPreferences
        val token = context
            .getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString("TOKEN", null)

        // 🚀 ADD HEADER HERE 👇
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        println("TOKEN: $token")
        println("TOKEN SENT: $token")
        return chain.proceed(request)
    }
}