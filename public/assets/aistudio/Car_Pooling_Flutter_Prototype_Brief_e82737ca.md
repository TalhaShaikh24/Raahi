# Car Pooling Flutter Prototype --- UI/UX & Dummy Data Build Brief

**Document type:** AI coding-agent handoff\
**Goal:** Build a polished, modern, highly usable Flutter prototype
using local dummy data only.\
**Backend status:** No API, database, authentication server, payment
gateway, maps API, or real-time service is required in this phase.

------------------------------------------------------------------------

## 1. Mission

Build a visually impressive but practical car-pooling mobile app
prototype. The app should help a person quickly understand two things:

1.  **Find a ride** as a passenger.
2.  **Offer a ride** as a driver.

The experience should feel trustworthy, calm, modern, friendly, and
exceptionally easy to use. Prioritize clarity and task completion over
decorative effects. Use realistic dummy content throughout so the
prototype feels like a real product rather than a collection of empty
screens.

The prototype must be runnable on an emulator or physical device and
should support complete user journeys with local state. All interactions
should work locally, even though data is not connected to a backend.

## 2. Scope and non-goals

### In scope

-   Flutter mobile application UI.
-   Responsive layouts for common phone sizes.
-   Local dummy data and mock repositories.
-   Working navigation and interactive controls.
-   Passenger and driver journeys.
-   Ride search, ride details, booking request, counter-offer
    negotiation.
-   Driver ride publishing and booking request management.
-   Local notifications/in-app activity simulation.
-   Profile, saved rides, booking history and settings screens.
-   Empty, loading, error, success and confirmation states.
-   Reusable design system and components.

### Explicitly out of scope for this prototype

-   Real API calls or backend integration.
-   Real login/OTP verification.
-   Real payment processing or wallet.
-   Real GPS tracking or route calculation.
-   Real push notifications, SMS or email.
-   Real document verification.
-   Production security, compliance or deployment.

Do not add fake API integrations. Keep service/repository interfaces
clean so real APIs can be connected later without rewriting the UI.

------------------------------------------------------------------------

## 3. Product concept and target experience

The product is a planned-trip car-pooling marketplace, not a taxi
dispatch clone. Drivers publish journeys they intend to take. Passengers
find a suitable portion of a route and request one or more seats.

A passenger should be able to:

-   Search from one place to another.
-   Select a travel date and number of seats.
-   Compare available rides.
-   Review driver, vehicle, route, price and preferences.
-   Request a seat at the listed fare or propose a counter-offer.
-   Negotiate with the driver through a clear offer timeline.
-   See booking status and trip information.

A driver should be able to:

-   Add a vehicle using mock data.
-   Publish a ride with route, date, time, seats and fare.
-   Review passenger requests.
-   Accept, reject or counter a passenger's fare.
-   See upcoming and completed rides.

A user can act as both passenger and driver. Provide a simple role/mode
switch in the UI rather than forcing separate accounts.

------------------------------------------------------------------------

## 4. Visual direction

### Design keywords

**Modern • Premium • Friendly • Trustworthy • Minimal • Spacious • Human
• Effortless**

Avoid a generic admin-dashboard appearance. This is a consumer mobile
product.

### Suggested visual system

Use this as a starting point; refine it consistently:

-   Primary: deep indigo / midnight navy, e.g. `#243B6B`.
-   Accent: fresh teal / mint, e.g. `#19A995`.
-   Highlight: warm amber, e.g. `#F4B544`.
-   Background: soft off-white, e.g. `#F7F8FC`.
-   Surface: white, e.g. `#FFFFFF`.
-   Main text: dark charcoal, e.g. `#182230`.
-   Secondary text: cool grey, e.g. `#687386`.
-   Success: accessible green.
-   Warning: amber.
-   Error: clear red.

Use a light theme for the first prototype. Keep colors in centralized
theme tokens so a dark theme can be added later.

### Typography

-   Use a clean, highly legible typeface such as Inter or a suitable
    bundled/system alternative.
-   Establish a clear hierarchy: screen title, section title, card
    title, supporting text, metadata.
-   Avoid tiny labels and low-contrast grey text.
-   Use sentence case, not excessive uppercase labels.

### Shape, spacing and elevation

-   Consistent 8-point spacing system.
-   Cards: approximately 16--20 px corner radius.
-   Inputs and buttons: approximately 12--16 px corner radius.
-   Subtle shadows only where they improve grouping.
-   Comfortable touch targets (ideally at least 44--48 logical pixels).
-   Use whitespace intentionally; avoid both cramped layouts and huge
    empty areas.

