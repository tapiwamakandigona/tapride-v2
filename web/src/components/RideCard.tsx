/**
 * RideCard — summary card for a ride, used in history and listings.
 */
import React from 'react';
import type { Ride } from '@/types';
import { StatusBadge } from './StatusBadge';
import { formatFare } from '@/lib/fare';

interface Props {
  ride: Ride;
  onClick?: () => void;
}

/** Displays a compact ride summary with status, addresses and fare. */
export const RideCard: React.FC<Props> = ({ ride, onClick }) => {
  const date = new Date(ride.$createdAt).toLocaleDateString();

  return (
    <div
      onClick={onClick}
      className={`rounded-xl bg-white p-4 shadow-sm border border-gray-100 ${onClick ? 'cursor-pointer hover:shadow-md transition-shadow' : ''}`}
    >
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs text-gray-400">{date}</span>
        <StatusBadge status={ride.status} />
      </div>
      <div className="space-y-1 text-sm">
        <div className="flex items-start gap-2">
          <span className="mt-0.5 h-2 w-2 rounded-full bg-green-500 flex-shrink-0" />
          <span className="text-gray-700 line-clamp-1">{ride.pickupAddress}</span>
        </div>
        <div className="flex items-start gap-2">
          <span className="mt-0.5 h-2 w-2 rounded-full bg-red-500 flex-shrink-0" />
          <span className="text-gray-700 line-clamp-1">{ride.dropoffAddress}</span>
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between text-sm">
        <span className="text-gray-500">{ride.distanceKm.toFixed(1)} km</span>
        <span className="font-semibold text-brand-600">{formatFare(ride.fare)}</span>
      </div>
    </div>
  );
};
