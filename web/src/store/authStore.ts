/**
 * Zustand store for authentication state.
 * Holds the current Appwrite user and their TapRide profile.
 */
import { create } from 'zustand';
import type { Models } from 'appwrite';
import type { Profile } from '@/types';

interface AuthState {
  user: Models.User<Models.Preferences> | null;
  profile: Profile | null;
  isLoading: boolean;
  setUser: (user: Models.User<Models.Preferences> | null) => void;
  setProfile: (profile: Profile | null) => void;
  setLoading: (loading: boolean) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  profile: null,
  isLoading: true,
  setUser: (user) => set({ user }),
  setProfile: (profile) => set({ profile }),
  setLoading: (isLoading) => set({ isLoading }),
  clear: () => set({ user: null, profile: null, isLoading: false }),
}));
