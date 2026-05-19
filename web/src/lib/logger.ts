/// <reference types="vite/client" />

/**
 * Logger utility — logs to the console in development,
 * silently no-ops in production builds.
 */

const isDev = import.meta.env.DEV;

export const logger = {
  log: (...args: unknown[]) => {
    if (isDev) console.log('[TapRide]', ...args);
  },
  warn: (...args: unknown[]) => {
    if (isDev) console.warn('[TapRide]', ...args);
  },
  error: (...args: unknown[]) => {
    if (isDev) console.error('[TapRide]', ...args);
  },
};
