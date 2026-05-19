package io.tapiwa.tapride.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * General-purpose Kotlin extension functions used throughout TapRide.
 */

/**
 * Returns `true` if this [String] is neither null nor blank.
 * Convenience inverse of [isNullOrBlank].
 */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/**
 * Formats this [Double] as a currency string with 2 decimal places.
 * Example: `12.5.toCurrencyString()` → `"$12.50"`
 *
 * @param symbol Currency symbol prefix. Defaults to `"$"`.
 */
fun Double.toCurrencyString(symbol: String = "$"): String =
    "$symbol%.2f".format(this)

/**
 * Formats this [Double] as a distance string.
 * Example: `3.45678.toDistanceString()` → `"3.46 km"`
 */
fun Double.toDistanceString(): String = "%.2f km".format(this)

/**
 * Returns the current UTC time as an ISO-8601 string.
 * Used when creating Appwrite documents that need a timestamp.
 */
fun currentIso8601(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

/**
 * Parses an ISO-8601 string and returns a human-readable short date/time.
 * Example: `"2024-06-15T14:30:00.000Z".toReadableDateTime()` → `"Jun 15, 14:30"`
 */
fun String.toReadableDateTime(): String = try {
    val parse = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    parse.timeZone = TimeZone.getTimeZone("UTC")
    val date = parse.parse(this) ?: return this
    val display = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    display.format(date)
} catch (e: Exception) {
    this
}
