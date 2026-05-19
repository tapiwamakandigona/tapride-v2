package io.tapiwa.tapride.data.model

import kotlinx.serialization.Serializable

/**
 * Post-ride rating submitted by a rider for a completed trip.
 *
 * @property id          Appwrite document ID.
 * @property rideId      The ride being rated.
 * @property raterId     Appwrite user ID of the person leaving the rating.
 * @property rateeId     Appwrite user ID of the person being rated.
 * @property stars       Numeric rating from 1 to 5.
 * @property comment     Optional free-text review.
 * @property createdAt   ISO-8601 timestamp of submission.
 */
@Serializable
data class Rating(
    val id: String,
    val rideId: String,
    val raterId: String,
    val rateeId: String,
    val stars: Int,
    val comment: String = "",
    val createdAt: String
)
