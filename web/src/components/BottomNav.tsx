/**
 * BottomNav — persistent navigation bar for authenticated users.
 * Shows different tabs based on the user's role.
 */
import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

interface NavItem {
  to: string;
  icon: string;
  label: string;
}

const RIDER_ITEMS: NavItem[] = [
  { to: '/rider', icon: '🏠', label: 'Home' },
  { to: '/history', icon: '📋', label: 'History' },
  { to: '/profile', icon: '👤', label: 'Profile' },
];

const DRIVER_ITEMS: NavItem[] = [
  { to: '/driver', icon: '🚗', label: 'Rides' },
  { to: '/history', icon: '📋', label: 'History' },
  { to: '/profile', icon: '👤', label: 'Profile' },
];

/** Role-aware bottom navigation bar. */
export const BottomNav: React.FC = () => {
  const { profile } = useAuthStore();
  if (!profile) return null;

  const items = profile.role === 'driver' ? DRIVER_ITEMS : RIDER_ITEMS;

  return (
    <nav className="fixed bottom-0 inset-x-0 z-40 bg-white border-t border-gray-200 flex">
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.to !== '/history' && item.to !== '/profile'}
          className={({ isActive }) =>
            `flex flex-1 flex-col items-center py-2 text-xs gap-1 transition-colors ${
              isActive ? 'text-brand-600 font-semibold' : 'text-gray-500'
            }`
          }
        >
          <span className="text-xl leading-none">{item.icon}</span>
          {item.label}
        </NavLink>
      ))}
    </nav>
  );
};
