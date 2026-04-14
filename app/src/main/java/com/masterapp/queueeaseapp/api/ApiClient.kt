package com.masterapp.queueeaseapp.api

import android.os.Build
import com.masterapp.queueeaseapp.App
import com.masterapp.queueeaseapp.BuildConfig
import com.masterapp.queueeaseapp.utils.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val baseUrl: String by lazy { resolveBaseUrl() }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(App.context))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Compatibility alias for old callers.
    val api: ApiService by lazy {
        apiService
    }

    private fun resolveBaseUrl(): String {
        val configuredBaseUrl = BuildConfig.BACKEND_BASE_URL.trim()
        if (configuredBaseUrl.isNotBlank()) {
            return ensureTrailingSlash(configuredBaseUrl)
        }

        val host = if (isEmulator()) BuildConfig.BACKEND_EMULATOR_HOST else BuildConfig.BACKEND_DEVICE_HOST
        val port = BuildConfig.BACKEND_PORT.trim()
        return "http://$host:$port/"
    }

    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.lowercase().contains("emulator") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            "google_sdk" == Build.PRODUCT
    }
}