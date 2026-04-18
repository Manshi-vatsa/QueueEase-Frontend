package com.masterapp.queueeaseapp.model

data class CrowdLevel(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val level: CrowdDensity,
    val areaName: String
)

enum class CrowdDensity(val value: String, val color: String, val priority: Int) {
    LOW("low", "#4CAF50", 1),      // Green
    MEDIUM("medium", "#FF9800", 2), // Orange  
    HIGH("high", "#F44336", 3)     // Red
}

data class GridArea(
    val id: String,
    val bounds: LatLngBounds,
    val crowdDensity: CrowdDensity,
    val centerName: String?
)

data class LatLngBounds(
    val northeast: LatLng,
    val southwest: LatLng
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)
