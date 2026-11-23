# Raahi — Car Pooling Prototype (Android / Jetpack Compose)

A polished, local-only Android carpooling marketplace prototype inspired by the Raahi UI/UX & Dummy Data Build Brief. All interactions run locally against reactive in-memory mock repositories with zero backend, payment, or live map dependencies.

## Key Implemented Journeys

- **Welcome & Onboarding**: Hero illustration, "Find a ride", "Offer a ride", and "Explore demo".
- **Passenger Explore & Search**: Pakistan city/area pickers (Karachi, Hyderabad, Lahore, Islamabad, Rawalpindi), origin/destination swap, date chips, seat stepper, quick recent searches, filter bottom sheet, and sorting.
- **Ride Details & Booking Request**: Interactive route stop timeline (select pickup & drop-off), driver verification & reviews, vehicle details, listed-fare booking, or proposing a custom PKR counter-offer.
- **Counter-Offer Negotiation (`B-1001` & custom bookings)**: Chronological offer timeline preserving every prior offer, turn-based Accept / Decline / Counter actions, static demo expiry indicator, and role-switching to test both Passenger and Driver replies.
- **My Trips & Booking Details**: Segmented tabs (Upcoming, Pending, Completed, Cancelled), fare breakdown, mock chat launch, simulated trip share, and cancellation with seat restoration.
- **Driver Hub, Publish Ride & Requests**: Role switch pill, quick stats, multi-section Publish Ride form with inline validation and vehicle capacity enforcement, and incoming passenger request management.
- **Inbox, Notifications, Profile & Safety**: Local demo chat composer, tappable activity notifications, mock garage & saved places, explicit Demo SOS disclaimer, and **Reset demo data**.

## Project Structure

- `com.example.model`: Typed domain models and status enums (`Ride`, `Booking`, `CounterOffer`, `DriverProfile`, `Vehicle`, etc.).
- `com.example.data.repository`: Clean repository interfaces (`RideRepository`, `BookingRepository`, `UserRepository`, `InboxRepository`).
- `com.example.data.mock`: `MockDataFactory` and `MockCarPoolStore` (single source of truth).
- `com.example.ui.theme`: Centralized Material 3 color tokens, `Plus Jakarta Sans` + `Inter` typography, and shapes.
- `com.example.ui.components`: Reusable ride cards, route timeline, driver mini-profile, status chips, steppers, and pickers.
- `com.example.ui.screens`: Modular Compose screens for onboarding, explore, search, ride details, booking, negotiation, trips, driver hub, publish ride, inbox/chat, profile, and safety.

## Libraries Used

- **Jetpack Compose + Material 3**: Declarative modern Android UI and adaptive NavigationBar / NavigationRail.
- **Navigation Compose**: Type-safe screen routing and backstack management.
- **Kotlin Coroutines & StateFlow**: Reactive local state updates across repositories and ViewModel.
- **Robolectric + JUnit**: Local JVM unit and Android resource tests.

## Deferred to Backend / Production Phase

- Real authentication / OTP verification
- Remote REST/GraphQL API & persistent cloud database
- Live GPS routing & map SDK integration
- Payment gateway / PKR wallet settlement
- Real-time push notifications, SMS, and CNIC/license verification

## How to Run & Reset Demo Data

1. Open and build the Android project (`gradle assembleDebug` / `gradle :app:testDebugUnitTest`).
2. To restore all initial rides, `B-1001` counter-offer history, and bookings during testing, go to **Profile → Reset demo data**.
