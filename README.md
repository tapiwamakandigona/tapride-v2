# TapRide v2

A full-featured ride-hailing app built with:
- **Web:** React 18 + TypeScript + Vite + Tailwind CSS
- **Android:** Kotlin + Jetpack Compose + MVVM
- **Backend:** Appwrite (auth, database, realtime, storage)

## Features

- 🚗 Rider and Driver roles
- 📍 Live map with driver locations (OpenStreetMap, no API key needed)
- 💬 In-ride real-time chat
- ⭐ Post-ride ratings
- 📱 Native Android app + responsive web app
- 🔔 Notifications (Web Audio API + Vibration)
- 📊 Ride history

## Project Structure

```
tapride-v2/
  web/        React + TypeScript web app
  android/    Kotlin Android app
  docs/       Implementation plans and ADRs
  .github/
    workflows/
      web-deploy.yml      → deploys web to GitHub Pages on push
      android-build.yml   → builds APK + creates GitHub Release on push
```

## Getting Started — Web

```bash
cd web
cp .env.example .env.local
# Fill in your Appwrite credentials in .env.local
npm install
npm run dev
```

## Getting Started — Android

Build via GitHub Actions (push to `main`, check Releases tab) or locally with Android Studio.

## Appwrite Setup

Create these collections in your Appwrite project (`tapride_db`):

| Collection ID       | Description                        |
|---------------------|------------------------------------|
| `profiles`          | User profiles (role, vehicle info) |
| `rides`             | Ride lifecycle documents           |
| `driver_locations`  | Real-time driver GPS positions     |
| `messages`          | In-ride chat messages              |
| `ratings`           | Post-ride ratings                  |

See `docs/PLAN.md` for full attribute lists and indexes.

## Environment Variables

```env
VITE_APPWRITE_ENDPOINT=https://fra.cloud.appwrite.io/v1
VITE_APPWRITE_PROJECT_ID=your_project_id
VITE_APPWRITE_DATABASE_ID=tapride_db
```

## Architecture Notes

- **No Google Maps API key needed** — uses OpenStreetMap + Leaflet (web) / osmdroid (Android)
- **No PostGIS** — proximity queries use bounding box + haversine post-filter
- **Realtime** — Appwrite Realtime WebSocket for live ride status + chat
- **AI-readable code** — every file has JSDoc/KDoc comments, typed interfaces, no magic strings
