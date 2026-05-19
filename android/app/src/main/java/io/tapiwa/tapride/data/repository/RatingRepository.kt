package io.tapiwa.tapride.data.repository

import io.appwrite.services.Databases
import io.appwrite.models.Document
import io.appwrite.ID
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Rating
import io.tapiwa.tapride.util.TapLogger

/**
 * Data access layer for post-ride [Rating] documents.
 *
 * @param databases Appwrite [Databases] service.
 */
class RatingRepository(private val databases: Databases) {

    /**
     * Persists a new rating after a completed ride.
     *
     * @param rating The [Rating] to store.
     * @return [Result] wrapping the stored [Rating] with its assigned ID.
     */
    suspend fun createRating(rating: Rating): Result<Rating> = try {
        val doc = databases.createDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_RATINGS,
            documentId = ID.unique(),
            data = rating.toMap()
        )
        Result.success(doc.toRating())
    } catch (e: Exception) {
        TapLogger.e("RatingRepository", "createRating failed", e)
        Result.failure(e)
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private fun Document<Map<String, Any>>.toRating() = Rating(
        id = id,
        rideId = data["rideId"] as? String ?: "",
        raterId = data["raterId"] as? String ?: "",
        rateeId = data["rateeId"] as? String ?: "",
        stars = (data["stars"] as? Number)?.toInt() ?: 5,
        comment = data["comment"] as? String ?: "",
        createdAt = data["createdAt"] as? String ?: ""
    )

    private fun Rating.toMap(): Map<String, Any> = mapOf(
        "rideId" to rideId,
        "raterId" to raterId,
        "rateeId" to rateeId,
        "stars" to stars,
        "comment" to comment,
        "createdAt" to createdAt
    )
}
