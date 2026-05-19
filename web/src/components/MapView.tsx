/**
 * MapView — Leaflet map showing the user's location, nearby drivers,
 * active ride markers and an OSRM-fetched route polyline.
 */
import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import type { DriverLocation } from '@/types';
import { logger } from '@/lib/logger';

// Fix default Leaflet icon paths broken by bundlers
delete (L.Icon.Default.prototype as unknown as Record<string, unknown>)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const userIcon = new L.DivIcon({
  html: '<div class="w-4 h-4 rounded-full bg-blue-500 border-2 border-white shadow-lg"></div>',
  className: '',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
});

const driverIcon = new L.DivIcon({
  html: '<div class="text-2xl leading-none">🚗</div>',
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 14],
});

const pickupIcon = new L.DivIcon({
  html: '<div class="text-2xl leading-none">📍</div>',
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 28],
});

const dropIcon = new L.DivIcon({
  html: '<div class="text-2xl leading-none">🏁</div>',
  className: '',
  iconSize: [28, 28],
  iconAnchor: [14, 28],
});

interface Props {
  userLat?: number;
  userLng?: number;
  drivers?: DriverLocation[];
  pickupLat?: number;
  pickupLng?: number;
  dropoffLat?: number;
  dropoffLng?: number;
  showRoute?: boolean;
  className?: string;
}

/** Re-centres the map when the centre coordinates change. */
const RecenterMap: React.FC<{ lat: number; lng: number }> = ({ lat, lng }) => {
  const map = useMap();
  useEffect(() => {
    map.setView([lat, lng], map.getZoom());
  }, [lat, lng, map]);
  return null;
};

/** Fetches an OSRM driving route between two points and returns GeoJSON coords. */
async function fetchRoute(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number,
): Promise<[number, number][]> {
  try {
    const url = `https://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=full&geometries=geojson`;
    const res = await fetch(url);
    const data = await res.json() as { routes?: Array<{ geometry: { coordinates: [number, number][] } }> };
    if (data.routes && data.routes.length > 0) {
      // OSRM returns [lng, lat] — swap for Leaflet [lat, lng]
      return data.routes[0].geometry.coordinates.map(([lng, lat]) => [lat, lng]);
    }
  } catch (e) {
    logger.error('fetchRoute', e);
  }
  return [];
}

/**
 * Interactive Leaflet map displaying the user, nearby drivers,
 * pickup/drop pins and the driving route.
 */
export const MapView: React.FC<Props> = ({
  userLat,
  userLng,
  drivers = [],
  pickupLat,
  pickupLng,
  dropoffLat,
  dropoffLng,
  showRoute = false,
  className = 'h-64',
}) => {
  const [routeCoords, setRouteCoords] = useState<[number, number][]>([]);

  const centerLat = userLat ?? -17.8252;
  const centerLng = userLng ?? 31.0335; // Default: Harare, Zimbabwe

  useEffect(() => {
    if (showRoute && pickupLat && pickupLng && dropoffLat && dropoffLng) {
      fetchRoute(pickupLat, pickupLng, dropoffLat, dropoffLng).then(setRouteCoords);
    } else {
      setRouteCoords([]);
    }
  }, [showRoute, pickupLat, pickupLng, dropoffLat, dropoffLng]);

  return (
    <div className={`rounded-xl overflow-hidden ${className}`}>
      <MapContainer
        center={[centerLat, centerLng]}
        zoom={14}
        className="h-full w-full"
        zoomControl={false}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {userLat && userLng && (
          <>
            <RecenterMap lat={centerLat} lng={centerLng} />
            <Marker position={[userLat, userLng]} icon={userIcon}>
              <Popup>You are here</Popup>
            </Marker>
          </>
        )}

        {drivers.map((d) => (
          <Marker key={d.driverId} position={[d.lat, d.lng]} icon={driverIcon}>
            <Popup>Driver nearby</Popup>
          </Marker>
        ))}

        {pickupLat && pickupLng && (
          <Marker position={[pickupLat, pickupLng]} icon={pickupIcon}>
            <Popup>Pickup</Popup>
          </Marker>
        )}

        {dropoffLat && dropoffLng && (
          <Marker position={[dropoffLat, dropoffLng]} icon={dropIcon}>
            <Popup>Drop-off</Popup>
          </Marker>
        )}

        {routeCoords.length > 0 && (
          <Polyline positions={routeCoords} color="#f0a929" weight={4} opacity={0.8} />
        )}
      </MapContainer>
    </div>
  );
};
