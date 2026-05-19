package io.tapiwa.tapride.util

/**
 * Fare calculation utilities for TapRide.
 *
 * The formula mirrors the backend formula so the Android client can
 * display an accurate estimate before a ride is created in Appwrite.
 */
object Fare {

    /** Base charge applied to every ride, in local currency units. */
    private const val BASE_FARE = 2.50

    /** Per-kilometre rate. */
    private const val RATE_PER_KM = 0.85

    /** Minimum fare regardless of distance. */
    private const val MINIMUM_FARE = 3.00

    /**
     * Estimates the fare for a ride given its distance.
     *
     * Formula: `max(BASE_FARE + distanceKm * RATE_PER_KM, MINIMUM_FARE)`
     *
     * @param distanceKm The route distance in kilometres, computed via [Geo.haversine].
     * @return Estimated fare rounded to 2 decimal places.
     */
    fun estimate(distanceKm: Double): Double {
        val raw = BASE_FARE + distanceKm * RATE_PER_KM
        return maxOf(raw, MINIMUM_FARE).roundTo(2)
    }

    /** Rounds a [Double] to [decimals] decimal places. */
    private fun Double.roundTo(decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return Math.round(this * factor) / factor
    }
}
