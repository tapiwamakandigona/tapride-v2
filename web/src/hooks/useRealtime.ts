/**
 * Generic hook that subscribes to an Appwrite Realtime channel
 * and calls the provided callback whenever an event arrives.
 * Automatically unsubscribes when the component unmounts or
 * when the channel string changes.
 *
 * Circuit-breaker: if the WebSocket fails to deliver any event within
 * CIRCUIT_OPEN_MS after subscribing, we treat the connection as dead and
 * stop retrying — polling fallbacks cover all critical paths.
 */
import { useEffect, useRef } from 'react';
import { subscribe } from '@/lib/appwrite';
import { logger } from '@/lib/logger';

type RealtimeEvent<T> = { events: string[]; payload: T };
type Callback<T> = (event: RealtimeEvent<T>) => void;

/** After this many ms with no activity we assume Realtime is broken and stop. */
const CIRCUIT_OPEN_MS = 15_000;

/** Track whether Realtime is globally disabled for this page load. */
let realtimeDisabled = false;

/**
 * @param channel - Appwrite Realtime channel string, e.g.
 *   `databases.tapride_db.collections.rides.documents`
 * @param callback - Function called on each event
 */
export function useRealtime<T>(channel: string, callback: Callback<T>): void {
  const callbackRef = useRef(callback);
  callbackRef.current = callback;

  useEffect(() => {
    if (!channel || realtimeDisabled) return;

    logger.log('Subscribing to', channel);
    let receivedEvent = false;
    let unsubscribed = false;

    const unsubscribe = subscribe<T>(channel, (event) => {
      receivedEvent = true;
      callbackRef.current(event);
    });

    // Circuit breaker: if no event arrives in time, disable Realtime globally
    const timer = setTimeout(() => {
      if (!receivedEvent && !unsubscribed) {
        logger.warn('Realtime circuit open — no events received, disabling WebSocket');
        realtimeDisabled = true;
        unsubscribe();
      }
    }, CIRCUIT_OPEN_MS);

    return () => {
      unsubscribed = true;
      clearTimeout(timer);
      logger.log('Unsubscribing from', channel);
      unsubscribe();
    };
  }, [channel]);
}
