package com.masterapp.queueeaseapp.api

object RetrofitClient {
    // Keep existing reference name while using the single ApiClient configuration.
    val api: ApiService
        get() = ApiClient.apiService
}