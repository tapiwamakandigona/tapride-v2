/**
 * AuthGuard — redirects unauthenticated users to /login.
 * Shows a loading spinner while the session is being verified.
 */
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { LoadingSpinner } from './LoadingSpinner';

interface Props {
  children: React.ReactNode;
}

/** Wraps protected routes and ensures the user is authenticated. */
export const AuthGuard: React.FC<Props> = ({ children }) => {
  const { user, isLoading } = useAuthStore();

  if (isLoading) return <LoadingSpinner fullScreen label="Loading…" />;
  if (!user) return <Navigate to="/login" replace />;

  return <>{children}</>;
};