### Iconography and imagery

-   Use one consistent icon family (Material Symbols or Lucide if
    package support is appropriate).
-   Use simple route illustrations, map-like placeholders, vehicle
    thumbnails and human-friendly avatars.
-   Use bundled/local assets or tasteful gradients/illustrations. Do not
    depend on remote image URLs.
-   If using generated placeholder avatars, keep them consistent and
    respectful.
-   Never use emoji as the primary icon system.

### Motion

-   Use short, purposeful transitions (roughly 150--250 ms).
-   Add subtle button feedback, bottom-sheet transitions and success
    confirmations.
-   Avoid excessive animations, parallax or distracting motion.
-   Respect reduced-motion preferences where practical.

------------------------------------------------------------------------

## 5. App navigation and information architecture

Use a bottom navigation bar with four or five primary destinations.
Recommended passenger-first navigation:

1.  **Explore** --- search and discover rides.
2.  **My Trips** --- upcoming, pending, completed and cancelled
    bookings.
3.  **Offer a Ride** --- prominent central action or clearly visible
    tab.
4.  **Inbox** --- messages and activity notifications.
5.  **Profile** --- personal details, preferences and settings.

If a central "Offer a Ride" tab makes navigation awkward, use four tabs
and a prominent action button on the home screen. Choose one approach
and apply it consistently.

Provide a role/mode switch in the profile or home header: - Passenger
mode - Driver mode

Do not duplicate every screen for each role. Reuse shared components and
show role-specific actions contextually.

------------------------------------------------------------------------

## 6. Required screens

### A. Welcome / lightweight onboarding

-   Simple product introduction.
-   Two clear actions: "Find a ride" and "Offer a ride".
-   Optional "Explore demo" action to bypass onboarding.
-   Do not create a long onboarding questionnaire.

### B. Explore / passenger home

This is the most important screen.

Include: - Friendly greeting and profile avatar. - Prominent "Where are
you going?" search card. - From and To fields with swap action. - Date
selector. - Seat count selector. - Search rides CTA. - Quick recent
searches. - A small "How it works" or safety reassurance area. - A few
recommended/upcoming ride cards using dummy data.

The first screen should make the next action obvious. Do not overwhelm
the user with too many sections.

### C. Search results

Include: - Search summary at the top (origin, destination, date,
seats). - Edit search action. - Filter and sort controls. - Ride cards
with: - Driver avatar, name and verified indicator. - Rating and number
of reviews. - Origin and destination. - Departure time and estimated
duration. - Available seats. - Per-seat price and currency. - Vehicle
name/type. - Small route-match label, e.g. "Great route match". - Useful
filters: departure time, price range, seats, verified drivers,
preferences. - Sorting: recommended, earliest departure, lowest price. -
Empty state with a helpful way to change search criteria.

### D. Ride details

Include: - Route overview with origin, destination and optional
intermediate stops. - Departure date/time and estimated duration. -
Driver profile, rating and verification. - Vehicle details and passenger
capacity. - Per-seat fare, seat availability and cancellation summary. -
Ride preferences: luggage, pets, smoking, music, etc. - Pickup/drop-off
selection if multiple stops are supported. - Primary action: "Request to
join". - Secondary action: "Make an offer" or allow offer amount within
booking flow. - Safety/report action.

### E. Booking request / fare offer

Make the fare interaction easy to understand.

Show: - Selected ride and seat count. - Driver's asking fare per seat. -
Number of seats. - Calculated total at asking fare. - Optional passenger
note. - Offer choice: - Continue at listed fare. - Make a
counter-offer. - If counter-offer is selected, show a numeric amount
field and a short explanation that the driver can accept, reject or
counter. - Show total proposed fare clearly. - Confirm request button. -
Friendly validation for invalid or empty values.

### F. Counter-offer negotiation

This is a signature feature and must be designed especially well.

Use a clear conversation/timeline pattern, not a confusing form.

Show: - Ride summary. - Current negotiation status. - Original listed
fare. - Offer history with actor labels, timestamps and amounts. - Each
proposal as a distinct event: - Driver offer. - Passenger
counter-offer. - Driver counter-offer. - One pending offer at a time. -
Only show response actions to the recipient of the current pending
offer. - Actions: Accept, Reject, Make counter-offer. - Offer expiry
indicator using a static demo countdown label or fixed "Expires in 12
min" sample. Do not implement a real countdown unless needed. - After
acceptance, show agreed price and next booking status. - After
rejection/expiry, show a clear next step. - Use dummy state transitions
so buttons visibly update the timeline and status.

