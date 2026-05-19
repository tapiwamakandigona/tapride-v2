package io.tapiwa.tapride.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single ride request and its full lifecycle.
 *
 * Status transitions:
 * `requested` → `accepted` → `in_progress` → `completed`
 *                                           ↘ `cancelled`
 *
 * @property id             Appwrite document ID.
 * @property riderId        User ID of the rider who requested the ride.
 * @property driverId       User ID of the assigned driver, or null if not yet accepted.
 * @property status         Current lifecycle status string.
 * @property pickupLat      Latitude of the pickup point.
 * @property pickupLng      Longitude of the pickup point.
 * @property pickupAddress  Human-readable pickup address.
 * @property dropLat        Latitude of the drop-off point.
 * @property dropLng        Longitude of the drop-off point.
 * @property dropAddress    Human-readable drop-off address.
 * @property fareEstimate   Calculated fare in local currency.
 * @property distanceKm     Route distance in kilometres.
 * @property requestedAt    ISO-8601 timestamp when the ride was requested.
 * @property acceptedAt     ISO-8601 timestamp when a driver accepted, or null.
 * @property completedAt    ISO-8601 timestamp when the ride completed, or null.
 * @property cancelReason   Optional reason string when status is `cancelled`.
 */
@Serializable
data class Ride(
    val id: String,
    val riderId: String,
    val driverId: String? = null,
    val status: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropLat: Double,
    val dropLng: Double,
    val dropAddress: String,
    val fareEstimate: Double,
    val distanceKm: Double,
    val requestedAt: String,
    val acceptedAt: String? = null,
    val completedAt: String? = null,
    val cancelReason: String? = null
)
