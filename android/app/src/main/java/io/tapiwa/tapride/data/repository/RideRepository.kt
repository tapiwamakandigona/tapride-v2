package io.tapiwa.tapride.data.repository

import io.appwrite.services.Databases
import io.appwrite.models.Document
import io.appwrite.Query
import io.appwrite.ID
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Ride
import io.tapiwa.tapride.util.TapLogger

/**
 * Data access layer for [Ride] documents.
 *
 * @param databases Appwrite [Databases] service.
 */
class RideRepository(private val databases: Databases) {

    /**
     * Creates a new ride request.
     *
     * @param ride The [Ride] to persist. The [Ride.id] field is ignored;
     *             Appwrite assigns a unique ID automatically.
     * @return [Result] wrapping the persisted [Ride] with its assigned ID.
     */
    suspend fun createRide(ride: Ride): Result<Ride> = try {
        val doc = databases.createDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            documentId = ID.unique(),
            data = ride.toMap()
        )
        Result.success(doc.toRide())
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "createRide failed", e)
        Result.failure(e)
    }

    /**
     * Fetches a single ride by document ID.
     *
     * @param rideId Appwrite document ID of the ride.
     * @return [Result] wrapping the [Ride] on success.
     */
    suspend fun getRide(rideId: String): Result<Ride> = try {
        val doc = databases.getDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            documentId = rideId
        )
        Result.success(doc.toRide())
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "getRide failed", e)
        Result.failure(e)
    }

    /**
     * Updates a ride document — typically used to change status, assign driver, etc.
     *
     * @param ride The [Ride] with updated fields.
     * @return [Result] wrapping the updated [Ride].
     */
    suspend fun updateRide(ride: Ride): Result<Ride> = try {
        val doc = databases.updateDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            documentId = ride.id,
            data = ride.toMap()
        )
        Result.success(doc.toRide())
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "updateRide failed", e)
        Result.failure(e)
    }

    /**
     * Lists all rides for a specific rider, ordered by most recent first.
     *
     * @param riderId The rider's Appwrite user ID.
     * @return [Result] wrapping a list of [Ride] objects.
     */
    suspend fun getRidesForRider(riderId: String): Result<List<Ride>> = try {
        val result = databases.listDocuments(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            queries = listOf(
                Query.equal("riderId", riderId),
                Query.orderDesc("\$createdAt")
            )
        )
        Result.success(result.documents.map { it.toRide() })
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "getRidesForRider failed", e)
        Result.failure(e)
    }

    /**
     * Lists all open ride requests (status = "requested"), available for drivers to accept.
     *
     * @return [Result] wrapping a list of open [Ride] objects.
     */
    suspend fun getOpenRides(): Result<List<Ride>> = try {
        val result = databases.listDocuments(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            queries = listOf(
                Query.equal("status", "requested"),
                Query.orderDesc("\$createdAt")
            )
        )
        Result.success(result.documents.map { it.toRide() })
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "getOpenRides failed", e)
        Result.failure(e)
    }

    /**
     * Fetches all rides assigned to a specific driver.
     *
     * @param driverId The driver's Appwrite user ID.
     * @return [Result] wrapping a list of [Ride] objects.
     */
    suspend fun getRidesForDriver(driverId: String): Result<List<Ride>> = try {
        val result = databases.listDocuments(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RIDES,
            queries = listOf(
                Query.equal("driverId", driverId),
                Query.orderDesc("\$createdAt")
            )
        )
        Result.success(result.documents.map { it.toRide() })
    } catch (e: Exception) {
        TapLogger.e("RideRepository", "getRidesForDriver failed", e)
        Result.failure(e)
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private fun Document<Map<String, Any>>.toRide() = Ride(
        id = id,
        riderId = data["riderId"] as? String ?: "",
        driverId = data["driverId"] as? String,
        status = data["status"] as? String ?: "requested",
        pickupLat = (data["pickupLat"] as? Number)?.toDouble() ?: 0.0,
        pickupLng = (data["pickupLng"] as? Number)?.toDouble() ?: 0.0,
        pickupAddress = data["pickupAddress"] as? String ?: "",
        dropLat = (data["dropLat"] as? Number)?.toDouble() ?: 0.0,
        dropLng = (data["dropLng"] as? Number)?.toDouble() ?: 0.0,
        dropAddress = data["dropAddress"] as? String ?: "",
        fareEstimate = (data["fareEstimate"] as? Number)?.toDouble() ?: 0.0,
        distanceKm = (data["distanceKm"] as? Number)?.toDouble() ?: 0.0,
        requestedAt = data["requestedAt"] as? String ?: "",
        acceptedAt = data["acceptedAt"] as? String,
        completedAt = data["completedAt"] as? String,
        cancelReason = data["cancelReason"] as? String
    )

    private fun Ride.toMap(): Map<String, Any?> = mapOf(
        "riderId" to riderId,
        "driverId" to driverId,
        "status" to status,
        "pickupLat" to pickupLat,
        "pickupLng" to pickupLng,
        "pickupAddress" to pickupAddress,
        "dropLat" to dropLat,
        "dropLng" to dropLng,
        "dropAddress" to dropAddress,
        "fareEstimate" to fareEstimate,
        "distanceKm" to distanceKm,
        "requestedAt" to requestedAt,
        "acceptedAt" to acceptedAt,
        "completedAt" to completedAt,
        "cancelReason" to cancelReason
    )
}
