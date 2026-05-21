/**
 * Generic hook that subscribes to an Appwrite Realtime channel
 * and calls the provided callback whenever an event arrives.
 * Automatically unsubscribes when the component unmounts or
 * when the channel string changes.
 *
 * Falls back gracefully if WebSocket is unavailable — polling
 * fallbacks in useChat and useRide cover all critical paths.
 */
import { useEffect, useRef } from 'react';
import { subscribe } from '@/lib/appwrite';
import { logger } from '@/lib/logger';

type RealtimeEvent<T> = { events: string[]; payload: T };
type Callback<T> = (event: RealtimeEvent<T>) => void;

/**
 * @param channel - Appwrite Realtime channel string, e.g.
 *   `databases.tapride_db.collections.rides.documents`
 * @param callback - Function called on each event
 */
export function useRealtime<T>(channel: string, callback: Callback<T>): void {
  const callbackRef = useRef(callback);
  callbackRef.current = callback;

  useEffect(() => {
    if (!channel) return;

    logger.log('Subscribing to', channel);

    const unsubscribe = subscribe<T>(channel, (event) => {
      callbackRef.current(event);
    });

    return () => {
      logger.log('Unsubscribing from', channel);
      unsubscribe();
    };
  }, [channel]);
}
