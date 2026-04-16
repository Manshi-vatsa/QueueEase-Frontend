package com.masterapp.queueeaseapp.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Build
import android.util.Log
import com.masterapp.queueeaseapp.BuildConfig
import java.io.IOException
import java.util.Properties

/**
 * Single source of truth for the Retrofit base URL.
 */
object ApiConfig {

    private const val TAG = "ApiConfig"
    private const val ASSET_PROPERTIES = "queueease_api.properties"

    private const val KEY_BASE_URL = "base_url"
    private const val KEY_SCHEME = "scheme"
    private const val KEY_PORT = "backend.port"
    private const val KEY_HOST = "backend.host"
    private const val KEY_DNS = "backend.dns"
    private const val KEY_EMULATOR_HOST = "emulator.host"
    private const val AUTO_GATEWAY_KEY = "auto_gateway"

    private const val EMULATOR_HOST_DEFAULT = "10.0.2.2"

    private val lock = Any()
    private var cachedProps: Properties? = null

    private fun properties(context: Context): Properties {
        synchronized(lock) {
            if (cachedProps == null) {
                val p = Properties()
                try {
                    context.assets.open(ASSET_PROPERTIES).use { p.load(it) }
                } catch (_: IOException) {
                    Log.w(TAG, "Missing assets/$ASSET_PROPERTIES; using Gradle defaults only")
                }
                cachedProps = p
            }
            return cachedProps!!
        }
    }

    fun resolveBaseUrl(context: Context): String {
        val gradleUrl = BuildConfig.BACKEND_BASE_URL.trim()
        if (gradleUrl.isNotBlank()) {
            return ensureTrailingSlash(gradleUrl)
        }

        val props = properties(context)
        val fromFile = props.getProperty(KEY_BASE_URL, "").trim()
        if (fromFile.isNotBlank()) {
            return ensureTrailingSlash(fromFile)
        }

        val port = port(props)
        val scheme = props.getProperty(KEY_SCHEME, "http").trim().ifBlank { "http" }

        if (isEmulator()) {
            val emuHost = props.getProperty(KEY_EMULATOR_HOST, EMULATOR_HOST_DEFAULT).trim()
                .ifBlank { EMULATOR_HOST_DEFAULT }
            val url = "$scheme://$emuHost:$port/"
            Log.d(TAG, "Using emulator base URL: $url")
            return url
        }

        val host = props.getProperty(KEY_HOST, "").trim()
        if (host.isNotBlank()) {
            return ensureTrailingSlash("$scheme://$host:$port")
        }

        val dns = props.getProperty(KEY_DNS, "").trim()
        if (dns.isNotBlank()) {
            return ensureTrailingSlash("$scheme://$dns:$port")
        }

        val autoGateway = props.getProperty(AUTO_GATEWAY_KEY, "false").trim().equals("true", ignoreCase = true)
        if (autoGateway) {
            val gw = defaultGatewayIpv4(context)
            if (gw != null) {
                val url = "$scheme://$gw:$port/"
                Log.w(TAG, "Using auto_gateway base URL: $url")
                return url
            }
        }

        error(
            "QueueEase API host is not configured. Set backend.host or base_url in assets/$ASSET_PROPERTIES, " +
                "or set BACKEND_BASE_URL in gradle.properties."
        )
    }

    private fun port(props: Properties): String {
        val fromFile = props.getProperty(KEY_PORT, "").trim()
        if (fromFile.isNotBlank()) return fromFile
        val fromGradle = BuildConfig.BACKEND_PORT.trim()
        return fromGradle.ifBlank { "8080" }
    }

    private fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trimEnd()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
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

    private fun defaultGatewayIpv4(context: Context): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val network = cm.activeNetwork ?: return null
        val lp: LinkProperties = cm.getLinkProperties(network) ?: return null
        for (route in lp.routes) {
            if (route.isDefaultRoute && route.gateway != null) {
                val gw = route.gateway?.hostAddress ?: continue
                if (gw != "0.0.0.0" && !gw.contains(':')) return gw
            }
        }
        return null
    }
}
