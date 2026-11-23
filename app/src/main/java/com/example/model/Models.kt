package com.example.model

import androidx.compose.runtime.Immutable

enum class AppRole {
    PASSENGER,
    DRIVER,
}

enum class VerificationLevel(val label: String) {
    BASIC("Not Verified"),
    VERIFIED_PASSENGER("Verified Passenger"),
    VERIFIED_DRIVER("Verified Driver"),
}

enum class VerificationStatus(val label: String) {
    NOT_STARTED("Not started"),
    PENDING("Pending review"),
    APPROVED("Approved"),
    REJECTED("Action needed"),
}

enum class RideAudience(val label: String) {
    ALL_USERS("All (Verified + Basic)"),
    VERIFIED_ONLY("Verified Only"),
}

enum class LuggageCapacity(val label: String) {
    LIGHT("Backpack / Small bag"),
    MEDIUM("1 Cabin suitcase"),
    LARGE("2+ Suitcases"),
}

enum class BookingStatus(val label: String) {
    PENDING("Pending"),
    NEGOTIATING("Negotiating"),
    CONFIRMED("Confirmed"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    REJECTED("Declined"),
}

enum class OfferActor(val label: String) {
    DRIVER("Driver"),
    PASSENGER("Passenger"),
}

enum class CounterOfferStatus(val label: String) {
    COUNTERED("Countered"),
    PENDING("Pending response"),
    ACCEPTED("Accepted"),
    REJECTED("Declined"),
    EXPIRED("Expired"),
}

enum class OfferAction {
    ACCEPT,
    REJECT,
}

enum class NotificationType {
    BOOKING_REQUEST,
    COUNTER_OFFER,
    OFFER_ACCEPTED,
    RIDE_REMINDER,
    BOOKING_CANCELLED,
    REVIEW_REQUEST,
    VERIFICATION_UPDATE,
    SAFETY_ALERT,
}

enum class RideSortOption(val label: String) {
    RECOMMENDED("Recommended"),
    EARLIEST("Earliest departure"),
    LOWEST_PRICE("Lowest price"),
}

enum class DepartureWindow(val label: String) {
    ANY("Any time"),
    MORNING("Morning (6am - 12pm)"),
    AFTERNOON("Afternoon (12pm - 5pm)"),
    EVENING("Evening (5pm+)"),
}

enum class ComplaintStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
}

@Immutable
data class SavedPlace(
    val id: String,
    val label: String,
    val city: String,
    val area: String,
)

@Immutable
data class UserPreferences(
    val acPreferred: Boolean = true,
    val quietRide: Boolean = false,
    val chattyPreferred: Boolean = true,
    val petsAllowed: Boolean = false,
    val smokingAllowed: Boolean = false,
    val womenOnlyPreferred: Boolean = false,
    val notificationsEnabled: Boolean = true,
)

@Immutable
data class AppUser(
    val id: String,
    val name: String,
    val phone: String,
    val homeCity: String,
    val initials: String,
    val dob: String = "1998-05-14",
    val gender: String = "Male",
    val bio: String = "Regular Karachi–Hyderabad commuter. Respectful, punctual, and loves chai stops.",
    val emergencyContactName: String = "Tariq Raza",
    val emergencyContactPhone: String = "+92 321 5550101",
    val rating: Double,
    val reviewCount: Int,
    val isVerified: Boolean,
    val verificationLevel: VerificationLevel = VerificationLevel.VERIFIED_DRIVER,
    val verificationStatus: VerificationStatus = VerificationStatus.APPROVED,
    val verificationRejectionReason: String? = null,
    val cnicVerified: Boolean = true,
    val licenceVerified: Boolean = true,
    val vehicleVerified: Boolean = true,
    val previewAsBasic: Boolean = false,
    val language: String = "English",
    val autoShareLiveRide: Boolean = true,
    val memberSince: String,
    val savedPlaces: List<SavedPlace>,
    val preferences: UserPreferences,
    val activeRole: AppRole = AppRole.PASSENGER,
) {
    val effectiveVerificationLevel: VerificationLevel
        get() = if (previewAsBasic) VerificationLevel.BASIC else verificationLevel

    val isEffectivelyVerified: Boolean
        get() = effectiveVerificationLevel != VerificationLevel.BASIC
}

@Immutable
data class DriverProfile(
    val id: String,
    val name: String,
    val initials: String,
    val avatarColorHex: Long,
    val rating: Double,
    val reviewCount: Int,
    val isVerified: Boolean,
    val verificationLevel: VerificationLevel = if (isVerified) VerificationLevel.VERIFIED_DRIVER else VerificationLevel.BASIC,
    val cnicVerified: Boolean = isVerified,
    val licenceVerified: Boolean = isVerified,
    val vehicleVerified: Boolean = isVerified,
    val reliabilityScore: Int = 97,
    val vehicleId: String,
    val bio: String,
    val tripsCompleted: Int,
    val memberSince: String,
    val responseRate: String,
)

@Immutable
data class Vehicle(
    val id: String,
    val driverId: String,
    val makeModel: String,
    val year: Int,
    val color: String,
    val plateNumber: String,
    val seatCapacity: Int,
    val hasAc: Boolean = true,
    val luggageCapacity: LuggageCapacity = LuggageCapacity.MEDIUM,
    val isActive: Boolean = false,
)

@Immutable
data class RideStop(
    val city: String,
    val area: String,
    val timeLabel: String,
)

@Immutable
data class RidePreferences(
    val acAvailable: Boolean = true,
    val petsAllowed: Boolean = false,
    val smokingAllowed: Boolean = false,
    val musicAllowed: Boolean = true,
    val chattyFriendly: Boolean = true,
    val womenOnly: Boolean = false,
    val luggageCapacity: LuggageCapacity = LuggageCapacity.MEDIUM,
)

