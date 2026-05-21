/**
 * Root application component — sets up React Router with all routes
 * and applies the AuthGuard to protected pages.
 * useAuth() is called once here at the root so that isLoading is set
 * to false as soon as the Appwrite session check completes.
 */
import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthGuard } from '@/components/AuthGuard';
import { BottomNav } from '@/components/BottomNav';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { useAuth } from '@/hooks/useAuth';

// Lazy-loaded pages for code splitting
const Splash = lazy(() => import('@/pages/Splash'));
const Login = lazy(() => import('@/pages/Login'));
const Register = lazy(() => import('@/pages/Register'));
const ForgotPassword = lazy(() => import('@/pages/ForgotPassword'));
const RiderDashboard = lazy(() => import('@/pages/RiderDashboard'));
const DriverDashboard = lazy(() => import('@/pages/DriverDashboard'));
const ActiveRide = lazy(() => import('@/pages/ActiveRide'));
const ChatPage = lazy(() => import('@/pages/ChatPage'));
const RateRide = lazy(() => import('@/pages/RateRide'));
const RideHistory = lazy(() => import('@/pages/RideHistory'));
const Profile = lazy(() => import('@/pages/Profile'));

/** Protected layout wrapper — renders children + bottom nav */
const ProtectedLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <AuthGuard>
    <>
      {children}
      <BottomNav />
    </>
  </AuthGuard>
);

/**
 * Inner router component — must be inside BrowserRouter so hooks work.
 * Calls useAuth() once here so the session check runs on every page load.
 */
const AppRoutes: React.FC = () => {
  useAuth(); // triggers init() → setLoading(false) after Appwrite session check

  return (
    <Suspense fallback={<LoadingSpinner fullScreen />}>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<Splash />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* Protected routes */}
        <Route
          path="/rider"
          element={
            <ProtectedLayout>
              <RiderDashboard />
            </ProtectedLayout>
          }
        />
        <Route
          path="/driver"
          element={
            <ProtectedLayout>
              <DriverDashboard />
            </ProtectedLayout>
          }
        />
        <Route
          path="/ride/:rideId"
          element={
            <AuthGuard>
              <ActiveRide />
            </AuthGuard>
          }
        />
        <Route
          path="/chat/:rideId"
          element={
            <AuthGuard>
              <ChatPage />
            </AuthGuard>
          }
        />
        <Route
          path="/rate/:rideId"
          element={
            <ProtectedLayout>
              <RateRide />
            </ProtectedLayout>
          }
        />
        <Route
          path="/history"
          element={
            <ProtectedLayout>
              <RideHistory />
            </ProtectedLayout>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedLayout>
              <Profile />
            </ProtectedLayout>
          }
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
};

const App: React.FC = () => (
  <BrowserRouter basename={import.meta.env.BASE_URL}>
    <AppRoutes />
  </BrowserRouter>
);

export default App;
