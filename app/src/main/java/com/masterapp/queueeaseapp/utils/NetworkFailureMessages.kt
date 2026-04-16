package com.masterapp.queueeaseapp.utils

/**
 * Maps low-level Retrofit/OkHttp errors (IPs, paths) to a short user-facing message.
 */
internal fun Throwable.isLikelyNetworkFailure(): Boolean {
    val m = message.orEmpty()
    val causeMsg = cause?.message.orEmpty()
    val combined = "$m $causeMsg"
    return combined.contains("Failed to connect", ignoreCase = true) ||
        combined.contains("Unable to resolve host", ignoreCase = true) ||
        combined.contains("timeout", ignoreCase = true) ||
        combined.contains("Connection refused", ignoreCase = true) ||
        combined.contains("ECONNREFUSED", ignoreCase = true) ||
        combined.contains("Network is unreachable", ignoreCase = true) ||
        combined.contains("No route to host", ignoreCase = true) ||
        combined.contains("Socket closed", ignoreCase = true) ||
        combined.contains("Software caused connection abort", ignoreCase = true) ||
        combined.contains("Cleartext HTTP traffic not permitted", ignoreCase = true)
}

fun Throwable.userFacingNetworkMessage(
    default: String = "Something went wrong. Please try again."
): String {
    return if (isLikelyNetworkFailure()) {
        "Cannot reach the server. Connect the phone to the same Wi‑Fi as your PC, then set backend.host in assets/queueease_api.properties to your PC IPv4 (ipconfig → IPv4 Address)."
    } else {
        default
    }
}
