/**
 * Register page — creates a new Appwrite user + profile document.
 */
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import type { UserRole } from '@/types';

/** Registration form for new riders and drivers. */
const Register: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('rider');
  const [vehicleType, setVehicleType] = useState('');
  const [licensePlate, setLicensePlate] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { register, error } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const ok = await register(email, password, fullName, phone, role);
    setSubmitting(false);
    if (ok) {
      navigate(role === 'driver' ? '/driver' : '/rider', { replace: true });
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4 py-10">
      <div className="mb-8 text-center">
        <div className="text-5xl mb-2">🚖</div>
        <h1 className="text-2xl font-bold text-gray-900">Create account</h1>
        <p className="text-sm text-gray-500 mt-1">Join TapRide today</p>
      </div>

      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-white rounded-2xl p-6 shadow-md">
        {error && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Role selector */}
        <div className="flex gap-3">
          {(['rider', 'driver'] as UserRole[]).map((r) => (
            <button
              key={r}
              type="button"
              onClick={() => setRole(r)}
              className={`flex-1 rounded-lg border py-2.5 text-sm font-medium transition-colors capitalize ${
                role === r
                  ? 'border-brand-500 bg-brand-50 text-brand-700'
                  : 'border-gray-300 text-gray-600'
              }`}
            >
              {r === 'rider' ? '🧑 Rider' : '🚗 Driver'}
            </button>
          ))}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Full Name</label>
          <input
            type="text"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
            placeholder="John Doe"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
            placeholder="you@example.com"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Phone</label>
          <input
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
            placeholder="+263..."
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
            placeholder="Min. 8 characters"
          />
        </div>

        {role === 'driver' && (
          <>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Vehicle Type</label>
              <input
                type="text"
                value={vehicleType}
                onChange={(e) => setVehicleType(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
                placeholder="e.g. Toyota Vitz"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">License Plate</label>
              <input
                type="text"
                value={licensePlate}
                onChange={(e) => setLicensePlate(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400"
                placeholder="e.g. ABC 1234"
              />
            </div>
          </>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-lg bg-brand-500 py-2.5 text-sm font-semibold text-white hover:bg-brand-600 disabled:opacity-60 transition-colors"
        >
          {submitting ? <LoadingSpinner size="sm" /> : 'Create Account'}
        </button>

        <p className="text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Sign in
          </Link>
        </p>
      </form>
    </div>
  );
};

export default Register;
