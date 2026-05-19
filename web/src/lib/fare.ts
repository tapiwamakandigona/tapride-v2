/**
 * Fare calculation utilities for TapRide.
 * Currency: ZiG (Zimbabwean Gold)
 */

const BASE_FARE = 2.0; // ZiG
const PER_KM = 0.5; // ZiG per km
const MIN_FARE = 3.0; // ZiG minimum

/**
 * Calculate the ride fare based on distance.
 * @param distanceKm - Distance of the trip in kilometres
 * @returns Fare amount in ZiG
 */
export function calculateFare(distanceKm: number): number {
  const fare = BASE_FARE + PER_KM * distanceKm;
  return Math.max(fare, MIN_FARE);
}

/** Format a fare amount as a ZiG currency string */
export function formatFare(amount: number): string {
  return `ZiG ${amount.toFixed(2)}`;
}
