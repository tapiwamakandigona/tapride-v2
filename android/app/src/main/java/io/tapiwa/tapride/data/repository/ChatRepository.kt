package io.tapiwa.tapride.data.repository

import io.appwrite.services.Databases
import io.appwrite.models.Document
import io.appwrite.Query
import io.appwrite.ID
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Message
import io.tapiwa.tapride.util.TapLogger

/**
 * Data access layer for in-ride [Message] documents.
 *
 * @param databases Appwrite [Databases] service.
 */
class ChatRepository(private val databases: Databases) {

    /**
     * Sends a new chat message for a ride.
     *
     * @param message The [Message] to persist.
     * @return [Result] wrapping the stored [Message] with its assigned ID.
     */
    suspend fun sendMessage(message: Message): Result<Message> = try {
        val doc = databases.createDocument(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_MESSAGES,
            documentId = ID.unique(),
            data = message.toMap()
        )
        Result.success(doc.toMessage())
    } catch (e: Exception) {
        TapLogger.e("ChatRepository", "sendMessage failed", e)
        Result.failure(e)
    }

    /**
     * Loads all messages for a given ride, ordered chronologically.
     *
     * @param rideId The ride's Appwrite document ID.
     * @return [Result] wrapping a list of [Message] objects.
     */
    suspend fun getMessages(rideId: String): Result<List<Message>> = try {
        val result = databases.listDocuments(
            databaseId = AppwriteConfig.DATABASE_ID,
            collectionId = AppwriteConfig.COL_MESSAGES,
            queries = listOf(
                Query.equal("rideId", rideId),
                Query.orderAsc("\$createdAt")
            )
        )
        Result.success(result.documents.map { it.toMessage() })
    } catch (e: Exception) {
        TapLogger.e("ChatRepository", "getMessages failed", e)
        Result.failure(e)
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private fun Document<Map<String, Any>>.toMessage() = Message(
        id = id,
        rideId = data["rideId"] as? String ?: "",
        senderId = data["senderId"] as? String ?: "",
        text = data["text"] as? String ?: "",
        sentAt = data["sentAt"] as? String ?: ""
    )

    private fun Message.toMap(): Map<String, Any> = mapOf(
        "rideId" to rideId,
        "senderId" to senderId,
        "text" to text,
        "sentAt" to sentAt
    )
}