Important prototype rule: preserve every previous offer in local state.
Do not overwrite the earlier offer when a new counter-offer is created.

### G. My Trips

Use tabs or segmented controls: - Upcoming - Pending - Completed -
Cancelled

Each trip card should show route, date, time, status, seat count and
agreed fare. Tap opens booking details.

### H. Booking details

Include: - Booking status and timeline. - Ride and route summary. -
Driver/passenger details. - Agreed fare and price breakdown. - Chat
entry point (mock). - Cancel booking action with confirmation dialog. -
Share trip (mock share sheet or local confirmation). - Report issue /
safety entry point. - Show counter-offer history when applicable.

### I. Driver dashboard

Include: - Greeting and driver mode indicator. - Primary "Publish a
ride" action. - Upcoming rides. - Pending booking requests. - Quick
stats using dummy data: active rides, seats booked, completed rides. -
Clear empty state for a new driver.

### J. Publish a ride flow

Use a multi-step form or a well-structured single page. Keep it simple
and avoid unnecessary fields.

Required fields: - Origin and destination. - Optional stops. - Travel
date. - Departure time. - Estimated duration or arrival time. - Vehicle
selection. - Available passenger seats. - Price per seat. - Luggage
capacity. - Preferences: pets, smoking, music, women-only matching if
this product policy is enabled. - Additional note. - Preview and
publish.

Use local validation: - Origin and destination must differ. - Date/time
must be in the future. - Seats cannot exceed the vehicle's passenger
capacity. - Price must be greater than zero. - Required fields must be
completed.

After publishing, show a success screen and add the ride to the local
driver dashboard.

### K. Booking requests (driver)

Include: - Request list with passenger profile, rating, seats requested
and requested fare. - Listed fare versus passenger offer. - Open
negotiation. - Accept, reject or counter-offer actions. - Confirmation
dialogs for destructive actions. - Update local seat availability when a
request is confirmed.

### L. Inbox / messages

-   Conversation list with avatar, name, ride context, latest message
    and unread indicator.
-   Mock chat screen with sent/received bubbles.
-   Composer and send action that adds a message locally.
-   Clearly label that messages are demo data if needed.
-   No real-time backend required.

### M. Notifications / activity

Show sample activity: - New booking request. - Counter-offer received. -
Offer accepted. - Ride reminder. - Booking cancelled. - Review request.

Tapping a notification should navigate to the relevant mock screen where
practical.

### N. Profile and settings

Include: - Profile details and avatar. - Passenger/driver mode switch. -
My vehicles (mock). - Saved places. - Preferences. - Verification status
as demo-only. - Help and support. - Terms, privacy and app settings. -
Sign out (local demo reset or return to welcome).

### O. Safety and support

Provide prototype screens or sheets for: - Share trip. - Emergency
contacts. - SOS demo confirmation (must clearly state that no emergency
service is contacted in prototype). - Report a user/ride. - Help and
support.

Never imply that the prototype's SOS button contacts real emergency
services.

------------------------------------------------------------------------

## 7. Dummy data requirements

Create a centralized mock data layer. Avoid hardcoding separate
inconsistent values inside each widget.

Use believable Pakistan-based sample locations and PKR prices. Example
cities and areas:

-   Karachi: Clifton, Gulshan-e-Iqbal, DHA, Saddar, Shahrah-e-Faisal.
-   Hyderabad: Latifabad, Qasimabad, Autobhan Road, Hyderabad Bypass.
-   Lahore: Gulberg, DHA, Johar Town, Model Town.
-   Islamabad/Rawalpindi: Blue Area, F-10, G-11, Saddar, Bahria Town.

Use fictional people and vehicles. Do not use real private individuals'
personal information.

### Sample drivers

  ID      Name            Rating   Reviews Vehicle
  ------- ------------- -------- --------- ---------------------
  D-001   Ahsan Malik        4.9       128 Toyota Corolla 2022
  D-002   Sara Ahmed         4.8        86 Honda City 2021
  D-003   Hamza Khan         4.7        54 Suzuki Swift 2023
  D-004   Zoya Ali           5.0        32 Kia Picanto 2022

