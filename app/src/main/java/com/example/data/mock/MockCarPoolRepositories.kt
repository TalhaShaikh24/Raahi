package com.example.data.mock

import com.example.core.Formatters
import com.example.data.repository.BookingRepository
import com.example.data.repository.InboxRepository
import com.example.data.repository.RideRepository
import com.example.data.repository.UserRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

object MockCarPoolStore : RideRepository, BookingRepository, UserRepository, InboxRepository {

    private val idCounter = AtomicInteger(4000)

    private val _userFlow = MutableStateFlow(MockDataFactory.initialUser())
    override val userFlow: StateFlow<AppUser> = _userFlow.asStateFlow()

    private val _driversFlow = MutableStateFlow(MockDataFactory.initialDrivers())
    override val driversFlow: StateFlow<List<DriverProfile>> = _driversFlow.asStateFlow()

    private val _vehiclesFlow = MutableStateFlow(MockDataFactory.initialVehicles())
    override val vehiclesFlow: StateFlow<List<Vehicle>> = _vehiclesFlow.asStateFlow()

    private val _ridesFlow = MutableStateFlow(MockDataFactory.initialRides())
    override val ridesFlow: StateFlow<List<Ride>> = _ridesFlow.asStateFlow()

    private val _bookingsFlow = MutableStateFlow(MockDataFactory.initialBookings())
    override val bookingsFlow: StateFlow<List<Booking>> = _bookingsFlow.asStateFlow()

    private val _conversationsFlow = MutableStateFlow(MockDataFactory.initialConversations())
    override val conversationsFlow: StateFlow<List<ChatConversation>> = _conversationsFlow.asStateFlow()

    private val _notificationsFlow = MutableStateFlow(MockDataFactory.initialNotifications())
    override val notificationsFlow: StateFlow<List<AppNotification>> = _notificationsFlow.asStateFlow()

    private val _reviewsFlow = MutableStateFlow(MockDataFactory.initialReviews())
    override val reviewsFlow: StateFlow<List<Review>> = _reviewsFlow.asStateFlow()

    // Extended prototype state flows
    private val _followedDriverIds = MutableStateFlow(setOf("D-001", "D-004"))
    val followedDriverIds: StateFlow<Set<String>> = _followedDriverIds.asStateFlow()

    private val _favouriteRoutes = MutableStateFlow(MockDataFactory.initialFavouriteRoutes())
    val favouriteRoutes: StateFlow<List<FavouriteRoute>> = _favouriteRoutes.asStateFlow()

    private val _waitlist = MutableStateFlow(MockDataFactory.initialWaitlist())
    val waitlist: StateFlow<List<WaitlistEntry>> = _waitlist.asStateFlow()

    private val _emergencyContacts = MutableStateFlow(MockDataFactory.initialEmergencyContacts())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private val _trustedContacts = MutableStateFlow(MockDataFactory.initialTrustedContacts())
    val trustedContacts: StateFlow<List<TrustedContact>> = _trustedContacts.asStateFlow()

    private val _blockedUsers = MutableStateFlow(MockDataFactory.initialBlockedUsers())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers.asStateFlow()

    private val _lostFoundItems = MutableStateFlow(MockDataFactory.initialLostFoundItems())
    val lostFoundItems: StateFlow<List<LostFoundItem>> = _lostFoundItems.asStateFlow()

    private val _complaints = MutableStateFlow(MockDataFactory.initialComplaints())
    val complaints: StateFlow<List<SupportComplaint>> = _complaints.asStateFlow()

    private val _payments = MutableStateFlow(MockDataFactory.initialPayments())
    val payments: StateFlow<List<PaymentTransaction>> = _payments.asStateFlow()

    private val _driverEarnings = MutableStateFlow(MockDataFactory.initialDriverEarnings())
    val driverEarnings: StateFlow<List<DriverEarningEntry>> = _driverEarnings.asStateFlow()

    fun resetDemoData() {
        idCounter.set(4000)
        _userFlow.value = MockDataFactory.initialUser()
        _driversFlow.value = MockDataFactory.initialDrivers()
        _vehiclesFlow.value = MockDataFactory.initialVehicles()
        _ridesFlow.value = MockDataFactory.initialRides()
        _bookingsFlow.value = MockDataFactory.initialBookings()
        _conversationsFlow.value = MockDataFactory.initialConversations()
        _notificationsFlow.value = MockDataFactory.initialNotifications()
        _reviewsFlow.value = MockDataFactory.initialReviews()
        _followedDriverIds.value = setOf("D-001", "D-004")
        _favouriteRoutes.value = MockDataFactory.initialFavouriteRoutes()
        _waitlist.value = MockDataFactory.initialWaitlist()
        _emergencyContacts.value = MockDataFactory.initialEmergencyContacts()
        _trustedContacts.value = MockDataFactory.initialTrustedContacts()
        _blockedUsers.value = MockDataFactory.initialBlockedUsers()
        _lostFoundItems.value = MockDataFactory.initialLostFoundItems()
        _complaints.value = MockDataFactory.initialComplaints()
        _payments.value = MockDataFactory.initialPayments()
        _driverEarnings.value = MockDataFactory.initialDriverEarnings()
    }

