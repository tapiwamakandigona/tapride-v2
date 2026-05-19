package io.tapiwa.tapride.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a TapRide user profile stored in Appwrite.
 *
 * @property userId   Appwrite user ID — matches [io.appwrite.models.User.id].
 * @property fullName Display name of the user.
 * @property phone    E.164 phone number.
 * @property role     Either `"rider"` or `"driver"`.
 * @property rating   Average star rating (1–5). Defaults to 5.0 for new users.
 * @property totalRides Cumulative completed ride count.
 * @property isOnline Whether the driver is currently accepting rides.
 * @property vehicleType Human-readable vehicle description (drivers only).
 * @property licensePlate Vehicle registration number (drivers only).
 */
@Serializable
data class Profile(
    val userId: String,
    val fullName: String,
    val phone: String,
    val role: String,
    val rating: Double = 5.0,
    val totalRides: Int = 0,
    val isOnline: Boolean = false,
    val vehicleType: String = "",
    val licensePlate: String = ""
)
