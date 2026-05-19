package io.tapiwa.tapride.data.model

import kotlinx.serialization.Serializable

/**
 * A single chat message exchanged between rider and driver during an active ride.
 *
 * @property id        Appwrite document ID.
 * @property rideId    The ride this message belongs to.
 * @property senderId  Appwrite user ID of the sender.
 * @property text      Message body text.
 * @property sentAt    ISO-8601 timestamp when the message was sent.
 */
@Serializable
data class Message(
    val id: String,
    val rideId: String,
    val senderId: String,
    val text: String,
    val sentAt: String
)