    override fun searchRides(query: RideSearchQuery): List<Ride> {
        val driversById = _driversFlow.value.associateBy { it.id }
        val blockedIds = _blockedUsers.value.map { it.userId }.toSet()

        val filtered = _ridesFlow.value.filter { ride ->
            if (ride.driverId in blockedIds) return@filter false

            val originMatches = query.originCity.isBlank() ||
                ride.originCity.contains(query.originCity, ignoreCase = true) ||
                ride.originArea.contains(query.originCity, ignoreCase = true) ||
                ride.stops.any { it.city.contains(query.originCity, ignoreCase = true) || it.area.contains(query.originCity, ignoreCase = true) }

            val destMatches = query.destinationCity.isBlank() ||
                ride.destinationCity.contains(query.destinationCity, ignoreCase = true) ||
                ride.destinationArea.contains(query.destinationCity, ignoreCase = true) ||
                ride.stops.any { it.city.contains(query.destinationCity, ignoreCase = true) || it.area.contains(query.destinationCity, ignoreCase = true) }

            val dateMatches = query.dateDayOffset == null || ride.dateDayOffset == query.dateDayOffset
            // Allow showing full rides (availableSeats == 0) when minSeats == 1 so users can test Waitlist!
            val seatsMatch = ride.availableSeats >= query.minSeats || (query.minSeats == 1 && ride.availableSeats == 0)
            val priceMatch = query.maxPricePkr == null || ride.pricePerSeatPkr <= query.maxPricePkr
            val verifiedMatch = !query.verifiedOnly || (driversById[ride.driverId]?.isVerified == true)
            val acMatch = !query.acRequired || ride.preferences.acAvailable
            val noSmokingMatch = !query.noSmokingOnly || !ride.preferences.smokingAllowed
            val petsMatch = !query.petsAllowedOnly || ride.preferences.petsAllowed
            val womenOnlyMatch = !query.womenOnly || ride.preferences.womenOnly
            val windowMatch = when (query.departureWindow) {
                DepartureWindow.ANY -> true
                DepartureWindow.MORNING -> ride.departureMinutesOfDay in (5 * 60)..(11 * 60 + 59)
                DepartureWindow.AFTERNOON -> ride.departureMinutesOfDay in (12 * 60)..(16 * 60 + 59)
                DepartureWindow.EVENING -> ride.departureMinutesOfDay >= 17 * 60
            }

            originMatches && destMatches && dateMatches && seatsMatch && priceMatch &&
                verifiedMatch && acMatch && noSmokingMatch && petsMatch && womenOnlyMatch && windowMatch
        }

        // Acceptance criterion A/F4: Verified drivers ALWAYS listed first, then by selected sort!
        return when (query.sortOption) {
            RideSortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<Ride> { driversById[it.driverId]?.isVerified == true }
                    .thenBy { it.dateDayOffset }
                    .thenByDescending { driversById[it.driverId]?.rating ?: 0.0 }
                    .thenBy { it.pricePerSeatPkr }
            )
            RideSortOption.EARLIEST -> filtered.sortedWith(
                compareByDescending<Ride> { driversById[it.driverId]?.isVerified == true }
                    .thenBy { it.dateDayOffset }
                    .thenBy { it.departureMinutesOfDay }
            )
            RideSortOption.LOWEST_PRICE -> filtered.sortedWith(
                compareByDescending<Ride> { driversById[it.driverId]?.isVerified == true }
                    .thenBy { it.pricePerSeatPkr }
            )
        }
    }

    override fun getRideById(rideId: String): Ride? = _ridesFlow.value.find { it.id == rideId }

    override suspend fun publishRide(request: CreateRideRequest): Result<Ride> {
        val vehicle = getVehicleById(request.vehicleId)
            ?: return Result.failure(IllegalArgumentException("Please select a valid vehicle."))
        if (request.originCity.equals(request.destinationCity, ignoreCase = true) &&
            request.originArea.equals(request.destinationArea, ignoreCase = true)
        ) {
            return Result.failure(IllegalArgumentException("Origin and destination must be different."))
        }
        if (request.availableSeats !in 1..vehicle.seatCapacity) {
            return Result.failure(IllegalArgumentException("Seats must be between 1 and ${vehicle.seatCapacity} for ${vehicle.makeModel}."))
        }
        if (request.pricePerSeatPkr <= 0) {
            return Result.failure(IllegalArgumentException("Fare per seat must be greater than PKR 0."))
        }

        // Enforce: Female-Only ON = Audience forced to Verified Only
        val effectiveAudience = if (request.preferences.womenOnly) {
            RideAudience.VERIFIED_ONLY
        } else {
            request.audience
        }

        val allStops = buildList {
            add(RideStop(request.originCity, request.originArea, request.departureTimeLabel))
            addAll(request.intermediateStops)
            add(RideStop(request.destinationCity, request.destinationArea, "Arrival"))
        }

        val newRide = Ride(
            id = "R-${idCounter.incrementAndGet()}",
            driverId = "D-ME",
            vehicleId = vehicle.id,
            originCity = request.originCity,
            originArea = request.originArea,
            destinationCity = request.destinationCity,
            destinationArea = request.destinationArea,
            stops = allStops,
            dateDayOffset = request.dateDayOffset,
            departureDateLabel = request.departureDateLabel,
            departureTimeLabel = request.departureTimeLabel,
            departureMinutesOfDay = request.departureMinutesOfDay,
            durationMinutes = request.durationMinutes,
            availableSeats = request.availableSeats,
            totalSeats = request.availableSeats,
            pricePerSeatPkr = request.pricePerSeatPkr,
            preferences = request.preferences,
            audience = effectiveAudience,
            notes = request.notes.ifBlank { "Published in Driver Mode. Flexible pickup near main road." },
            routeMatchLabel = "Just published",
            isDriverOwned = true,
        )

        _ridesFlow.update { listOf(newRide) + it }
        pushNotification(
            type = NotificationType.RIDE_REMINDER,
            title = "Ride published",
            body = "${newRide.originCity} → ${newRide.destinationCity} (${effectiveAudience.label}) is now live.",
            targetRideId = newRide.id,
        )
        return Result.success(newRide)
    }

    override fun getBookingById(bookingId: String): Booking? =
        _bookingsFlow.value.find { it.id == bookingId }

    override suspend fun createBooking(request: CreateBookingRequest): Result<Booking> {
        val ride = getRideById(request.rideId)
            ?: return Result.failure(IllegalArgumentException("Ride could not be found."))
        val user = _userFlow.value
        if (ride.audience == RideAudience.VERIFIED_ONLY && !user.isEffectivelyVerified) {
            return Result.failure(IllegalStateException("Verify your account to book this ride."))
        }
        if (request.seatsRequested !in 1..ride.availableSeats) {
            return Result.failure(IllegalArgumentException("Only ${ride.availableSeats} seat(s) available on this ride."))
        }
        val proposed = request.proposedFarePerSeatPkr
        if (proposed != null && proposed <= 0) {
            return Result.failure(IllegalArgumentException("Counter-offer amount must be greater than PKR 0."))
        }

        val driver = getDriverById(ride.driverId)
        val isCounter = proposed != null && proposed != ride.pricePerSeatPkr
        val bookingId = "B-${idCounter.incrementAndGet()}"

        val offers = if (isCounter) {
            listOf(
                CounterOffer("OF-${idCounter.incrementAndGet()}", 1, OfferActor.DRIVER, driver?.name ?: "Driver", ride.pricePerSeatPkr, "Just now", "Listed ride fare per seat", CounterOfferStatus.COUNTERED),
                CounterOffer("OF-${idCounter.incrementAndGet()}", 2, OfferActor.PASSENGER, "${user.name} (You)", proposed!!, "Just now", request.note.ifBlank { "Passenger counter-offer for ${Formatters.formatSeats(request.seatsRequested)}" }, CounterOfferStatus.PENDING),
            )
        } else {
            listOf(
                CounterOffer("OF-${idCounter.incrementAndGet()}", 1, OfferActor.PASSENGER, "${user.name} (You)", ride.pricePerSeatPkr, "Just now", request.note.ifBlank { "Requested at listed fare" }, CounterOfferStatus.PENDING),
            )
        }

        val newBooking = Booking(
            id = bookingId,
            rideId = ride.id,
            passengerId = user.id,
            passengerName = user.name,
            passengerInitials = user.initials,
            passengerRating = user.rating,
            passengerVerified = user.isEffectivelyVerified,
            seatsRequested = request.seatsRequested,
            listedFarePerSeatPkr = ride.pricePerSeatPkr,
            agreedFarePerSeatPkr = null,
            note = request.note,
            pickupPoint = request.pickupPoint,
            dropoffPoint = request.dropoffPoint,
            status = if (isCounter) BookingStatus.NEGOTIATING else BookingStatus.PENDING,
            createdAtLabel = "Just now",
            offerExpiryLabel = if (isCounter) "Expires in 12 min" else "Awaiting driver",
            offers = offers,
            rideStartOtp = "1234",
        )

        _bookingsFlow.update { listOf(newBooking) + it }
        pushNotification(
            type = if (isCounter) NotificationType.COUNTER_OFFER else NotificationType.BOOKING_REQUEST,
            title = if (isCounter) "Counter-offer sent" else "Booking request sent",
            body = "${ride.originCity} → ${ride.destinationCity} (${Formatters.formatPkr(proposed ?: ride.pricePerSeatPkr)}/seat).",
            targetBookingId = newBooking.id,
        )
        return Result.success(newBooking)
    }

    override suspend fun acceptDriverRequest(bookingId: String): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val ride = getRideById(booking.rideId)
            ?: return Result.failure(IllegalArgumentException("Ride not found."))
        if (ride.availableSeats < booking.seatsRequested) {
            return Result.failure(IllegalArgumentException("Not enough seats left (${ride.availableSeats} available)."))
        }

        val pendingAmount = booking.offers.lastOrNull()?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr
        val updatedOffers = booking.offers.mapIndexed { idx, offer ->
            if (idx == booking.offers.lastIndex && offer.status == CounterOfferStatus.PENDING) offer.copy(status = CounterOfferStatus.ACCEPTED) else offer
        }
        val updatedBooking = booking.copy(
            status = BookingStatus.CONFIRMED,
            agreedFarePerSeatPkr = pendingAmount,
            offerExpiryLabel = "Confirmed • OTP ${booking.rideStartOtp}",
            offers = updatedOffers,
        )
        updateBookingAndSeats(updatedBooking, seatDelta = -booking.seatsRequested)
        pushNotification(
            type = NotificationType.OFFER_ACCEPTED,
            title = "Request accepted",
            body = "${booking.passengerName} confirmed for ${Formatters.formatSeats(booking.seatsRequested)} at ${Formatters.formatPkr(pendingAmount)}/seat.",
            targetBookingId = updatedBooking.id,
        )
        return Result.success(updatedBooking)
    }

    override suspend fun rejectDriverRequest(bookingId: String): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val updatedOffers = booking.offers.mapIndexed { idx, offer ->
            if (idx == booking.offers.lastIndex && offer.status == CounterOfferStatus.PENDING) offer.copy(status = CounterOfferStatus.REJECTED) else offer
        }
        val updatedBooking = booking.copy(status = BookingStatus.REJECTED, offerExpiryLabel = "Declined", offers = updatedOffers)
        updateBookingOnly(updatedBooking)
        pushNotification(
            type = NotificationType.BOOKING_CANCELLED,
            title = "Request declined",
            body = "${booking.passengerName}'s request for ${Formatters.formatSeats(booking.seatsRequested)} was declined.",
            targetBookingId = updatedBooking.id,
        )
        return Result.success(updatedBooking)
    }

    override suspend fun submitCounterOffer(
        bookingId: String,
        actor: OfferActor,
        amountPerSeatPkr: Int,
        note: String,
    ): Result<Booking> {
        if (amountPerSeatPkr <= 0) {
            return Result.failure(IllegalArgumentException("Please enter a valid PKR amount above 0."))
        }
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val ride = getRideById(booking.rideId)
        val driver = ride?.let { getDriverById(it.driverId) }

        val actorDisplayName = when (actor) {
            OfferActor.PASSENGER -> if (booking.isIncomingDriverRequest) booking.passengerName else "${_userFlow.value.name} (You)"
            OfferActor.DRIVER -> if (booking.isIncomingDriverRequest) "${_userFlow.value.name} (You)" else (driver?.name ?: "Driver")
        }

        val archivedOffers = booking.offers.map { offer ->
            if (offer.status == CounterOfferStatus.PENDING) offer.copy(status = CounterOfferStatus.COUNTERED) else offer
        }
        val nextOffer = CounterOffer(
            id = "OF-${idCounter.incrementAndGet()}",
            sequence = archivedOffers.size + 1,
            actor = actor,
            actorName = actorDisplayName,
            amountPerSeatPkr = amountPerSeatPkr,
            timestampLabel = "Just now",
            note = note.ifBlank { "Proposed ${Formatters.formatPkr(amountPerSeatPkr)}/seat" },
            status = CounterOfferStatus.PENDING,
        )

        val updatedBooking = booking.copy(
            status = BookingStatus.NEGOTIATING,
            offerExpiryLabel = "Expires in 12 min",
            offers = archivedOffers + nextOffer,
        )
        updateBookingOnly(updatedBooking)
        pushNotification(
            type = NotificationType.COUNTER_OFFER,
            title = "New counter-offer (${Formatters.formatPkr(amountPerSeatPkr)})",
            body = "$actorDisplayName proposed ${Formatters.formatPkr(amountPerSeatPkr)}/seat.",
            targetBookingId = updatedBooking.id,
        )
        return Result.success(updatedBooking)
    }

    override suspend fun respondToPendingOffer(bookingId: String, action: OfferAction): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val pendingOffer = booking.offers.lastOrNull { it.status == CounterOfferStatus.PENDING }
            ?: return Result.failure(IllegalArgumentException("No active pending offer to respond to."))

        return when (action) {
            OfferAction.ACCEPT -> {
                val ride = getRideById(booking.rideId)
                    ?: return Result.failure(IllegalArgumentException("Ride not found."))
                if (ride.availableSeats < booking.seatsRequested) {
                    return Result.failure(IllegalArgumentException("Not enough seats left on this ride."))
                }
                val updatedOffers = booking.offers.map { offer ->
                    if (offer.id == pendingOffer.id) offer.copy(status = CounterOfferStatus.ACCEPTED) else offer
                }
                val updatedBooking = booking.copy(
                    status = BookingStatus.CONFIRMED,
                    agreedFarePerSeatPkr = pendingOffer.amountPerSeatPkr,
                    offerExpiryLabel = "Agreed • OTP ${booking.rideStartOtp}",
                    offers = updatedOffers,
                )
                updateBookingAndSeats(updatedBooking, seatDelta = -booking.seatsRequested)
                pushNotification(
                    type = NotificationType.OFFER_ACCEPTED,
                    title = "Offer accepted!",
                    body = "Agreed fare: ${Formatters.formatPkr(pendingOffer.amountPerSeatPkr)}/seat for ${ride.originCity} → ${ride.destinationCity}.",
                    targetBookingId = updatedBooking.id,
                )
                Result.success(updatedBooking)
            }
            OfferAction.REJECT -> {
                val updatedOffers = booking.offers.map { offer ->
                    if (offer.id == pendingOffer.id) offer.copy(status = CounterOfferStatus.REJECTED) else offer
                }
                val updatedBooking = booking.copy(status = BookingStatus.REJECTED, offerExpiryLabel = "Offer declined", offers = updatedOffers)
                updateBookingOnly(updatedBooking)
                pushNotification(
                    type = NotificationType.BOOKING_CANCELLED,
                    title = "Offer declined",
                    body = "The offer of ${Formatters.formatPkr(pendingOffer.amountPerSeatPkr)}/seat was declined.",
                    targetBookingId = updatedBooking.id,
                )
                Result.success(updatedBooking)
            }
        }
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val wasConfirmed = booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.ONGOING
        val updatedOffers = booking.offers.map { offer ->
            if (offer.status == CounterOfferStatus.PENDING) offer.copy(status = CounterOfferStatus.EXPIRED) else offer
        }
        val updated = booking.copy(status = BookingStatus.CANCELLED, offerExpiryLabel = "Cancelled", offers = updatedOffers)
        updateBookingAndSeats(updated, seatDelta = if (wasConfirmed) booking.seatsRequested else 0)
        pushNotification(
            type = NotificationType.BOOKING_CANCELLED,
            title = "Booking cancelled",
            body = "Booking ${booking.id} has been cancelled.",
            targetBookingId = updated.id,
        )
        return Result.success(updated)
    }

    fun verifyRideStartOtp(bookingId: String, enteredCode: String): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        if (enteredCode != booking.rideStartOtp && enteredCode != "1234") {
            return Result.failure(IllegalArgumentException("Invalid OTP. Demo code is 1234."))
        }
        val updated = booking.copy(
            status = BookingStatus.ONGOING,
            offerExpiryLabel = "Trip in progress",
        )
        updateBookingOnly(updated)
        pushNotification(
            type = NotificationType.RIDE_REMINDER,
            title = "Trip started",
            body = "Ride Start OTP verified. Live safety check-ins enabled.",
            targetBookingId = updated.id,
        )
        return Result.success(updated)
    }

    fun completeOngoingRide(bookingId: String): Result<Booking> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val updated = booking.copy(
            status = BookingStatus.COMPLETED,
            offerExpiryLabel = "Completed",
        )
        updateBookingOnly(updated)
        pushNotification(
            type = NotificationType.REVIEW_REQUEST,
            title = "Trip completed!",
            body = "Tap to pay or rate your experience for booking ${booking.id}.",
            targetBookingId = updated.id,
        )
        return Result.success(updated)
    }

    fun recordPayment(bookingId: String, method: String, promoCode: String?): Result<PaymentTransaction> {
        val booking = getBookingById(bookingId)
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val ride = getRideById(booking.rideId)
        val perSeat = booking.agreedFarePerSeatPkr ?: booking.listedFarePerSeatPkr
        val gross = perSeat * booking.seatsRequested
        val cleanPromo = promoCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        val discount = if (cleanPromo == "FIRST50") (gross / 2).coerceAtMost(250) else 0
        val finalAmount = (gross - discount).coerceAtLeast(0)

        val tx = PaymentTransaction(
            id = "PAY-${idCounter.incrementAndGet()}",
            bookingId = booking.id,
            routeSummary = "${ride?.originCity ?: ""} → ${ride?.destinationCity ?: ""}",
            amountPkr = finalAmount,
            discountPkr = discount,
            promoCode = if (discount > 0) cleanPromo else null,
            method = method,
            statusLabel = "Paid (Demo)",
            dateLabel = "Just now",
        )
        _payments.update { listOf(tx) + it }
        updateBookingOnly(booking.copy(isPaid = true, paymentMethod = method))
        return Result.success(tx)
    }

    fun submitReview(bookingId: String, driverId: String, rating: Double, comment: String) {
        val booking = getBookingById(bookingId)
        val rev = Review(
            id = "REV-${idCounter.incrementAndGet()}",
            driverId = driverId,
            authorName = _userFlow.value.name,
            rating = rating,
            comment = comment.ifBlank { "Great shared ride!" },
            dateLabel = "Just now",
        )
        _reviewsFlow.update { listOf(rev) + it }
        if (booking != null) {
            updateBookingOnly(booking.copy(isRated = true))
        }
    }

    private fun updateBookingOnly(updated: Booking) {
        _bookingsFlow.update { list -> list.map { if (it.id == updated.id) updated else it } }
    }

    private fun updateBookingAndSeats(updated: Booking, seatDelta: Int) {
        updateBookingOnly(updated)
        if (seatDelta != 0) {
            _ridesFlow.update { rides ->
                rides.map { ride ->
                    if (ride.id == updated.rideId) {
                        val nextSeats = (ride.availableSeats + seatDelta).coerceIn(0, ride.totalSeats)
                        ride.copy(availableSeats = nextSeats)
                    } else {
                        ride
                    }
                }
            }
        }
    }

    override fun getDriverById(driverId: String): DriverProfile? =
        _driversFlow.value.find { it.id == driverId }

    override fun getVehicleById(vehicleId: String): Vehicle? =
        _vehiclesFlow.value.find { it.id == vehicleId }

    override fun getReviewsForDriver(driverId: String): List<Review> =
        _reviewsFlow.value.filter { it.driverId == driverId }

    override fun switchRole(role: AppRole) {
        _userFlow.update { it.copy(activeRole = role) }
    }

    override fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        _userFlow.update { it.copy(preferences = transform(it.preferences)) }
    }

    fun updateUser(transform: (AppUser) -> AppUser) {
        _userFlow.update(transform)
        // Keep D-ME driver profile badges in sync with AppUser
        val updatedUser = _userFlow.value
        _driversFlow.update { drivers ->
            drivers.map { d ->
                if (d.id == "D-ME") {
                    d.copy(
                        name = updatedUser.name,
                        isVerified = updatedUser.isEffectivelyVerified,
                        verificationLevel = updatedUser.effectiveVerificationLevel,
                        cnicVerified = updatedUser.cnicVerified && !updatedUser.previewAsBasic,
                        licenceVerified = updatedUser.licenceVerified && !updatedUser.previewAsBasic,
                        vehicleVerified = updatedUser.vehicleVerified && !updatedUser.previewAsBasic,
                    )
                } else {
                    d
                }
            }
        }
    }

    fun submitVerification(isDriver: Boolean) {
        _userFlow.update {
            it.copy(
                verificationStatus = VerificationStatus.PENDING,
                verificationRejectionReason = null,
                previewAsBasic = false,
            )
        }
        pushNotification(
            type = NotificationType.VERIFICATION_UPDATE,
            title = if (isDriver) "Driver verification submitted" else "Passenger verification submitted",
            body = "Your documents are in Pending Review (demo). You can test Approved or Rejected states from Verification Status.",
        )
    }

    fun simulateVerificationOutcome(status: VerificationStatus, isDriver: Boolean) {
        _userFlow.update {
            when (status) {
                VerificationStatus.APPROVED -> it.copy(
                    isVerified = true,
                    verificationStatus = VerificationStatus.APPROVED,
                    verificationLevel = if (isDriver) VerificationLevel.VERIFIED_DRIVER else VerificationLevel.VERIFIED_PASSENGER,
                    cnicVerified = true,
                    licenceVerified = isDriver,
                    vehicleVerified = isDriver,
                    verificationRejectionReason = null,
                    previewAsBasic = false,
                )
                VerificationStatus.REJECTED -> it.copy(
                    isVerified = false,
                    verificationStatus = VerificationStatus.REJECTED,
                    verificationLevel = VerificationLevel.BASIC,
                    cnicVerified = false,
                    licenceVerified = false,
                    vehicleVerified = false,
                    verificationRejectionReason = "CNIC back photo appeared slightly blurry in glare. Please re-upload a clear photo.",
                )
                else -> it.copy(verificationStatus = status)
            }
        }
    }

    override fun addVehicle(
        makeModel: String,
        year: Int,
        color: String,
        plateNumber: String,
        seatCapacity: Int,
        luggageCapacity: LuggageCapacity,
    ): Vehicle {
        val vehicle = Vehicle(
            id = "V-${idCounter.incrementAndGet()}",
            driverId = "D-ME",
            makeModel = makeModel.trim(),
            year = year,
            color = color.trim().ifBlank { "Silver" },
            plateNumber = plateNumber.trim().uppercase().ifBlank { "KHI-24-0001" },
            seatCapacity = seatCapacity.coerceIn(1, 6),
            hasAc = true,
            luggageCapacity = luggageCapacity,
            isActive = false,
        )
        _vehiclesFlow.update { it + vehicle }
        return vehicle
    }

    fun updateVehicle(vehicle: Vehicle) {
        _vehiclesFlow.update { list -> list.map { if (it.id == vehicle.id) vehicle else it } }
    }

    fun setActiveVehicle(vehicleId: String) {
        _vehiclesFlow.update { list ->
            list.map {
                if (it.driverId == "D-ME") it.copy(isActive = it.id == vehicleId) else it
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        _vehiclesFlow.update { list -> list.filterNot { it.id == vehicleId } }
    }

    override fun addSavedPlace(label: String, city: String, area: String) {
        val place = SavedPlace("SP-${idCounter.incrementAndGet()}", label.trim().ifBlank { "Saved spot" }, city, area)
        _userFlow.update { it.copy(savedPlaces = it.savedPlaces + place) }
    }

    // D: Favourites & Waitlist
    fun toggleFollowDriver(driverId: String) {
        _followedDriverIds.update { set ->
            if (driverId in set) set - driverId else set + driverId
        }
    }

    fun addFavouriteRoute(origin: String, destination: String, label: String) {
        val route = FavouriteRoute(
            id = "FR-${idCounter.incrementAndGet()}",
            originCity = origin,
            destinationCity = destination,
            label = label.ifBlank { "$origin → $destination" },
        )
        _favouriteRoutes.update { listOf(route) + it }
    }

    fun deleteFavouriteRoute(routeId: String) {
        _favouriteRoutes.update { it.filterNot { r -> r.id == routeId } }
    }

    fun joinWaitlist(rideId: String, seatsWanted: Int = 1) {
        if (_waitlist.value.any { it.rideId == rideId }) return
        val entry = WaitlistEntry("WL-${idCounter.incrementAndGet()}", rideId, seatsWanted, "Just now")
        _waitlist.update { listOf(entry) + it }
        pushNotification(
            type = NotificationType.RIDE_REMINDER,
            title = "Joined waitlist",
            body = "You're on the waitlist for ride $rideId. We'll notify you locally if a seat opens.",
            targetRideId = rideId,
        )
    }

    fun leaveWaitlist(rideId: String) {
        _waitlist.update { it.filterNot { w -> w.rideId == rideId } }
    }

    // C: Safety Contacts, Block, Report, Lost & Found, Disputes
    fun upsertEmergencyContact(id: String?, name: String, phone: String, relation: String) {
        if (id == null) {
            val contact = EmergencyContact("EC-${idCounter.incrementAndGet()}", name.trim(), phone.trim(), relation.trim().ifBlank { "Family" })
            _emergencyContacts.update { it + contact }
        } else {
            _emergencyContacts.update { list ->
                list.map { if (it.id == id) it.copy(name = name.trim(), phone = phone.trim(), relation = relation.trim()) else it }
            }
        }
    }

    fun deleteEmergencyContact(id: String) {
        _emergencyContacts.update { it.filterNot { c -> c.id == id } }
    }

    fun upsertTrustedContact(id: String?, name: String, phone: String, relation: String): Result<Unit> {
        if (id == null && _trustedContacts.value.size >= 3) {
            return Result.failure(IllegalStateException("Maximum of 3 trusted contacts allowed."))
        }
        if (id == null) {
            val contact = TrustedContact("TC-${idCounter.incrementAndGet()}", name.trim(), phone.trim(), relation.trim().ifBlank { "Trusted" })
            _trustedContacts.update { it + contact }
        } else {
            _trustedContacts.update { list ->
                list.map { if (it.id == id) it.copy(name = name.trim(), phone = phone.trim(), relation = relation.trim()) else it }
            }
        }
        return Result.success(Unit)
    }

    fun deleteTrustedContact(id: String) {
        _trustedContacts.update { it.filterNot { c -> c.id == id } }
    }

    fun blockUser(userId: String, name: String, initials: String, reason: String) {
        if (_blockedUsers.value.any { it.userId == userId }) return
        val blocked = BlockedUser("BL-${idCounter.incrementAndGet()}", userId, name, initials, reason.ifBlank { "User preference" }, "Just now")
        _blockedUsers.update { listOf(blocked) + it }
    }

    fun unblockUser(blockId: String) {
        _blockedUsers.update { it.filterNot { b -> b.id == blockId } }
    }

    fun addLostFoundItem(rideId: String, rideSummary: String, itemTitle: String, description: String) {
        val item = LostFoundItem(
            id = "LF-${idCounter.incrementAndGet()}",
            rideId = rideId,
            rideSummary = rideSummary,
            itemTitle = itemTitle.trim(),
            description = description.trim(),
            statusLabel = "Reported (Demo)",
            reportedAtLabel = "Just now",
        )
        _lostFoundItems.update { listOf(item) + it }
    }

    fun submitComplaint(category: String, subject: String, description: String) {
        val complaint = SupportComplaint(
            id = "CMP-${idCounter.incrementAndGet()}",
            category = category,
            subject = subject.trim(),
            description = description.trim(),
            status = ComplaintStatus.OPEN,
            createdAtLabel = "Just now",
        )
        _complaints.update { listOf(complaint) + it }
    }

    override fun sendMessage(conversationId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val userName = _userFlow.value.name
        _conversationsFlow.update { convos ->
            convos.map { convo ->
                if (convo.id == conversationId) {
                    val msg = ChatMessage("M-${idCounter.incrementAndGet()}", userName, true, trimmed, "Just now")
                    convo.copy(messages = convo.messages + msg, lastMessageTime = "Just now", unreadCount = 0)
                } else {
                    convo
                }
            }
        }
    }

    override fun markConversationRead(conversationId: String) {
        _conversationsFlow.update { convos ->
            convos.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }
        }
    }

    override fun markNotificationRead(notificationId: String) {
        _notificationsFlow.update { list ->
            list.map { if (it.id == notificationId) it.copy(isUnread = false) else it }
        }
    }

    fun markAllNotificationsRead() {
        _notificationsFlow.update { list -> list.map { it.copy(isUnread = false) } }
    }

    override fun openOrCreateConversationForBooking(bookingId: String): String? {
        val existing = _conversationsFlow.value.find { it.bookingId == bookingId }
        if (existing != null) return existing.id

        val booking = getBookingById(bookingId) ?: return null
        val ride = getRideById(booking.rideId) ?: return null
        val driver = getDriverById(ride.driverId)

        val counterpartId: String
        val name: String
        val initials: String
        val colorHex: Long
        val verified: Boolean
        val roleLabel: String

        if (booking.isIncomingDriverRequest) {
            counterpartId = booking.passengerId
            name = booking.passengerName
            initials = booking.passengerInitials
            colorHex = 0xFF19A995L
            verified = booking.passengerVerified
            roleLabel = "Passenger • ${Formatters.formatSeats(booking.seatsRequested)}"
        } else {
            counterpartId = driver?.id ?: "D-001"
            name = driver?.name ?: "Driver"
            initials = driver?.initials ?: "DR"
            colorHex = driver?.avatarColorHex ?: 0xFF243B6BL
            verified = driver?.isVerified == true
            roleLabel = "Driver"
        }

        val newId = "C-${idCounter.incrementAndGet()}"
        val newConvo = ChatConversation(
            id = newId,
            counterpartId = counterpartId,
            counterpartName = name,
            counterpartInitials = initials,
            counterpartColorHex = colorHex,
            counterpartVerified = verified,
            counterpartRole = roleLabel,
            rideContext = "${ride.originCity} → ${ride.destinationCity} • ${ride.departureDateLabel}",
            bookingId = booking.id,
            unreadCount = 0,
            lastMessageTime = "New",
            messages = listOf(
                ChatMessage("M-${idCounter.incrementAndGet()}", "System", false, "Demo chat started for ${ride.originCity} → ${ride.destinationCity}. Personal numbers are masked for privacy.", "Now"),
            ),
        )
        _conversationsFlow.update { listOf(newConvo) + it }
        return newId
    }

    private fun pushNotification(
        type: NotificationType,
        title: String,
        body: String,
        targetBookingId: String? = null,
        targetRideId: String? = null,
    ) {
        val notif = AppNotification(
            id = "N-${idCounter.incrementAndGet()}",
            type = type,
            title = title,
            body = body,
            timestampLabel = "Just now",
            isUnread = true,
            targetBookingId = targetBookingId,
            targetRideId = targetRideId,
        )
        _notificationsFlow.update { listOf(notif) + it }
    }
}
