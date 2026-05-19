package io.tapiwa.tapride.data.repository

import io.appwrite.services.Databases
import io.appwrite.models.Document
import io.appwrite.Query
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Profile
import io.tapiwa.tapride.util.TapLogger

/**
 * Data access layer for user [Profile] documents.
 *
 * @param databases Appwrite [Databases] service.
 */
class ProfileRepository(private val databases: Databases) {

    /**
     * Fetches the profile for the given [userId].
     *
     * @param userId Appwrite user ID.
     * @return [Result] wrapping the [Profile] on success.
     */
    suspend fun getProfile(userId: String): Result<Profile> = try {
        val doc = databases.getDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_PROFILES,
            documentId = userId
        )
        Result.success(doc.toProfile())
    } catch (e: Exception) {
        TapLogger.e("ProfileRepository", "getProfile failed", e)
        Result.failure(e)
    }

    /**
     * Creates a new profile document in Appwrite.
     * The document ID is set to the user's Appwrite user ID.
     *
     * @param profile The [Profile] to persist.
     * @return [Result] wrapping the stored [Profile] on success.
     */
    suspend fun createProfile(profile: Profile): Result<Profile> = try {
        val doc = databases.createDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_PROFILES,
            documentId = profile.userId,
            data = profile.toMap()
        )
        Result.success(doc.toProfile())
    } catch (e: Exception) {
        TapLogger.e("ProfileRepository", "createProfile failed", e)
        Result.failure(e)
    }

    /**
     * Updates an existing profile document.
     *
     * @param profile The updated [Profile].
     * @return [Result] wrapping the updated [Profile] on success.
     */
    suspend fun updateProfile(profile: Profile): Result<Profile> = try {
        val doc = databases.updateDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_PROFILES,
            documentId = profile.userId,
            data = profile.toMap()
        )
        Result.success(doc.toProfile())
    } catch (e: Exception) {
        TapLogger.e("ProfileRepository", "updateProfile failed", e)
        Result.failure(e)
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private fun Document<Map<String, Any>>.toProfile() = Profile(
        userId = data["userId"] as? String ?: id,
        fullName = data["fullName"] as? String ?: "",
        phone = data["phone"] as? String ?: "",
        role = data["role"] as? String ?: "rider",
        rating = (data["rating"] as? Number)?.toDouble() ?: 5.0,
        totalRides = (data["totalRides"] as? Number)?.toInt() ?: 0,
        isOnline = data["isOnline"] as? Boolean ?: false,
        vehicleType = data["vehicleType"] as? String ?: "",
        licensePlate = data["licensePlate"] as? String ?: ""
    )

    private fun Profile.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "fullName" to fullName,
        "phone" to phone,
        "role" to role,
        "rating" to rating,
        "totalRides" to totalRides,
        "isOnline" to isOnline,
        "vehicleType" to vehicleType,
        "licensePlate" to licensePlate
    )
}
