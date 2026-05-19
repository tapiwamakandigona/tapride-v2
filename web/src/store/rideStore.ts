/**
 * Zustand store for active ride state.
 * Tracks the current ride and nearby drivers visible on the map.
 */
import { create } from 'zustand';
import type { Ride, DriverLocation } from '@/types';

interface RideState {
  activeRide: Ride | null;
  nearbyDrivers: DriverLocation[];
  setActiveRide: (ride: Ride | null) => void;
  setNearbyDrivers: (drivers: DriverLocation[]) => void;
  updateNearbyDriver: (driver: DriverLocation) => void;
  removeNearbyDriver: (driverId: string) => void;
  clear: () => void;
}

export const useRideStore = create<RideState>((set) => ({
  activeRide: null,
  nearbyDrivers: [],
  setActiveRide: (activeRide) => set({ activeRide }),
  setNearbyDrivers: (nearbyDrivers) => set({ nearbyDrivers }),
  updateNearbyDriver: (driver) =>
    set((state) => {
      const idx = state.nearbyDrivers.findIndex((d) => d.driverId === driver.driverId);
      if (idx === -1) {
        return { nearbyDrivers: [...state.nearbyDrivers, driver] };
      }
      const updated = [...state.nearbyDrivers];
      updated[idx] = driver;
      return { nearbyDrivers: updated };
    }),
  removeNearbyDriver: (driverId) =>
    set((state) => ({
      nearbyDrivers: state.nearbyDrivers.filter((d) => d.driverId !== driverId),
    })),
  clear: () => set({ activeRide: null, nearbyDrivers: [] }),
}));