@Immutable
data class Ride(
    val id: String,
    val driverId: String,
    val vehicleId: String,
    val originCity: String,
    val originArea: String,
    val destinationCity: String,
    val destinationArea: String,
    val stops: List<RideStop>,
    val dateDayOffset: Int,
    val departureDateLabel: String,
    val departureTimeLabel: String,
    val departureMinutesOfDay: Int,
    val durationMinutes: Int,
    val availableSeats: Int,
    val totalSeats: Int,
    val pricePerSeatPkr: Int,
    val preferences: RidePreferences,
    val audience: RideAudience = if (preferences.womenOnly) RideAudience.VERIFIED_ONLY else RideAudience.ALL_USERS,
    val notes: String,
    val routeMatchLabel: String,
    val isDriverOwned: Boolean = false,
)

@Immutable
data class CounterOffer(
    val id: String,
    val sequence: Int,
    val actor: OfferActor,
    val actorName: String,
    val amountPerSeatPkr: Int,
    val timestampLabel: String,
    val note: String,
    val status: CounterOfferStatus,
)

@Immutable
data class Booking(
    val id: String,
    val rideId: String,
    val passengerId: String,
    val passengerName: String,
    val passengerInitials: String,
    val passengerRating: Double,
    val passengerVerified: Boolean = true,
    val seatsRequested: Int,
    val listedFarePerSeatPkr: Int,
    val agreedFarePerSeatPkr: Int?,
    val note: String,
    val pickupPoint: String,
    val dropoffPoint: String,
    val status: BookingStatus,
    val createdAtLabel: String,
    val offerExpiryLabel: String,
    val offers: List<CounterOffer>,
    val isIncomingDriverRequest: Boolean = false,
    val rideStartOtp: String = "1234",
    val isPaid: Boolean = false,
    val paymentMethod: String? = null,
    val isRated: Boolean = false,
)

@Immutable
data class ChatMessage(
    val id: String,
    val senderName: String,
    val isFromMe: Boolean,
    val text: String,
    val timestampLabel: String,
)

@Immutable
data class ChatConversation(
    val id: String,
    val counterpartId: String,
    val counterpartName: String,
    val counterpartInitials: String,
    val counterpartColorHex: Long,
    val counterpartVerified: Boolean,
    val counterpartRole: String,
    val rideContext: String,
    val bookingId: String?,
    val unreadCount: Int,
    val lastMessageTime: String,
    val messages: List<ChatMessage>,
)

@Immutable
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestampLabel: String,
    val isUnread: Boolean,
    val targetBookingId: String? = null,
    val targetRideId: String? = null,
)

@Immutable
data class Review(
    val id: String,
    val driverId: String,
    val authorName: String,
    val rating: Double,
    val comment: String,
    val dateLabel: String,
)

@Immutable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String,
)

@Immutable
data class TrustedContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String,
)

@Immutable
data class BlockedUser(
    val id: String,
    val userId: String,
    val name: String,
    val initials: String,
    val reason: String,
    val blockedAtLabel: String,
)

@Immutable
data class LostFoundItem(
    val id: String,
    val rideId: String,
    val rideSummary: String,
    val itemTitle: String,
    val description: String,
    val statusLabel: String,
    val reportedAtLabel: String,
)

@Immutable
data class SupportComplaint(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: ComplaintStatus,
    val createdAtLabel: String,
)

@Immutable
data class FavouriteRoute(
    val id: String,
    val originCity: String,
    val destinationCity: String,
    val label: String,
)

@Immutable
data class WaitlistEntry(
    val id: String,
    val rideId: String,
    val seatsWanted: Int,
    val joinedAtLabel: String,
)

@Immutable
data class PaymentTransaction(
    val id: String,
    val bookingId: String,
    val routeSummary: String,
    val amountPkr: Int,
    val discountPkr: Int,
    val promoCode: String?,
    val method: String,
    val statusLabel: String,
    val dateLabel: String,
)

@Immutable
data class DriverEarningEntry(
    val id: String,
    val rideSummary: String,
    val passengerName: String,
    val seats: Int,
    val amountPkr: Int,
    val dateLabel: String,
)

@Immutable
data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
)

@Immutable
data class RideSearchQuery(
    val originCity: String = "",
    val destinationCity: String = "",
    val dateDayOffset: Int? = null,
    val minSeats: Int = 1,
    val maxPricePkr: Int? = null,
    val verifiedOnly: Boolean = false,
    val departureWindow: DepartureWindow = DepartureWindow.ANY,
    val acRequired: Boolean = false,
    val noSmokingOnly: Boolean = false,
    val petsAllowedOnly: Boolean = false,
    val womenOnly: Boolean = false,
    val sortOption: RideSortOption = RideSortOption.RECOMMENDED,
)

data class CreateRideRequest(
    val originCity: String,
    val originArea: String,
    val destinationCity: String,
    val destinationArea: String,
    val intermediateStops: List<RideStop>,
    val dateDayOffset: Int,
    val departureDateLabel: String,
    val departureTimeLabel: String,
    val departureMinutesOfDay: Int,
    val durationMinutes: Int,
    val vehicleId: String,
    val availableSeats: Int,
    val pricePerSeatPkr: Int,
    val preferences: RidePreferences,
    val audience: RideAudience,
    val notes: String,
)

data class CreateBookingRequest(
    val rideId: String,
    val seatsRequested: Int,
    val proposedFarePerSeatPkr: Int?,
    val pickupPoint: String,
    val dropoffPoint: String,
    val note: String,
)
