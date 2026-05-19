package io.tapiwa.tapride.data.repository

import io.appwrite.services.Databases
import io.appwrite.models.Document
import io.appwrite.ID
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.DriverLocation
import io.tapiwa.tapride.util.TapLogger

/**
 * Data access layer for [DriverLocation] documents.
 *
 * One document per driver is maintained; it is upserted on every location ping.
 *
 * @param databases Appwrite [Databases] service.
 */
class LocationRepository(private val databases: Databases) {

    /**
     * Upserts the driver's current location.
     * If no document exists for the driver, one is created;
     * otherwise the existing document is updated.
     *
     * @param location The [DriverLocation] snapshot to persist.
     * @return [Result] wrapping the stored [DriverLocation] on success.
     */
    suspend fun upsertLocation(location: DriverLocation): Result<DriverLocation> = try {
        // Use driverId as the document ID for easy upsert logic.
        val existing = try {
            databases.getDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COL_DRIVER_LOCATIONS,
                documentId = location.driverId
            )
            true
        } catch (e: Exception) {
            false
        }

        val doc = if (existing) {
            databases.updateDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COL_DRIVER_LOCATIONS,
                documentId = location.driverId,
                data = location.toMap()
            )
        } else {
            databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COL_DRIVER_LOCATIONS,
                documentId = location.driverId,
                data = location.toMap()
            )
        }
        Result.success(doc.toDriverLocation())
    } catch (e: Exception) {
        TapLogger.e("LocationRepository", "upsertLocation failed", e)
        Result.failure(e)
    }

    /**
     * Fetches a specific driver's last known location.
     *
     * @param driverId The driver's Appwrite user ID.
     * @return [Result] wrapping the [DriverLocation] on success.
     */
    suspend fun getLocation(driverId: String): Result<DriverLocation> = try {
        val doc = databases.getDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_DRIVER_LOCATIONS,
            documentId = driverId
        )
        Result.success(doc.toDriverLocation())
    } catch (e: Exception) {
        TapLogger.e("LocationRepository", "getLocation failed", e)
        Result.failure(e)
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private fun Document<Map<String, Any>>.toDriverLocation() = DriverLocation(
        driverId = data["driverId"] as? String ?: id,
        latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
        longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
        updatedAt = data["updatedAt"] as? String ?: "",
        isOnline = data["isOnline"] as? Boolean ?: true
    )

    private fun DriverLocation.toMap(): Map<String, Any> = mapOf(
        "driverId" to driverId,
        "latitude" to latitude,
        "longitude" to longitude,
        "updatedAt" to updatedAt,
        "isOnline" to isOnline
    )
}
