package com.example.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.mock.MockCarPoolStore
import com.example.data.mock.MockDataFactory
import com.example.data.repository.BookingRepository
import com.example.data.repository.InboxRepository
import com.example.data.repository.RideRepository
import com.example.data.repository.UserRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class EntryStage {
    SPLASH,
    ONBOARDING,
    OTP_LOGIN,
    PROFILE_SETUP,
    CHOOSE_MODE,
    APP_READY,
}

@Immutable
data class CarPoolUiState(
    val entryStage: EntryStage = EntryStage.SPLASH,
    val hasSeenOnboarding: Boolean = false,
    val user: AppUser = MockDataFactory.initialUser(),
    val drivers: List<DriverProfile> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val rides: List<Ride> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val conversations: List<ChatConversation> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val followedDriverIds: Set<String> = emptySet(),
    val favouriteRoutes: List<FavouriteRoute> = emptyList(),
    val waitlist: List<WaitlistEntry> = emptyList(),
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val trustedContacts: List<TrustedContact> = emptyList(),
    val blockedUsers: List<BlockedUser> = emptyList(),
    val lostFoundItems: List<LostFoundItem> = emptyList(),
    val complaints: List<SupportComplaint> = emptyList(),
    val payments: List<PaymentTransaction> = emptyList(),
    val driverEarnings: List<DriverEarningEntry> = emptyList(),
    val searchQuery: RideSearchQuery = RideSearchQuery(
        originCity = "Hyderabad",
        destinationCity = "Karachi",
        dateDayOffset = null,
        minSeats = 1,
    ),
    val searchResults: List<Ride> = emptyList(),
    val snackbarMessage: String? = null,
    // Precomputed once per state emission (never allocated during scroll/composition)
    val driversById: Map<String, DriverProfile> = emptyMap(),
    val vehiclesById: Map<String, Vehicle> = emptyMap(),
    val ridesById: Map<String, Ride> = emptyMap(),
    val bookingsById: Map<String, Booking> = emptyMap(),
    val unreadChatCount: Int = 0,
    val unreadNotificationCount: Int = 0,
    val unreadInboxCount: Int = 0,
    val passengerBookings: List<Booking> = emptyList(),
    val driverOwnedRides: List<Ride> = emptyList(),
    val incomingDriverRequests: List<Booking> = emptyList(),
    val pendingDriverRequests: List<Booking> = emptyList(),
    val upcomingExploreRides: List<Ride> = emptyList(),
    val myVehicles: List<Vehicle> = emptyList(),
    val followedDrivers: List<DriverProfile> = emptyList(),
    val totalDriverEarningsPkr: Int = 0,
)