### Sample rides

  ---------------------------------------------------------------------------------
  Ride ID    Route             Date/time            Seats      Fare/seat Driver
  ---------- ----------------- ----------- -------------- -------------- ----------
  R-001      Hyderabad →       22 Nov,                  3      PKR 1,000 Ahsan
             Karachi           08:00                                     Malik

  R-002      Karachi →         22 Nov,                  2        PKR 900 Sara Ahmed
             Hyderabad         17:30                                     

  R-003      Lahore →          23 Nov,                  3      PKR 2,200 Hamza Khan
             Islamabad         07:00                                     

  R-004      Islamabad →       24 Nov,                  1      PKR 2,000 Zoya Ali
             Lahore            09:00                                     

  R-005      Gulshan-e-Iqbal → 22 Nov,                  2        PKR 350 Ahsan
             Clifton           09:15                                     Malik
  ---------------------------------------------------------------------------------

The sample dates are illustrative. Make the demo data generator use
dates relative to the current date or provide a clearly labeled fixed
demo date so search results do not appear mysteriously outdated.

### Sample counter-offer history

For booking `B-1001`, create this example:

  Sequence   Actor         Offer per seat Status
  ---------- ----------- ---------------- -----------
  1          Driver             PKR 1,000 Countered
  2          Passenger            PKR 800 Countered
  3          Driver               PKR 900 Pending

The passenger can accept PKR 900, reject it, or counter with another
amount. If accepted, store PKR 900 as the agreed fare and preserve the
full history.

### Sample bookings

Include at least: - One confirmed upcoming booking. - One pending
request awaiting driver response. - One booking in negotiation. - One
completed booking. - One cancelled booking.

### Dummy data quality

-   Use one source of truth for drivers, vehicles, rides, bookings and
    offers.
-   Link records using IDs.
-   Ensure ride seat counts and booking seat counts are consistent.
-   Use formatted currency helper functions.
-   Provide a "Reset demo data" option.
-   Include enough records to test filters, empty states and status
    variations.

------------------------------------------------------------------------

## 8. Interaction requirements

Every visible control must do something meaningful in the prototype.

Examples: - Search button filters local rides. - Date and seat selectors
update the search. - Filter chips change visible results. - Ride card
opens the correct detail screen. - Request button creates a local
booking. - Counter-offer actions update local offer history and
status. - Driver accept/reject updates booking and seat inventory. -
Publish ride adds a ride to local data. - Chat send adds a message. -
Cancel action changes local booking status. - Mode switch changes
driver/passenger home experience. - Notification tap opens the relevant
screen. - Reset demo data restores the original sample state.

Do not leave decorative buttons that appear functional but do nothing.
If a feature is intentionally not implemented, disable it and label it
as coming later.

------------------------------------------------------------------------

## 9. Flutter architecture and implementation guidance

Use a maintainable structure without overengineering a prototype.

Suggested structure:

``` text
lib/
  main.dart
  app/
    app.dart
    router.dart
    theme/
      app_colors.dart
      app_theme.dart
      app_text_styles.dart
      app_spacing.dart
  core/
    constants/
    extensions/
    formatters/
    widgets/
      app_button.dart
      app_text_field.dart
      app_card.dart
      status_chip.dart
      empty_state.dart
      loading_view.dart
      error_view.dart
  data/
    mock/
      mock_data.dart
      mock_data_factory.dart
    repositories/
      ride_repository.dart
      booking_repository.dart
      user_repository.dart
      message_repository.dart
  models/
    user.dart
    driver.dart
    vehicle.dart
    ride.dart
    ride_stop.dart
    booking.dart
    counter_offer.dart
    message.dart
    notification.dart
    review.dart
  features/
    onboarding/
    explore/
    ride_search/
    ride_details/
    booking/
    negotiation/
    my_trips/
    driver_dashboard/
    publish_ride/
    requests/
    inbox/
    notifications/
    profile/
    safety/
```

### State management

Choose one state-management approach and use it consistently (for
example, Riverpod, Provider or Bloc). For a prototype, prefer the
simplest option that supports shared local state cleanly.

Keep UI widgets separate from mock data and business logic. Use
repository interfaces so a future API repository can replace the mock
repository.

Example conceptual interfaces:

``` dart
abstract class RideRepository {
  Future<List<Ride>> searchRides(RideSearchQuery query);
  Future<Ride?> getRideById(String rideId);
  Future<Ride> publishRide(CreateRideRequest request);
}

abstract class BookingRepository {
  Future<Booking> createBooking(CreateBookingRequest request);
  Future<Booking> acceptRequest(String bookingId);
  Future<Booking> rejectRequest(String bookingId);
  Future<List<CounterOffer>> getOffers(String bookingId);
  Future<CounterOffer> createCounterOffer(
    String bookingId,
    double pricePerSeat,
  );
  Future<Booking> respondToOffer(String offerId, OfferAction action);
}
```

