package com.masterapp.queueeaseapp.utils

object SessionManager {

    private var token: String? = null

    // ✅ SAVE TOKEN
    fun saveToken(t: String) {
        token = t
    }

    // ✅ GET TOKEN
    fun getToken(): String? {
        return token
    }
}