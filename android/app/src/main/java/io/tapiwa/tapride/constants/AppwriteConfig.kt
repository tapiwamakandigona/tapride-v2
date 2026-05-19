package io.tapiwa.tapride.constants

/**
 * Central configuration object for Appwrite backend.
 * All endpoint, project, database, and collection identifiers live here —
 * never hardcode these values elsewhere.
 */
object AppwriteConfig {
    /** Base URL of the Appwrite instance. */
    const val ENDPOINT = "https://fra.cloud.appwrite.io/v1"

    /** Appwrite project ID for TapRide. */
    const val PROJECT_ID = "69e62515000e9e781653"

    /** Database ID used for all TapRide collections. */
    const val DATABASE_ID = "tapride_db"

    // ── Collection IDs ──────────────────────────────────────────────────────

    /** Stores user profile documents (role, vehicle info, rating, etc.). */
    const val COL_PROFILES = "profiles"

    /** Stores ride request/lifecycle documents. */
    const val COL_RIDES = "rides"

    /** Stores live driver GPS coordinates. */
    const val COL_DRIVER_LOCATIONS = "driver_locations"

    /** Stores in-ride chat messages. */
    const val COL_MESSAGES = "messages"

    /** Stores post-ride ratings submitted by riders/drivers. */
    const val COL_RATINGS = "ratings"
}
