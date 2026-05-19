/**
 * Appwrite singleton — exports a single client, account, databases,
 * and realtime subscribe helper so we don't recreate them on every import.
 */
import { Client, Account, Databases } from 'appwrite';

const ENDPOINT = 'https://fra.cloud.appwrite.io/v1';
const PROJECT_ID = '69e62515000e9e781653';

export const DATABASE_ID = 'tapride_db';

// Collection IDs
export const COLLECTIONS = {
  PROFILES: 'profiles',
  RIDES: 'rides',
  DRIVER_LOCATIONS: 'driver_locations',
  MESSAGES: 'messages',
  RATINGS: 'ratings',
} as const;

const client = new Client().setEndpoint(ENDPOINT).setProject(PROJECT_ID);

export const account = new Account(client);
export const databases = new Databases(client);

/**
 * Subscribe to one or more Appwrite Realtime channels.
 * Returns an unsubscribe function.
 */
export function subscribe<T>(
  channels: string | string[],
  callback: (response: { events: string[]; payload: T }) => void,
): () => void {
  return client.subscribe(channels, callback as Parameters<typeof client.subscribe>[1]);
}

export default client;
