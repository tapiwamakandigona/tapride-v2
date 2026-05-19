# Appwrite schema for TapRide
# Run these via Appwrite Console or CLI

DATABASE_ID: tapride_db

## profiles
Attributes:
  userId:        string(36)   required
  fullName:      string(128)  required
  phone:         string(20)   required
  role:          enum [rider, driver] required
  rating:        float        default=5.0
  totalRides:    integer      default=0
  isOnline:      boolean      default=false
  vehicleType:   string(64)
  licensePlate:  string(20)
  avatarUrl:     url

Indexes:
  userId   UNIQUE

Permissions (collection level):
  create: users
  read:   users    (row-level security handles per-doc access)

## rides
Attributes:
  riderId:         string(36) required
  driverId:        string(36)
  status:          enum [requested, accepted, in_progress, completed, cancelled] required
  pickupLat:       float required
  pickupLng:       float required
  pickupAddress:   string(256) required
  dropLat:         float required
  dropLng:         float required
  dropAddress:     string(256) required
  fareEstimate:    float required
  distanceKm:      float required
  requestedAt:     datetime required
  acceptedAt:      datetime
  completedAt:     datetime
  cancelReason:    string(256)

Indexes:
  status
  riderId
  driverId
  [riderId, status]
  [driverId, status]
  [status, requestedAt]

Permissions:
  create: users
  read: users  (row-level: rider + driver only)

## driver_locations
Attributes:
  driverId:      string(36) required
  lat:           float required
  lng:           float required
  isAvailable:   boolean default=false
  heading:       float default=0
  updatedAt:     datetime required

Indexes:
  driverId  UNIQUE
  isAvailable

Permissions:
  create: users
  read: users

## messages
Attributes:
  rideId:       string(36) required
  senderId:     string(36) required
  senderName:   string(128) required
  content:      string(1024) required

Indexes:
  rideId
  [rideId, $createdAt]

Permissions:
  create: users
  read: users

## ratings
Attributes:
  rideId:   string(36) required
  raterId:  string(36) required
  ratedId:  string(36) required
  score:    integer required  (1-5)
  comment:  string(512)

Indexes:
  rideId
  ratedId
  [rideId, raterId]  UNIQUE

Permissions:
  create: users
  read: users
