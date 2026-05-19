# TapRide v2 — Implementation Plan

> **Stack:** Kotlin Android (MVVM) + React/TypeScript Web + Appwrite backend
> 
> **Goal:** Full-featured ride-hailing app — functional web site + downloadable APK — clean, bug-free,
> AI-readable codebase ready for future iteration.

---

## Architecture

```
tapride-v2/
  web/          React + TypeScript + Vite + Tailwind — deployed to GitHub Pages or Vercel
  android/      Kotlin + Jetpack Compose + Appwrite Android SDK
  docs/         Plans, schema, ADRs
  .github/
    workflows/  CI: web deploy + android APK build → GitHub Release
```

## Appwrite Schema

**Database ID:** `tapride_db`

### Collections
1. `profiles`        — one per user (role, phone, vehicle info, rating)
2. `rides`           — full ride lifecycle
3. `driver_locations`— high-write GPS pings
4. `messages`        — in-ride chat
5. `ratings`         — post-ride ratings

### Indexes
- rides: `[status]`, `[riderId]`, `[driverId]`, `[riderId,status]`, `[driverId,status]`
- driver_locations: `[driverId]` UNIQUE, `[isAvailable]`
- messages: `[rideId]`, `[rideId,$createdAt]`
- ratings: `[rideId]`, `[ratedId]`

## Screens

### Web + Android (same features)
1. Splash / Auth gate
2. Login / Register
3. Rider Dashboard (map, request ride, see nearby drivers)
4. Driver Dashboard (toggle online, accept/decline rides)
5. Active Ride (live tracking, chat, status)
6. Chat (in-ride messaging)
7. Rate Ride
8. Ride History
9. Profile / Settings
10. Forgot Password / Reset

## CI/CD
- Every push to `main` → build Android APK → attach as GitHub Release artifact
- Web auto-deploys to GitHub Pages
