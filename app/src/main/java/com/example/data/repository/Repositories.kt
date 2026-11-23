package com.example.data.repository

import com.example.model.*
import kotlinx.coroutines.flow.StateFlow

interface RideRepository {
    val ridesFlow: StateFlow<List<Ride>>
    fun searchRides(query: RideSearchQuery): List<Ride>
    fun getRideById(rideId: String): Ride?
    suspend fun publishRide(request: CreateRideRequest): Result<Ride>
}

interface BookingRepository {
    val bookingsFlow: StateFlow<List<Booking>>
    fun getBookingById(bookingId: String): Booking?
    suspend fun createBooking(request: CreateBookingRequest): Result<Booking>
    suspend fun acceptDriverRequest(bookingId: String): Result<Booking>
    suspend fun rejectDriverRequest(bookingId: String): Result<Booking>
    suspend fun submitCounterOffer(
        bookingId: String,
        actor: OfferActor,
        amountPerSeatPkr: Int,
        note: String,
    ): Result<Booking>
    suspend fun respondToPendingOffer(bookingId: String, action: OfferAction): Result<Booking>
    suspend fun cancelBooking(bookingId: String): Result<Booking>
}

interface UserRepository {
    val userFlow: StateFlow<AppUser>
    val driversFlow: StateFlow<List<DriverProfile>>
    val vehiclesFlow: StateFlow<List<Vehicle>>
    val reviewsFlow: StateFlow<List<Review>>
    fun getDriverById(driverId: String): DriverProfile?
    fun getVehicleById(vehicleId: String): Vehicle?
    fun getReviewsForDriver(driverId: String): List<Review>
    fun switchRole(role: AppRole)
    fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
    fun addVehicle(
        makeModel: String,
        year: Int,
        color: String,
        plateNumber: String,
        seatCapacity: Int,
        luggageCapacity: LuggageCapacity,
    ): Vehicle
    fun addSavedPlace(label: String, city: String, area: String)
}

interface InboxRepository {
    val conversationsFlow: StateFlow<List<ChatConversation>>
    val notificationsFlow: StateFlow<List<AppNotification>>
    fun sendMessage(conversationId: String, text: String)
    fun markConversationRead(conversationId: String)
    fun markNotificationRead(notificationId: String)
    fun openOrCreateConversationForBooking(bookingId: String): String?
}
