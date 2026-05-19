/**
 * Hook for driver location management.
 * - Publishes GPS position to `driver_locations` every 10 seconds when active.
 * - Queries nearby drivers from Appwrite for the rider's map view.
 */
import { useState, useEffect, useCallback, useRef } from 'react';
import { ID, Query, AppwriteException, Permission, Role } from 'appwrite';
import { databases, DATABASE_ID, COLLECTIONS } from '@/lib/appwrite';
import { useAuthStore } from '@/store/authStore';
import { useRideStore } from '@/store/rideStore';
import { boundingBox, haversineKm } from '@/lib/geo';
import type { DriverLocation } from '@/types';
import { logger } from '@/lib/logger';

const PUBLISH_INTERVAL_MS = 10_000;
const MAX_DRIVER_DISTANCE_KM = 5;

export function useDriverLocation() {
  const { user } = useAuthStore();
  const { setNearbyDrivers } = useRideStore();
  const [publishing, setPublishing] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const locationDocIdRef = useRef<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  /** Publish the driver's current GPS position to Appwrite */
  const publishLocation = useCallback(async (lat: number, lng: number, heading?: number) => {
    if (!user) return;
    try {
      const payload = {
        driverId: user.$id,
        lat,
        lng,
        isAvailable: true,
        heading: heading ?? 0,
        updatedAt: new Date().toISOString(),
      };

      if (locationDocIdRef.current) {
        await databases.updateDocument(
          DATABASE_ID,
          COLLECTIONS.DRIVER_LOCATIONS,
          locationDocIdRef.current,
          payload,
        );
      } else {
        // Check if document already exists for this driver
        const existing = await databases.listDocuments(
          DATABASE_ID,
          COLLECTIONS.DRIVER_LOCATIONS,
          [Query.equal('driverId', user.$id)],
        );
        if (existing.documents.length > 0) {
          locationDocIdRef.current = existing.documents[0].$id;
          await databases.updateDocument(
            DATABASE_ID,
            COLLECTIONS.DRIVER_LOCATIONS,
            locationDocIdRef.current,
            payload,
          );
        } else {
          const doc = await databases.createDocument(
            DATABASE_ID,
            COLLECTIONS.DRIVER_LOCATIONS,
            ID.unique(),
            payload,
            [
              Permission.read(Role.users()),
              Permission.update(Role.user(user.$id)),
              Permission.delete(Role.user(user.$id)),
            ],
          );
          locationDocIdRef.current = doc.$id;
        }
      }
    } catch (e) {
      logger.error('publishLocation', e);
    }
  }, [user]);

  /** Start publishing GPS every 10 seconds */
  const startPublishing = useCallback(() => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation not supported');
      return;
    }
    setPublishing(true);

    const publish = () => {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          publishLocation(pos.coords.latitude, pos.coords.longitude, pos.coords.heading ?? undefined);
        },
        (err) => {
          logger.warn('GPS error', err.message);
        },
        { enableHighAccuracy: true, timeout: 8000 },
      );
    };

    publish(); // Immediate first publish
    intervalRef.current = setInterval(publish, PUBLISH_INTERVAL_MS);
  }, [publishLocation]);

  /** Stop publishing and mark driver as unavailable */
  const stopPublishing = useCallback(async () => {
    setPublishing(false);
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (!user || !locationDocIdRef.current) return;
    try {
      await databases.updateDocument(
        DATABASE_ID,
        COLLECTIONS.DRIVER_LOCATIONS,
        locationDocIdRef.current,
        { isAvailable: false, updatedAt: new Date().toISOString() },
      );
    } catch (e) {
      const err = e instanceof AppwriteException ? e.message : String(e);
      logger.error('stopPublishing', err);
    }
  }, [user]);

  /** Fetch nearby available drivers around a given location */
  const fetchNearbyDrivers = useCallback(async (lat: number, lng: number) => {
    try {
      const { minLat, maxLat, minLng, maxLng } = boundingBox(lat, lng);
      const res = await databases.listDocuments(DATABASE_ID, COLLECTIONS.DRIVER_LOCATIONS, [
        Query.greaterThanEqual('lat', minLat),
        Query.lessThanEqual('lat', maxLat),
        Query.greaterThanEqual('lng', minLng),
        Query.lessThanEqual('lng', maxLng),
        Query.equal('isAvailable', true),
        Query.limit(20),
      ]);

      const drivers = (res.documents as unknown as DriverLocation[]).filter(
        (d) => haversineKm(lat, lng, d.lat, d.lng) <= MAX_DRIVER_DISTANCE_KM,
      );
      setNearbyDrivers(drivers);
    } catch (e) {
      logger.error('fetchNearbyDrivers', e);
    }
  }, [setNearbyDrivers]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, []);

  return {
    publishing,
    locationError,
    startPublishing,
    stopPublishing,
    fetchNearbyDrivers,
    publishLocation,
  };
}
