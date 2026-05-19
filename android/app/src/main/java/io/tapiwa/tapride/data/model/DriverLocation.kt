package io.tapiwa.tapride.data.model

import kotlinx.serialization.Serializable

/**
 * Snapshot of a driver's current GPS position.
 *
 * One document per driver is maintained in the [io.tapiwa.tapride.constants.AppwriteConfig.COL_DRIVER_LOCATIONS]
 * collection. It is upserted every 10 seconds while the driver is online.
 *
 * @property driverId  Appwrite user ID of the driver.
 * @property latitude  Current latitude.
 * @property longitude Current longitude.
 * @property updatedAt ISO-8601 timestamp of the last update.
 * @property isOnline  Whether the driver is actively accepting rides.
 */
@Serializable
data class DriverLocation(
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: String,
    val isOnline: Boolean = true
)
