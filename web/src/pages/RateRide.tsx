/**
 * RateRide page — lets the rider rate their driver after a completed ride.
 */
import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ID, AppwriteException } from 'appwrite';
import { databases, DATABASE_ID, COLLECTIONS } from '@/lib/appwrite';
import { useAuthStore } from '@/store/authStore';
import { useRideStore } from '@/store/rideStore';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { logger } from '@/lib/logger';

/** Star-rating form submitted to the ratings collection. */
const RateRide: React.FC = () => {
  const { rideId } = useParams<{ rideId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { activeRide } = useRideStore();

  const [score, setScore] = useState<1 | 2 | 3 | 4 | 5>(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const driverId = activeRide?.driverId;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !rideId || !driverId) return;
    setError(null);
    setSubmitting(true);
    try {
      await databases.createDocument(DATABASE_ID, COLLECTIONS.RATINGS, ID.unique(), {
        rideId,
        raterId: user.$id,
        ratedId: driverId,
        score,
        comment: comment.trim(),
        createdAt: new Date().toISOString(),
      });
      navigate('/rider', { replace: true });
    } catch (e) {
      const msg = e instanceof AppwriteException ? e.message : 'Failed to submit rating';
      setError(msg);
      logger.error('RateRide', e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-sm space-y-5 bg-white rounded-2xl p-6 shadow-md"
      >
        <div className="text-center">
          <div className="text-4xl mb-2">⭐</div>
          <h1 className="text-xl font-bold text-gray-900">Rate your driver</h1>
          <p className="text-sm text-gray-500 mt-1">How was your ride?</p>
        </div>

        {error && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Star selector */}
        <div className="flex justify-center gap-3">
          {([1, 2, 3, 4, 5] as const).map((s) => (
            <button
              key={s}
              type="button"
              onClick={() => setScore(s)}
              className={`text-3xl transition-transform hover:scale-110 ${
                s <= score ? 'opacity-100' : 'opacity-30'
              }`}
            >
              ⭐
            </button>
          ))}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Comment (optional)
          </label>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={3}
            className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
            placeholder="Tell us more…"
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-brand-500 py-2.5 text-sm font-semibold text-white hover:bg-brand-600 disabled:opacity-60 transition-colors"
        >
          {submitting ? <LoadingSpinner size="sm" /> : 'Submit Rating'}
        </button>

        <button
          type="button"
          onClick={() => navigate('/rider')}
          className="w-full text-center text-sm text-gray-400 hover:text-gray-600"
        >
          Skip
        </button>
      </form>
    </div>
  );
};

export default RateRide;
