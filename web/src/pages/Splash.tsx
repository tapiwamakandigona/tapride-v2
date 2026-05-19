/**
 * Splash page — shown briefly on first load before redirecting.
 */
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

/** Animated splash screen that routes users based on auth state. */
const Splash: React.FC = () => {
  const navigate = useNavigate();
  const { user, profile, isLoading } = useAuthStore();

  useEffect(() => {
    if (isLoading) return;
    const timer = setTimeout(() => {
      if (!user) {
        navigate('/login', { replace: true });
      } else if (profile?.role === 'driver') {
        navigate('/driver', { replace: true });
      } else {
        navigate('/rider', { replace: true });
      }
    }, 1500);
    return () => clearTimeout(timer);
  }, [isLoading, user, profile, navigate]);

  return (
    <div className="flex h-screen flex-col items-center justify-center bg-brand-500">
      <div className="mb-4 text-6xl animate-bounce">🚖</div>
      <h1 className="text-4xl font-bold text-white tracking-tight">TapRide</h1>
      <p className="mt-2 text-brand-100 text-sm">Your ride, one tap away</p>
    </div>
  );
};

export default Splash;