These are local mock implementations for now. Do not create HTTP clients
or API URLs in this phase.

### Models

Use typed Dart models, enums for statuses and predictable serialization
where useful. Avoid passing loosely structured `Map<String, dynamic>`
throughout the UI.

Recommended model concepts: - `AppUser` - `DriverProfile` - `Vehicle` -
`Ride` - `RideStop` - `RidePreference` - `Booking` - `CounterOffer` -
`ChatConversation` - `ChatMessage` - `AppNotification` - `Review`

Use `DateTime` for dates and times, and `double` only for display
calculations if needed. For production financial calculations, use a
decimal-safe approach; for the prototype, keep prices as integer PKR
values to avoid floating-point rounding issues.

------------------------------------------------------------------------

## 10. Reusable UI components

Create reusable components for:

-   Primary, secondary and text buttons.
-   Search location input.
-   Date picker field.
-   Seat quantity selector.
-   Fare/price display.
-   Driver mini profile.
-   Ride result card.
-   Route timeline.
-   Vehicle information card.
-   Rating and verification indicator.
-   Booking status chip.
-   Counter-offer timeline item.
-   Offer composer / amount input.
-   Filter bottom sheet.
-   Confirmation dialog.
-   Empty state.
-   Error state and retry action.
-   Loading skeleton.
-   Notification list item.
-   Chat message bubble.
-   Form section header.

Keep component APIs simple and consistent. Avoid duplicating slightly
different versions of the same component across features.

------------------------------------------------------------------------

## 11. UX quality checklist

Before calling the prototype complete, verify:

-   A first-time user can identify how to find a ride without
    explanation.
-   Primary action is visually obvious on each screen.
-   Search form is short and easy to complete.
-   Price per seat and total price are never ambiguous.
-   Route origin, destination and stops are easy to understand.
-   Driver trust signals are visible but not overwhelming.
-   Counter-offer timeline makes it obvious whose turn it is.
-   Current pending offer is visually distinct.
-   Accepted fare is clearly labeled as the agreed fare.
-   Forms show inline validation and preserve entered values after
    errors.
-   Empty states explain what happened and what the user can do next.
-   Loading and error states are designed, not blank screens.
-   Back navigation behaves predictably.
-   Keyboard does not hide important form actions.
-   Layout works on small and large phones.
-   Text scales without overflow.
-   Contrast and touch targets are accessible.
-   No button is fake or non-functional.
-   Demo data can be reset.

------------------------------------------------------------------------

## 12. Deliverables

The AI coding agent must provide:

1.  Complete Flutter source code.
2.  Clear project structure.
3.  Centralized theme and reusable components.
4.  Typed models and enums.
5.  Local mock repositories and realistic dummy data.
6.  Working navigation.
7.  Working passenger and driver demo journeys.
8.  Working counter-offer negotiation simulation.
9.  Working local ride publishing and booking state changes.
10. A README with setup/run instructions.
11. A short list of packages used and why.
12. A list of features intentionally deferred to API/backend
    integration.
13. Screenshots of the main screens if the environment supports emulator
    screenshots.
14. A test checklist and known limitations.

------------------------------------------------------------------------

## 13. Definition of done

The prototype is complete only when:

-   It launches successfully on an Android emulator or device.
-   All core screens are reachable through navigation.
-   Passenger can search, inspect a ride and create a booking using
    dummy data.
-   Passenger and driver can complete a counter-offer exchange.
-   Driver can publish a ride and manage requests locally.
-   Booking status and available seats update consistently.
-   Inbox, notifications and profile screens display coherent sample
    data.
-   Empty, error, validation and confirmation states are present.
-   No real API/backend dependency is required.
-   The codebase is organized so API repositories can be added later.
-   The README explains how to run and reset the demo.

------------------------------------------------------------------------

## 14. Final instruction to the AI coding agent

First inspect the Flutter project and report its current structure and
dependencies. If the project is empty, initialize a clean Flutter app.
Then propose the screen flow, design tokens, architecture and
implementation milestones.

Build the app as a polished, cohesive product---not as disconnected
sample screens. Start with the Explore screen and the core passenger
journey, then implement the driver journey, counter-offer flow and
remaining screens. Use dummy data only. Run the app, test every
interaction, fix layout overflow and navigation issues, and provide a
completion report.

**Do not connect APIs yet. Do not invent backend endpoints. Do not leave
important controls non-functional. Focus on an amazing, modern,
accessible and genuinely user-friendly Flutter prototype.**