class CarPoolViewModel(
    private val rideRepo: RideRepository = MockCarPoolStore,
    private val bookingRepo: BookingRepository = MockCarPoolStore,
    private val userRepo: UserRepository = MockCarPoolStore,
    private val inboxRepo: InboxRepository = MockCarPoolStore,
) : ViewModel() {

    private val _entryStage = MutableStateFlow(EntryStage.SPLASH)
    private val _hasSeenOnboarding = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow(
        RideSearchQuery(
            originCity = "Hyderabad",
            destinationCity = "Karachi",
            dateDayOffset = null,
            minSeats = 1,
        )
    )
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val searchResultsFlow: StateFlow<List<Ride>> = combine(
        rideRepo.ridesFlow,
        userRepo.driversFlow,
        MockCarPoolStore.blockedUsers,
        _searchQuery,
    ) { _, _, _, query ->
        rideRepo.searchRides(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = rideRepo.searchRides(_searchQuery.value),
    )

    private val coreDataFlow = combine(
        userRepo.userFlow,
        userRepo.driversFlow,
        userRepo.vehiclesFlow,
        rideRepo.ridesFlow,
        bookingRepo.bookingsFlow,
    ) { user, drivers, vehicles, rides, bookings ->
        CoreSnapshot(user, drivers, vehicles, rides, bookings)
    }

    private val socialDataFlow = combine(
        inboxRepo.conversationsFlow,
        inboxRepo.notificationsFlow,
        userRepo.reviewsFlow,
        _entryStage,
        _hasSeenOnboarding,
        _searchQuery,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        SocialSnapshot(
            conversations = arr[0] as List<ChatConversation>,
            notifications = arr[1] as List<AppNotification>,
            reviews = arr[2] as List<Review>,
            entryStage = arr[3] as EntryStage,
            hasSeenOnboarding = arr[4] as Boolean,
            searchQuery = arr[5] as RideSearchQuery,
        )
    }

    private val safetyExtrasFlow = combine(
        MockCarPoolStore.followedDriverIds,
        MockCarPoolStore.favouriteRoutes,
        MockCarPoolStore.waitlist,
        MockCarPoolStore.emergencyContacts,
        MockCarPoolStore.trustedContacts,
        MockCarPoolStore.blockedUsers,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        SafetySnapshot(
            followedDriverIds = arr[0] as Set<String>,
            favouriteRoutes = arr[1] as List<FavouriteRoute>,
            waitlist = arr[2] as List<WaitlistEntry>,
            emergencyContacts = arr[3] as List<EmergencyContact>,
            trustedContacts = arr[4] as List<TrustedContact>,
            blockedUsers = arr[5] as List<BlockedUser>,
        )
    }

    private val financeExtrasFlow = combine(
        MockCarPoolStore.lostFoundItems,
        MockCarPoolStore.complaints,
        MockCarPoolStore.payments,
        MockCarPoolStore.driverEarnings,
        _snackbarMessage,
    ) { lostFound, complaints, payments, earnings, snackbar ->
        FinanceSnapshot(lostFound, complaints, payments, earnings, snackbar)
    }

    val uiState: StateFlow<CarPoolUiState> = combine(
        coreDataFlow,
        socialDataFlow,
        safetyExtrasFlow,
        financeExtrasFlow,
        searchResultsFlow,
    ) { core, social, safety, finance, filteredResults ->
        val driversById = core.drivers.associateBy { it.id }
        val vehiclesById = core.vehicles.associateBy { it.id }
        val ridesById = core.rides.associateBy { it.id }
        val bookingsById = core.bookings.associateBy { it.id }
        val unreadChats = social.conversations.sumOf { it.unreadCount }
        val unreadNotifs = social.notifications.count { it.isUnread }
        val passengerBookings = core.bookings.filter { !it.isIncomingDriverRequest }
        val driverOwnedRides = core.rides.filter { it.isDriverOwned }
        val incomingRequests = core.bookings.filter { it.isIncomingDriverRequest }
        val pendingRequests = incomingRequests.filter {
            it.status == BookingStatus.PENDING || it.status == BookingStatus.NEGOTIATING
        }
        val blockedIds = safety.blockedUsers.map { it.userId }.toSet()
        val upcomingExploreRides = core.rides
            .filter { !it.isDriverOwned && it.driverId !in blockedIds }
            .sortedByDescending { driversById[it.driverId]?.isVerified == true }
            .take(5)
        val myVehicles = core.vehicles.filter { it.driverId == "D-ME" }
        val followedDrivers = core.drivers.filter { it.id in safety.followedDriverIds }
        val totalEarnings = finance.driverEarnings.sumOf { it.amountPkr }

        CarPoolUiState(
            entryStage = social.entryStage,
            hasSeenOnboarding = social.hasSeenOnboarding,
            user = core.user,
            drivers = core.drivers,
            vehicles = core.vehicles,
            rides = core.rides,
            bookings = core.bookings,
            conversations = social.conversations,
            notifications = social.notifications,
            reviews = social.reviews,
            followedDriverIds = safety.followedDriverIds,
            favouriteRoutes = safety.favouriteRoutes,
            waitlist = safety.waitlist,
            emergencyContacts = safety.emergencyContacts,
            trustedContacts = safety.trustedContacts,
            blockedUsers = safety.blockedUsers,
            lostFoundItems = finance.lostFoundItems,
            complaints = finance.complaints,
            payments = finance.payments,
            driverEarnings = finance.driverEarnings,
            searchQuery = social.searchQuery,
            searchResults = filteredResults,
            snackbarMessage = finance.snackbar,
            driversById = driversById,
            vehiclesById = vehiclesById,
            ridesById = ridesById,
            bookingsById = bookingsById,
            unreadChatCount = unreadChats,
            unreadNotificationCount = unreadNotifs,
            unreadInboxCount = unreadChats + unreadNotifs,
            passengerBookings = passengerBookings,
            driverOwnedRides = driverOwnedRides,
            incomingDriverRequests = incomingRequests,
            pendingDriverRequests = pendingRequests,
            upcomingExploreRides = upcomingExploreRides,
            myVehicles = myVehicles,
            followedDrivers = followedDrivers,
            totalDriverEarningsPkr = totalEarnings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CarPoolUiState(
            searchResults = rideRepo.searchRides(_searchQuery.value),
        ),
    )

    // Entry & Auth flow
    fun finishSplash() {
        if (_entryStage.value == EntryStage.SPLASH) {
            _entryStage.value = if (_hasSeenOnboarding.value) EntryStage.OTP_LOGIN else EntryStage.ONBOARDING
        }
    }

    fun finishOnboardingSlides() {
        _hasSeenOnboarding.value = true
        _entryStage.value = EntryStage.OTP_LOGIN
    }

    fun completeOtpLogin(phone: String) {
        MockCarPoolStore.updateUser { it.copy(phone = phone.ifBlank { "+92 300 5550192" }) }
        _entryStage.value = EntryStage.PROFILE_SETUP
        showMessage("OTP verified (demo)")
    }

    fun completeProfileSetup(
        name: String,
        dob: String,
        gender: String,
        emergencyName: String,
        emergencyPhone: String,
    ) {
        val initials = name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "DR" }
        MockCarPoolStore.updateUser {
            it.copy(
                name = name.trim(),
                initials = initials,
                dob = dob,
                gender = gender,
                emergencyContactName = emergencyName.trim(),
                emergencyContactPhone = emergencyPhone.trim(),
            )
        }
        _entryStage.value = EntryStage.CHOOSE_MODE
    }

    fun continueAsBasic() {
        MockCarPoolStore.updateUser {
            it.copy(
                isVerified = false,
                verificationLevel = VerificationLevel.BASIC,
                verificationStatus = VerificationStatus.NOT_STARTED,
                cnicVerified = false,
                licenceVerified = false,
                vehicleVerified = false,
                previewAsBasic = true,
            )
        }
        _entryStage.value = EntryStage.APP_READY
        showMessage("Entered as Basic (Not Verified)")
    }

    fun startVerificationFromChooser() {
        MockCarPoolStore.updateUser { it.copy(previewAsBasic = false) }
        _entryStage.value = EntryStage.APP_READY
    }

    fun skipToDemoApp() {
        _hasSeenOnboarding.value = true
        _entryStage.value = EntryStage.APP_READY
    }

    fun logoutToOtp() {
        _entryStage.value = EntryStage.OTP_LOGIN
    }

    fun signOutToOnboarding() {
        _entryStage.value = EntryStage.ONBOARDING
    }

    fun togglePreviewMode(previewBasic: Boolean) {
        MockCarPoolStore.updateUser { it.copy(previewAsBasic = previewBasic) }
        showMessage(if (previewBasic) "Previewing as Basic (Not Verified)" else "Previewing as Verified account")
    }

    fun submitVerificationWizard(isDriver: Boolean) {
        MockCarPoolStore.submitVerification(isDriver)
        showMessage("Documents submitted for review (demo)")
    }

    fun simulateVerificationOutcome(status: VerificationStatus, isDriver: Boolean) {
        MockCarPoolStore.simulateVerificationOutcome(status, isDriver)
        showMessage("Verification status updated: ${status.label}")
    }

    fun switchRole(role: AppRole) {
        userRepo.switchRole(role)
        showMessage(if (role == AppRole.DRIVER) "Switched to Driver Mode" else "Switched to Passenger Mode")
    }

    fun updateSearchQuery(transform: (RideSearchQuery) -> RideSearchQuery) {
        _searchQuery.update(transform)
    }

    fun swapSearchCities() {
        _searchQuery.update { q ->
            q.copy(originCity = q.destinationCity, destinationCity = q.originCity)
        }
    }

    fun applyQuickSearch(origin: String, destination: String) {
        _searchQuery.update {
            it.copy(originCity = origin, destinationCity = destination, dateDayOffset = null)
        }
    }

    fun clearAllSearchCriteria() {
        _searchQuery.value = RideSearchQuery()
    }

    fun createBooking(
        request: CreateBookingRequest,
        onCreated: (Booking) -> Unit,
    ) {
        viewModelScope.launch {
            bookingRepo.createBooking(request).fold(
                onSuccess = { booking ->
                    showMessage(
                        if (booking.status == BookingStatus.NEGOTIATING) {
                            "Counter-offer sent! Track it in the negotiation timeline."
                        } else {
                            "Booking request sent to driver."
                        }
                    )
                    onCreated(booking)
                },
                onFailure = { err -> showMessage(err.message ?: "Unable to create booking.") },
            )
        }
    }

    fun respondToPendingOffer(bookingId: String, action: OfferAction) {
        viewModelScope.launch {
            bookingRepo.respondToPendingOffer(bookingId, action).fold(
                onSuccess = {
                    showMessage(if (action == OfferAction.ACCEPT) "Offer accepted! Booking confirmed." else "Offer declined.")
                },
                onFailure = { err -> showMessage(err.message ?: "Action failed.") },
            )
        }
    }

    fun submitCounterOffer(
        bookingId: String,
        actor: OfferActor,
        amountPerSeatPkr: Int,
        note: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            bookingRepo.submitCounterOffer(bookingId, actor, amountPerSeatPkr, note).fold(
                onSuccess = {
                    showMessage("Counter-offer submitted.")
                    onSuccess()
                },
                onFailure = { err -> showMessage(err.message ?: "Invalid counter-offer.") },
            )
        }
    }

    fun acceptDriverRequest(bookingId: String) {
        viewModelScope.launch {
            bookingRepo.acceptDriverRequest(bookingId).fold(
                onSuccess = { showMessage("Passenger confirmed and seats updated.") },
                onFailure = { err -> showMessage(err.message ?: "Could not accept request.") },
            )
        }
    }

    fun rejectDriverRequest(bookingId: String) {
        viewModelScope.launch {
            bookingRepo.rejectDriverRequest(bookingId).fold(
                onSuccess = { showMessage("Request declined.") },
                onFailure = { err -> showMessage(err.message ?: "Could not decline request.") },
            )
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepo.cancelBooking(bookingId).fold(
                onSuccess = { showMessage("Booking cancelled.") },
                onFailure = { err -> showMessage(err.message ?: "Could not cancel booking.") },
            )
        }
    }

    fun verifyRideStartOtp(bookingId: String, code: String, onSuccess: () -> Unit) {
        MockCarPoolStore.verifyRideStartOtp(bookingId, code).fold(
            onSuccess = {
                showMessage("OTP verified! Trip is now Ongoing.")
                onSuccess()
            },
            onFailure = { err -> showMessage(err.message ?: "Invalid OTP") },
        )
    }

    fun completeOngoingRide(bookingId: String) {
        MockCarPoolStore.completeOngoingRide(bookingId).fold(
            onSuccess = { showMessage("Trip marked Completed!") },
            onFailure = { err -> showMessage(err.message ?: "Could not complete trip") },
        )
    }

    fun recordPayment(bookingId: String, method: String, promoCode: String?, onPaid: (PaymentTransaction) -> Unit) {
        MockCarPoolStore.recordPayment(bookingId, method, promoCode).fold(
            onSuccess = { tx ->
                showMessage("Demo payment recorded via ${tx.method}")
                onPaid(tx)
            },
            onFailure = { err -> showMessage(err.message ?: "Payment failed") },
        )
    }

    fun submitReview(bookingId: String, driverId: String, rating: Double, comment: String) {
        MockCarPoolStore.submitReview(bookingId, driverId, rating, comment)
        showMessage("Thank you! Your review was saved locally.")
    }

    fun publishRide(
        request: CreateRideRequest,
        onPublished: (Ride) -> Unit,
    ) {
        viewModelScope.launch {
            rideRepo.publishRide(request).fold(
                onSuccess = { ride ->
                    showMessage("Ride published successfully!")
                    onPublished(ride)
                },
                onFailure = { err -> showMessage(err.message ?: "Please check your ride details.") },
            )
        }
    }

    fun sendChatMessage(conversationId: String, text: String) = inboxRepo.sendMessage(conversationId, text)
    fun markConversationRead(conversationId: String) = inboxRepo.markConversationRead(conversationId)
    fun markNotificationRead(notificationId: String) = inboxRepo.markNotificationRead(notificationId)
    fun markAllNotificationsRead() {
        MockCarPoolStore.markAllNotificationsRead()
        showMessage("All notifications marked as read")
    }
    fun openConversationForBooking(bookingId: String): String? = inboxRepo.openOrCreateConversationForBooking(bookingId)

    fun updatePreferences(transform: (UserPreferences) -> UserPreferences) = userRepo.updatePreferences(transform)
    fun updateUserProfile(transform: (AppUser) -> AppUser) {
        MockCarPoolStore.updateUser(transform)
        showMessage("Profile saved locally")
    }

    fun addVehicle(
        makeModel: String,
        year: Int,
        color: String,
        plateNumber: String,
        seatCapacity: Int,
        luggageCapacity: LuggageCapacity,
    ): Vehicle {
        val vehicle = userRepo.addVehicle(makeModel, year, color, plateNumber, seatCapacity, luggageCapacity)
        showMessage("$makeModel added to your garage.")
        return vehicle
    }

    fun updateVehicle(vehicle: Vehicle) {
        MockCarPoolStore.updateVehicle(vehicle)
        showMessage("Vehicle updated")
    }

    fun setActiveVehicle(vehicleId: String) {
        MockCarPoolStore.setActiveVehicle(vehicleId)
        showMessage("Active vehicle updated")
    }

    fun deleteVehicle(vehicleId: String) {
        MockCarPoolStore.deleteVehicle(vehicleId)
        showMessage("Vehicle removed")
    }

    fun addSavedPlace(label: String, city: String, area: String) {
        userRepo.addSavedPlace(label, city, area)
        showMessage("Saved place \"$label\" added.")
    }

    fun toggleFollowDriver(driverId: String) {
        val wasFollowing = driverId in uiState.value.followedDriverIds
        MockCarPoolStore.toggleFollowDriver(driverId)
        showMessage(if (wasFollowing) "Unfollowed driver" else "Added to Favourite Drivers")
    }

    fun addFavouriteRoute(origin: String, destination: String, label: String) {
        MockCarPoolStore.addFavouriteRoute(origin, destination, label)
        showMessage("Route saved to Favourites")
    }

    fun deleteFavouriteRoute(id: String) {
        MockCarPoolStore.deleteFavouriteRoute(id)
        showMessage("Route removed")
    }

    fun joinWaitlist(rideId: String) {
        MockCarPoolStore.joinWaitlist(rideId)
        showMessage("Added to Waitlist! Check My Trips → Waitlist.")
    }

    fun leaveWaitlist(rideId: String) {
        MockCarPoolStore.leaveWaitlist(rideId)
        showMessage("Left waitlist")
    }

    fun upsertEmergencyContact(id: String?, name: String, phone: String, relation: String) {
        MockCarPoolStore.upsertEmergencyContact(id, name, phone, relation)
        showMessage("Emergency contact saved")
    }

    fun deleteEmergencyContact(id: String) {
        MockCarPoolStore.deleteEmergencyContact(id)
        showMessage("Emergency contact removed")
    }

    fun upsertTrustedContact(id: String?, name: String, phone: String, relation: String) {
        MockCarPoolStore.upsertTrustedContact(id, name, phone, relation).fold(
            onSuccess = { showMessage("Trusted contact saved") },
            onFailure = { err -> showMessage(err.message ?: "Limit reached") },
        )
    }

    fun deleteTrustedContact(id: String) {
        MockCarPoolStore.deleteTrustedContact(id)
        showMessage("Trusted contact removed")
    }

    fun blockUser(userId: String, name: String, initials: String, reason: String) {
        MockCarPoolStore.blockUser(userId, name, initials, reason)
        showMessage("$name blocked locally")
    }

    fun unblockUser(blockId: String) {
        MockCarPoolStore.unblockUser(blockId)
        showMessage("User unblocked")
    }

    fun addLostFoundItem(rideId: String, rideSummary: String, itemTitle: String, description: String) {
        MockCarPoolStore.addLostFoundItem(rideId, rideSummary, itemTitle, description)
        showMessage("Lost & Found item logged locally")
    }

    fun submitComplaint(category: String, subject: String, description: String) {
        MockCarPoolStore.submitComplaint(category, subject, description)
        showMessage("Support ticket submitted (demo)")
    }

    fun resetDemoData() {
        MockCarPoolStore.resetDemoData()
        _searchQuery.value = RideSearchQuery(
            originCity = "Hyderabad",
            destinationCity = "Karachi",
            dateDayOffset = null,
            minSeats = 1,
        )
        showMessage("Demo data reset to original sample rides and bookings.")
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun consumeMessage() {
        _snackbarMessage.value = null
    }

    private data class CoreSnapshot(
        val user: AppUser,
        val drivers: List<DriverProfile>,
        val vehicles: List<Vehicle>,
        val rides: List<Ride>,
        val bookings: List<Booking>,
    )

    private data class SocialSnapshot(
        val conversations: List<ChatConversation>,
        val notifications: List<AppNotification>,
        val reviews: List<Review>,
        val entryStage: EntryStage,
        val hasSeenOnboarding: Boolean,
        val searchQuery: RideSearchQuery,
    )

    private data class SafetySnapshot(
        val followedDriverIds: Set<String>,
        val favouriteRoutes: List<FavouriteRoute>,
        val waitlist: List<WaitlistEntry>,
        val emergencyContacts: List<EmergencyContact>,
        val trustedContacts: List<TrustedContact>,
        val blockedUsers: List<BlockedUser>,
    )

    private data class FinanceSnapshot(
        val lostFoundItems: List<LostFoundItem>,
        val complaints: List<SupportComplaint>,
        val payments: List<PaymentTransaction>,
        val driverEarnings: List<DriverEarningEntry>,
        val snackbar: String?,
    )
}
