/**
 * Notification helpers using the Web Audio API (beep) and
 * the Vibration API.  Both are no-ops when the APIs are unavailable
 * (e.g. during SSR or in environments that block them).
 */

/**
 * Play a short beep tone to alert the user.
 * @param frequency - Hz (default 880 = A5)
 * @param durationMs - Duration in milliseconds (default 200)
 */
export function beep(frequency = 880, durationMs = 200): void {
  try {
    const ctx = new AudioContext();
    const oscillator = ctx.createOscillator();
    const gain = ctx.createGain();

    oscillator.connect(gain);
    gain.connect(ctx.destination);

    oscillator.type = 'sine';
    oscillator.frequency.value = frequency;
    gain.gain.setValueAtTime(0.3, ctx.currentTime);

    oscillator.start(ctx.currentTime);
    oscillator.stop(ctx.currentTime + durationMs / 1000);

    oscillator.onended = () => {
      ctx.close();
    };
  } catch {
    // Audio API not available — silently ignore
  }
}

/**
 * Vibrate the device (mobile) using the Vibration API.
 * @param pattern - Vibration pattern in ms (default: [200])
 */
export function vibrate(pattern: number | number[] = [200]): void {
  try {
    if (navigator.vibrate) {
      navigator.vibrate(pattern);
    }
  } catch {
    // Vibration API not available
  }
}

/**
 * Combined ride-request alert: beep + vibrate.
 */
export function rideRequestAlert(): void {
  beep(660, 300);
  vibrate([200, 100, 200]);
}
