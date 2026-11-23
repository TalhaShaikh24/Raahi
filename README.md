# Raahi — Car Pooling Prototype (Android / Jetpack Compose)

A polished, local-only Android carpooling marketplace prototype inspired by the Raahi UI/UX & Dummy Data Build Brief. All interactions run locally against reactive in-memory mock repositories with zero backend, payment, or live map dependencies.

## Key Implemented Journeys

- **Quick sign-up (3 steps)**: Animated splash → one welcome screen → phone + 4-digit code (demo: `1234`) → name only. No role question and no long forms; date of birth, emergency contacts and ID verification are asked for later, when a feature needs them.
- **Passenger Explore & Search**: Gradient home hero, Pakistan city/area pickers, origin/destination swap, date pills, seat stepper, saved routes, filters and sorting.
- **Chat & agree on a fare**: Every booking opens a chat with the driver or passenger. A deal panel pinned to the top of the chat shows the current offer with **Accept / Counter / Decline**; each offer appears as a card in the thread, and agreed deals post a "Deal agreed" message. The offer sheet has ± buttons and suggested prices. Use "Demo: reply as driver" to answer as the other side.
- **Offer a ride (driver)**: 3 quick steps (route, time, seats & price) with a live "you could earn" preview. Car, stops, luggage, preferences and audience are optional under **More options**.
- **My Rides**: Three tabs (Upcoming, Requests, Past) with an animated segmented control.
- **Profile**: Essentials up front (mode switch, safety, emergency contacts, help, settings). Favourites, waitlist, payments, promos, earnings and demo tools sit under an expandable **More** section.
- **Motion & design**: Shared motion tokens (`ui/components/Motion.kt`): press-bounce on buttons and cards, staggered entrance animations, rolling price counters, pulsing status dots, animated screen transitions, and a bottom bar that slides away on detail screens. Headings use Plus Jakarta Sans; primary actions use the brand gradient.

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
