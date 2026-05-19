package io.tapiwa.tapride.util

import kotlin.math.*

/**
 * Geographic utility functions used throughout TapRide.
 */
object Geo {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Computes the great-circle distance between two GPS coordinates
     * using the Haversine formula.
     *
     * @param lat1 Latitude of the first point in decimal degrees.
     * @param lng1 Longitude of the first point in decimal degrees.
     * @param lat2 Latitude of the second point in decimal degrees.
     * @param lng2 Longitude of the second point in decimal degrees.
     * @return Distance in kilometres.
     */
    fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Computes a bounding box around a centre point for querying nearby drivers.
     *
     * @param lat       Centre latitude.
     * @param lng       Centre longitude.
     * @param radiusKm  Radius of the bounding box in kilometres.
     * @return [BoundingBox] with min/max lat/lng values.
     */
    fun boundingBox(lat: Double, lng: Double, radiusKm: Double): BoundingBox {
        val latDelta = radiusKm / EARTH_RADIUS_KM * (180 / Math.PI)
        val lngDelta = radiusKm / (EARTH_RADIUS_KM * cos(Math.toRadians(lat))) * (180 / Math.PI)
        return BoundingBox(
            minLat = lat - latDelta,
            maxLat = lat + latDelta,
            minLng = lng - lngDelta,
            maxLng = lng + lngDelta
        )
    }

    /**
     * Represents a rectangular geographic bounding box.
     *
     * @property minLat Minimum latitude.
     * @property maxLat Maximum latitude.
     * @property minLng Minimum longitude.
     * @property maxLng Maximum longitude.
     */
    data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double
    )
}
