package com.masterapp.queueeaseapp.utils

import android.content.Context
import androidx.core.content.edit
import com.masterapp.queueeaseapp.App

object SessionManager {

    private const val PREF_NAME = "APP_PREF"
    private const val KEY_TOKEN = "TOKEN"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_ROLE = "ROLE"
    private const val NO_USER_ID = Long.MIN_VALUE

    private val lock = Any()

    private val preferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        App.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getToken(): String? = synchronized(lock) {
        preferences.getString(KEY_TOKEN, null)
    }

    fun getUserId(): Long? = synchronized(lock) {
        val stored = preferences.getLong(KEY_USER_ID, NO_USER_ID)
        if (stored == NO_USER_ID) null else stored
    }

    fun getRole(): String? = synchronized(lock) {
        preferences.getString(KEY_ROLE, null)
    }

    fun saveSession(token: String, userId: Long, role: String) = synchronized(lock) {
        preferences.edit {
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_ROLE, role)
        }
    }

    fun clearSession() = synchronized(lock) {
        preferences.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_ROLE)
        }
    }

    fun isLoggedIn(): Boolean = synchronized(lock) {
        !getToken().isNullOrBlank() && getUserId() != null
    }

    // Compatibility helper for existing callers; use saveSession() for full session writes.
    fun saveToken(token: String) = synchronized(lock) {
        preferences.edit {
            putString(KEY_TOKEN, token)
        }
    }
}