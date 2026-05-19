/**
 * Geographic utility functions: haversine distance, bounding box,
 * and geohash encode/decode helpers via ngeohash.
 */
import ngeohash from 'ngeohash';

const EARTH_RADIUS_KM = 6371;

/** Convert degrees to radians */
const toRad = (deg: number): number => (deg * Math.PI) / 180;

/**
 * Calculate the haversine distance in kilometres between two coordinates.
 */
export function haversineKm(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number,
): number {
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
}

/** Bounding box delta in degrees (~2 km) */
const BOX_DELTA = 0.02;

/**
 * Returns min/max lat/lng for a ~2 km bounding box around a point.
 * Used to query nearby drivers from Appwrite with simple range queries.
 */
export function boundingBox(lat: number, lng: number): {
  minLat: number;
  maxLat: number;
  minLng: number;
  maxLng: number;
} {
  return {
    minLat: lat - BOX_DELTA,
    maxLat: lat + BOX_DELTA,
    minLng: lng - BOX_DELTA,
    maxLng: lng + BOX_DELTA,
  };
}

/**
 * Encode a lat/lng pair to a geohash string at the given precision.
 */
export function encodeGeohash(lat: number, lng: number, precision = 7): string {
  return ngeohash.encode(lat, lng, precision);
}

/**
 * Decode a geohash string back to a { latitude, longitude } object.
 */
export function decodeGeohash(hash: string): { latitude: number; longitude: number } {
  return ngeohash.decode(hash);
}
