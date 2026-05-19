/**
 * ForgotPassword page — sends a password reset email via Appwrite.
 */
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { LoadingSpinner } from '@/components/LoadingSpinner';

/** Sends a password reset link to the provided email address. */
const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);
  const { sendPasswordReset, error } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const ok = await sendPasswordReset(email);
    setSubmitting(false);
    if (ok) setSent(true);
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4">
      <div className="mb-8 text-center">
        <div className="text-5xl mb-2">🔑</div>
        <h1 className="text-2xl font-bold text-gray-900">Reset password</h1>
        <p className="text-sm text-gray-500 mt-1">We&apos;ll send a reset link to your email</p>
      </div>

      {sent ? (
        <div className="w-full max-w-sm rounded-2xl bg-green-50 border border-green-200 p-6 text-center">
          <p className="text-green-700 font-medium">Email sent!</p>
          <p className="text-sm text-green-600 mt-1">Check your inbox for the reset link.</p>
          <Link to="/login" className="mt-4 inline-block text-sm text-brand-600 hover:underline">
            Back to login
          </Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-white rounded-2xl p-6 shadow-md">
          {error && (
            <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700">
              {error}
            </div>
          )}

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

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-lg bg-brand-500 py-2.5 text-sm font-semibold text-white hover:bg-brand-600 disabled:opacity-60 transition-colors"
          >
            {submitting ? <LoadingSpinner size="sm" /> : 'Send Reset Link'}
          </button>

          <p className="text-center text-sm text-gray-500">
            <Link to="/login" className="text-brand-600 hover:underline">
              Back to login
            </Link>
          </p>
        </form>
      )}
    </div>
  );
};

export default ForgotPassword;
