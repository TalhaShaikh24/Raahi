package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CounterOfferNegotiationScreen(
    bookingId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onRespondToOffer: (bookingId: String, action: OfferAction) -> Unit,
    onSubmitCounterOffer: (bookingId: String, actor: OfferActor, amount: Int, note: String) -> Unit,
    onSwitchRole: (AppRole) -> Unit,
    onOpenBookingDetails: (String) -> Unit,
    onBrowseOtherRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.bookingsById[bookingId]
    val ride = booking?.let { state.ridesById[it.rideId] }

    if (booking == null || ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Fare negotiation", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Rounded.Handshake,
                    title = "Negotiation not found",
                    subtitle = "This booking record could not be loaded.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )
            }
        }
        return
    }

    val pendingOffer = remember(booking.offers) {
        booking.offers.lastOrNull { it.status == CounterOfferStatus.PENDING }
    }
    val userRoleInBooking: OfferActor = if (state.user.activeRole == AppRole.DRIVER) OfferActor.DRIVER else OfferActor.PASSENGER
    val isUsersTurnToRespond = pendingOffer != null && pendingOffer.actor != userRoleInBooking

    var showCounterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Counter-offer timeline",
                subtitle = "${ride.originCity} → ${ride.destinationCity} • ${booking.id}",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { onOpenBookingDetails(booking.id) }) {
                        Text("Details", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                    }
                },
            )
        },
        bottomBar = {
            if (booking.status == BookingStatus.NEGOTIATING || booking.status == BookingStatus.PENDING) {
                Column(modifier = Modifier.background(AppColors.Surface)) {
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        if (isUsersTurnToRespond && pendingOffer != null) {
                            Text(
                                text = "Your turn: Respond to ${Formatters.formatPkr(pendingOffer.amountPerSeatPkr)}/seat",
                                style = AppTextStyles.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Primary,
                            )
                            RaahiPrimaryButton(
                                text = "Accept Offer",
                                onClick = { onRespondToOffer(booking.id, OfferAction.ACCEPT) },
                                modifier = Modifier.testTag("negotiation_accept_button"),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                            ) {
                                RaahiSecondaryButton(
                                    text = "Decline",
                                    onClick = { onRespondToOffer(booking.id, OfferAction.REJECT) },
                                    textColor = AppColors.Error,
                                    borderColor = AppColors.Error.copy(alpha = 0.4f),
                                    modifier = Modifier.weight(1f).testTag("negotiation_reject_button"),
                                )
                                RaahiSecondaryButton(
                                    text = "Counter",
                                    onClick = { showCounterSheet = true },
                                    modifier = Modifier.weight(1f).testTag("negotiation_counter_button"),
                                )
                            }
                        } else if (pendingOffer != null) {
                            val waitingFor = if (pendingOffer.actor == OfferActor.PASSENGER) OfferActor.DRIVER else OfferActor.PASSENGER
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                                Text("Waiting for ${waitingFor.label} response", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                                Text(
                                    text = "Switch role to test the ${waitingFor.label.lowercase()}'s reply in this prototype.",
                                    style = AppTextStyles.caption,
                                    color = AppColors.TextSecondary,
                                )
                                RaahiSecondaryButton(
                                    text = "Reply as ${waitingFor.label}",
                                    onClick = {
                                        onSwitchRole(if (waitingFor == OfferActor.DRIVER) AppRole.DRIVER else AppRole.PASSENGER)
                                    },
                                    modifier = Modifier.testTag("negotiation_switch_role_button"),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item(key = "summary_card", contentType = "summary") {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            BookingStatusChip(status = booking.status)
                            RaahiPill(
                                text = booking.offerExpiryLabel,
                                containerColor = AppColors.AccentSoft,
                                contentColor = AppColors.AccentOnSoft,
                                icon = Icons.Rounded.Timer,
                            )
                        }

                        Text(
                            text = "${ride.originCity} (${booking.pickupPoint}) → ${ride.destinationCity} (${booking.dropoffPoint})",
                            style = AppTextStyles.cardTitle,
                            color = AppColors.TextPrimary,
                        )
                        Text(
                            text = "${ride.departureDateLabel} • ${ride.departureTimeLabel} • ${Formatters.formatSeats(booking.seatsRequested)}",
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                        )

                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Original listed fare", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                Text("${Formatters.formatPkr(booking.listedFarePerSeatPkr)}/seat", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val displayAmount = booking.agreedFarePerSeatPkr ?: pendingOffer?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr
                                Text(if (booking.agreedFarePerSeatPkr != null) "Agreed fare" else "Current offer", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                Text(
                                    text = "${Formatters.formatPkr(displayAmount)}/seat",
                                    style = AppTextStyles.price,
                                    color = if (booking.agreedFarePerSeatPkr != null) AppColors.VerifiedText else AppColors.Primary,
                                )
                                Text(
                                    text = "${Formatters.formatPkr(displayAmount * booking.seatsRequested)} total",
                                    style = AppTextStyles.caption,
                                    color = AppColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            if (booking.status == BookingStatus.CONFIRMED && booking.agreedFarePerSeatPkr != null) {
                item(key = "confirmed_banner", contentType = "status_banner") {
                    RaahiCard(
                        containerColor = AppColors.VerifiedBg,
                        borderColor = AppColors.VerifiedText.copy(alpha = 0.4f),
                        elevated = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AppColors.VerifiedText, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(AppSpacing.s12))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Confirmed at ${Formatters.formatPkr(booking.agreedFarePerSeatPkr)}/seat", style = AppTextStyles.cardTitle, color = AppColors.VerifiedText)
                                Text("Ride Start OTP: ${booking.rideStartOtp}", style = AppTextStyles.caption, color = AppColors.TextPrimary)
                            }
                            TextButton(onClick = { onOpenBookingDetails(booking.id) }) {
                                Text("Trip", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                            }
                        }
                    }
                }
            } else if (booking.status == BookingStatus.REJECTED || booking.status == BookingStatus.CANCELLED) {
                item(key = "closed_banner", contentType = "status_banner") {
                    RaahiCard(
                        containerColor = AppColors.SosBg,
                        borderColor = AppColors.Error.copy(alpha = 0.35f),
                        elevated = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Cancel, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(AppSpacing.s12))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Negotiation closed", style = AppTextStyles.cardTitle, color = AppColors.Error)
                                Text("Browse other upcoming carpools on this route.", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            }
                            TextButton(onClick = onBrowseOtherRides) {
                                Text("Explore", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                            }
                        }
                    }
                }
            }

            item(key = "offer_history_header", contentType = "section_header") {
                Text("Offer history", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            }

            items(
                items = booking.offers,
                key = { "${it.sequence}_${it.timestampLabel}" },
                contentType = { "offer_timeline_item" },
            ) { offer ->
                CounterOfferTimelineItem(offer = offer, seatsRequested = booking.seatsRequested)
            }

            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(Modifier.height(AppSpacing.s16))
            }
        }
    }

    if (showCounterSheet) {
        CounterOfferComposerSheet(
            listedFare = booking.listedFarePerSeatPkr,
            currentOffer = pendingOffer?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr,
            seats = booking.seatsRequested,
            onSubmit = { amount, note ->
                onSubmitCounterOffer(booking.id, userRoleInBooking, amount, note)
                showCounterSheet = false
            },
            onDismiss = { showCounterSheet = false },
        )
    }
}

@Composable
fun CounterOfferTimelineItem(
    offer: CounterOffer,
    seatsRequested: Int,
) {
    val isPending = offer.status == CounterOfferStatus.PENDING
    val isDriver = offer.actor == OfferActor.DRIVER
    val perSeatLabel = remember(offer.amountPerSeatPkr) { "${Formatters.formatPkr(offer.amountPerSeatPkr)} / seat" }
    val totalLabel = remember(offer.amountPerSeatPkr, seatsRequested) {
        "Total: ${Formatters.formatPkr(offer.amountPerSeatPkr * seatsRequested)}"
    }

    RaahiCard(
        containerColor = if (isPending) AppColors.Surface else AppColors.Background,
        borderColor = if (isPending) AppColors.Accent else AppColors.Border,
        borderWidth = if (isPending) 1.5.dp else 1.dp,
        elevated = false,
        modifier = Modifier.fillMaxWidth().testTag("offer_timeline_item_${offer.sequence}"),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isDriver) AppColors.PrimaryDark else AppColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("#${offer.sequence}", style = AppTextStyles.chip, color = Color.White)
                    }
                    Spacer(Modifier.width(AppSpacing.s8))
                    Column {
                        Text(
                            text = "${offer.actor.label} • ${offer.actorName}",
                            style = AppTextStyles.bodyCompact,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(offer.timestampLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    }
                }
                OfferStatusChip(status = offer.status)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = perSeatLabel,
                    style = AppTextStyles.price,
                    color = if (isPending) AppColors.Primary else AppColors.TextPrimary,
                )
                Text(totalLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            if (offer.note.isNotBlank()) {
                Text(offer.note, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CounterOfferComposerSheet(
    listedFare: Int,
    currentOffer: Int,
    seats: Int,
    onSubmit: (amount: Int, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf(currentOffer.toString()) }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        shape = RoundedCornerShape(topStart = AppSpacing.radiusSheet, topEnd = AppSpacing.radiusSheet),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.s20)
                .padding(bottom = AppSpacing.s24),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            Text("Propose counter-offer", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            Text(
                text = "Listed fare: ${Formatters.formatPkr(listedFare)}/seat • Latest: ${Formatters.formatPkr(currentOffer)}/seat",
                style = AppTextStyles.caption,
                color = AppColors.TextSecondary,
            )
            RaahiTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() }; errorMsg = null },
                label = "Counter-offer per seat (PKR)",
                prefixText = "PKR ",
                errorText = errorMsg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                fieldTestTag = "counter_composer_amount",
            )
            val parsed = amountText.toIntOrNull() ?: 0
            if (parsed > 0) {
                Text("Total for ${Formatters.formatSeats(seats)}: ${Formatters.formatPkr(parsed * seats)}", style = AppTextStyles.cardTitle, color = AppColors.Primary)
            }
            RaahiTextField(
                value = note,
                onValueChange = { note = it },
                label = "Message (optional)",
                placeholder = "e.g., Ready for early pickup",
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                RaahiPrimaryButton(
                    text = "Send Counter-Offer",
                    onClick = {
                        val amount = amountText.toIntOrNull()
                        if (amount == null || amount <= 0) {
                            errorMsg = "Enter a valid PKR amount greater than 0."
                            return@RaahiPrimaryButton
                        }
                        onSubmit(amount, note)
                    },
                    modifier = Modifier.testTag("counter_composer_submit"),
                )
                RaahiSecondaryButton(text = "Cancel", onClick = onDismiss)
            }
        }
    }
}

private enum class TripFilterTab(val label: String) {
    UPCOMING("Upcoming"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    WAITLIST("Waitlist"),
    CANCELLED("Cancelled"),
}

@Composable
fun MyTripsScreen(
    state: CarPoolUiState,
    onOpenBooking: (String) -> Unit,
    onOpenNegotiation: (String) -> Unit,
    onOpenLiveRide: (String) -> Unit,
    onOpenRideDetail: (String) -> Unit,
    onLeaveWaitlist: (String) -> Unit,
    onOpenRateReview: (String) -> Unit,
    onExploreRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(TripFilterTab.UPCOMING) }

    val tabCounts = remember(state.passengerBookings, state.waitlist.size) {
        var upcoming = 0
        var pending = 0
        var completed = 0
        var cancelled = 0
        for (booking in state.passengerBookings) {
            when (booking.status) {
                BookingStatus.CONFIRMED, BookingStatus.ONGOING -> upcoming++
                BookingStatus.PENDING, BookingStatus.NEGOTIATING -> pending++
                BookingStatus.COMPLETED -> completed++
                BookingStatus.CANCELLED, BookingStatus.REJECTED -> cancelled++
            }
        }
        mapOf(
            TripFilterTab.UPCOMING to upcoming,
            TripFilterTab.PENDING to pending,
            TripFilterTab.COMPLETED to completed,
            TripFilterTab.WAITLIST to state.waitlist.size,
            TripFilterTab.CANCELLED to cancelled,
        )
    }

    val filteredBookings = remember(state.passengerBookings, selectedTab) {
        when (selectedTab) {
            TripFilterTab.UPCOMING -> state.passengerBookings.filter { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.ONGOING }
            TripFilterTab.PENDING -> state.passengerBookings.filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.NEGOTIATING }
            TripFilterTab.COMPLETED -> state.passengerBookings.filter { it.status == BookingStatus.COMPLETED }
            TripFilterTab.WAITLIST -> emptyList()
            TripFilterTab.CANCELLED -> state.passengerBookings.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        Column(modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16)) {
            Text("My Rides", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
            Spacer(Modifier.height(AppSpacing.s4))
            Text("Track bookings, counter-offers, live rides, and waitlist", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = AppSpacing.screenPadding,
            containerColor = AppColors.Surface,
            contentColor = AppColors.Primary,
            divider = { HorizontalDivider(color = AppColors.Border, thickness = 1.dp) },
        ) {
            TripFilterTab.entries.forEach { tab ->
                val count = tabCounts[tab] ?: 0
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = "${tab.label} ($count)",
                            style = AppTextStyles.chip,
                            maxLines = 1,
                            softWrap = false,
                        )
                    },
                    modifier = Modifier.testTag("trips_tab_${tab.name.lowercase()}"),
                )
            }
        }

        if (selectedTab == TripFilterTab.WAITLIST) {
            if (state.waitlist.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Rounded.HourglassEmpty,
                        title = "No waitlisted rides",
                        subtitle = "When a ride is full, tap Join Waitlist on Ride Details to track it here.",
                        actionLabel = "Explore rides",
                        onAction = onExploreRides,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    items(
                        items = state.waitlist,
                        key = { it.id },
                        contentType = { "waitlist_card" },
                    ) { entry ->
                        val ride = state.ridesById[entry.rideId]
                        WaitlistTripCard(
                            entry = entry,
                            ride = ride,
                            onOpenRideDetail = onOpenRideDetail,
                            onLeaveWaitlist = onLeaveWaitlist,
                        )
                    }
                }
            }
        } else if (filteredBookings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Rounded.Commute,
                    title = "No ${selectedTab.label.lowercase()} trips",
                    subtitle = "Explore upcoming intercity and local carpools to book a seat.",
                    actionLabel = "Find a ride",
                    onAction = onExploreRides,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(
                    items = filteredBookings,
                    key = { it.id },
                    contentType = { "trip_booking_card" },
                ) { booking ->
                    val ride = state.ridesById[booking.rideId]
                    val driver = ride?.let { state.driversById[it.driverId] }
                    TripBookingCard(
                        booking = booking,
                        ride = ride,
                        driverName = driver?.name ?: "Driver",
                        onOpenBooking = onOpenBooking,
                        onOpenNegotiation = onOpenNegotiation,
                        onOpenLiveRide = onOpenLiveRide,
                        onOpenRateReview = onOpenRateReview,
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitlistTripCard(
    entry: WaitlistEntry,
    ride: Ride?,
    onOpenRideDetail: (String) -> Unit,
    onLeaveWaitlist: (String) -> Unit,
) {
    val clickRide = remember(ride?.id, onOpenRideDetail) {
        { if (ride != null) onOpenRideDetail(ride.id) }
    }
    val leaveClick = remember(entry.rideId, onLeaveWaitlist) {
        { onLeaveWaitlist(entry.rideId) }
    }
    val seatsLabel = remember(entry.seatsWanted) { Formatters.formatSeats(entry.seatsWanted) }

    RaahiCard(
        onClick = clickRide,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                RaahiPill(text = "Waitlisted", containerColor = AppColors.AccentSoft, contentColor = AppColors.AccentOnSoft)
                Text(entry.joinedAtLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            Text("${ride?.originCity ?: ""} → ${ride?.destinationCity ?: ""}", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
            Text("${ride?.departureDateLabel ?: ""} • ${ride?.departureTimeLabel ?: ""} • $seatsLabel", style = AppTextStyles.caption, color = AppColors.TextSecondary)
            RaahiSecondaryButton(text = "Leave Waitlist", onClick = leaveClick)
        }
    }
}

@Composable
private fun TripBookingCard(
    booking: Booking,
    ride: Ride?,
    driverName: String,
    onOpenBooking: (String) -> Unit,
    onOpenNegotiation: (String) -> Unit,
    onOpenLiveRide: (String) -> Unit,
    onOpenRateReview: (String) -> Unit,
) {
    val totalFareFormatted = remember(booking.agreedFarePerSeatPkr, booking.offers, booking.listedFarePerSeatPkr, booking.seatsRequested) {
        val activeAmount = booking.agreedFarePerSeatPkr
            ?: booking.offers.lastOrNull()?.amountPerSeatPkr
            ?: booking.listedFarePerSeatPkr
        Formatters.formatPkr(activeAmount * booking.seatsRequested)
    }
    val seatsDriverLabel = remember(booking.seatsRequested, driverName) {
        "${Formatters.formatSeats(booking.seatsRequested)} • $driverName"
    }
    val cardClick = remember(booking.id, booking.status, onOpenNegotiation, onOpenLiveRide, onOpenBooking) {
        {
            when (booking.status) {
                BookingStatus.NEGOTIATING -> onOpenNegotiation(booking.id)
                BookingStatus.ONGOING -> onOpenLiveRide(booking.id)
                else -> onOpenBooking(booking.id)
            }
        }
    }

    RaahiCard(
        onClick = cardClick,
        elevated = false,
        modifier = Modifier.fillMaxWidth().testTag("trip_booking_card_${booking.id}"),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BookingStatusChip(status = booking.status)
                Text("${ride?.departureDateLabel ?: ""} • ${ride?.departureTimeLabel ?: ""}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            Text(
                text = "${ride?.originCity ?: ""} (${booking.pickupPoint}) → ${ride?.destinationCity ?: ""} (${booking.dropoffPoint})",
                style = AppTextStyles.cardTitle,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = seatsDriverLabel,
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = totalFareFormatted,
                    style = AppTextStyles.price,
                    color = AppColors.Primary,
                )
            }
            if (booking.status == BookingStatus.CONFIRMED) {
                Text("Ride Start OTP: ${booking.rideStartOtp} (share with driver at pickup)", style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.PrimaryDark)
            } else if (booking.status == BookingStatus.ONGOING) {
                RaahiPrimaryButton(text = "Open Live Ride", onClick = { onOpenLiveRide(booking.id) })
            } else if (booking.status == BookingStatus.COMPLETED && !booking.isRated) {
                RaahiSecondaryButton(text = "Rate & Review", onClick = { onOpenRateReview(booking.id) })
            } else if (booking.status == BookingStatus.NEGOTIATING) {
                Surface(shape = RoundedCornerShape(AppSpacing.radiusInput), color = AppColors.AccentSoft, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Handshake, contentDescription = null, tint = AppColors.AccentOnSoft, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(AppSpacing.s8))
                        Text("Counter-offer active • Tap to respond", style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.AccentOnSoft)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailsScreen(
    bookingId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onOpenNegotiation: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onCancelBooking: (String) -> Unit,
    onOpenSafety: () -> Unit,
    onVerifyStartOtp: (String, String, () -> Unit) -> Unit,
    onOpenLiveRide: (String) -> Unit,
    onOpenPayment: (String) -> Unit,
    onOpenRateReview: (String) -> Unit,
    onOpenLostFound: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.bookingsById[bookingId]
    val ride = booking?.let { state.ridesById[it.rideId] }
    val driver = ride?.let { state.driversById[it.driverId] }
    val vehicle = ride?.let { state.vehiclesById[it.vehicleId] }

    var showCancelDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("1234") }

    if (booking == null || ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Booking details", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Booking could not be found.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    val activeFarePerSeat = booking.agreedFarePerSeatPkr
        ?: booking.offers.lastOrNull()?.amountPerSeatPkr
        ?: booking.listedFarePerSeatPkr

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Booking ${booking.id}",
                subtitle = "${ride.originCity} → ${ride.destinationCity}",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BookingStatusChip(status = booking.status)
                        Text("${ride.departureDateLabel} • ${ride.departureTimeLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    }
                    Text("${booking.pickupPoint} → ${booking.dropoffPoint}", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text("${Formatters.formatSeats(booking.seatsRequested)} • Created ${booking.createdAtLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
            }

            if (booking.status == BookingStatus.CONFIRMED) {
                RaahiCard(
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                                Text("Passenger Ride Start OTP", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                                Text("Share this 4-digit code with the driver to begin", style = AppTextStyles.caption, color = AppColors.PrimaryDark)
                            }
                            Surface(shape = RoundedCornerShape(AppSpacing.radiusInput), color = AppColors.Surface) {
                                Text(
                                    text = booking.rideStartOtp,
                                    style = AppTextStyles.screenTitle,
                                    color = AppColors.Primary,
                                    modifier = Modifier.padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s8),
                                )
                            }
                        }
                        RaahiPrimaryButton(
                            text = "Start Ride (Enter OTP)",
                            onClick = { showOtpDialog = true },
                            modifier = Modifier.testTag("booking_enter_otp_button"),
                        )
                    }
                }
            }

            if (booking.status == BookingStatus.ONGOING) {
                RaahiPrimaryButton(
                    text = "Open Live Ride & SOS",
                    icon = Icons.Rounded.Navigation,
                    onClick = { onOpenLiveRide(booking.id) },
                    modifier = Modifier.testTag("booking_open_live_ride"),
                )
            }

            if (driver != null) {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        DriverMiniProfile(driver = driver, vehicle = vehicle)
                        Text(
                            text = "Privacy protection: personal phone numbers are hidden. Use in-app chat or masked call.",
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                            RaahiSecondaryButton(
                                text = "Chat",
                                icon = Icons.Rounded.ChatBubbleOutline,
                                onClick = { onOpenChat(booking.id) },
                                modifier = Modifier.weight(1f).testTag("booking_details_chat_button"),
                            )
                            RaahiSecondaryButton(
                                text = "Call",
                                icon = Icons.Rounded.Call,
                                onClick = { onShowMessage("Calling via app… (masked number)") },
                                modifier = Modifier.weight(1f).testTag("booking_details_masked_call_button"),
                            )
                        }
                        RaahiSecondaryButton(
                            text = "Share Ride Link",
                            icon = Icons.Rounded.Share,
                            onClick = { showShareDialog = true },
                            modifier = Modifier.testTag("booking_details_share_button"),
                        )
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Fare & payment", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Listed fare", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                        Text("${Formatters.formatPkr(booking.listedFarePerSeatPkr)}/seat", style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (booking.agreedFarePerSeatPkr != null) "Agreed fare" else "Latest offer", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                        Text("${Formatters.formatPkr(activeFarePerSeat)} × ${booking.seatsRequested}", style = AppTextStyles.bodyCompact, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    }
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        Text(Formatters.formatPkr(activeFarePerSeat * booking.seatsRequested), style = AppTextStyles.price, color = AppColors.Primary)
                    }
                    if (!booking.isPaid && (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.ONGOING || booking.status == BookingStatus.COMPLETED)) {
                        RaahiPrimaryButton(
                            text = "Pay Fare",
                            onClick = { onOpenPayment(booking.id) },
                            modifier = Modifier.testTag("booking_details_pay_button"),
                        )
                    } else if (booking.isPaid) {
                        VerificationBadgeChip(isVerified = true, label = "Paid via ${booking.paymentMethod ?: "Demo"}")
                    }
                    RaahiSecondaryButton(
                        text = "Offer History (${booking.offers.size})",
                        icon = Icons.Rounded.History,
                        onClick = { onOpenNegotiation(booking.id) },
                        modifier = Modifier.testTag("booking_details_negotiation_button"),
                    )
                }
            }

            if (booking.status == BookingStatus.COMPLETED && !booking.isRated) {
                RaahiPrimaryButton(
                    text = "Rate & Review Trip",
                    icon = Icons.Rounded.Star,
                    onClick = { onOpenRateReview(booking.id) },
                    modifier = Modifier.testTag("booking_details_rate_button"),
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                RaahiSecondaryButton(
                    text = "Safety",
                    icon = Icons.Rounded.Shield,
                    onClick = onOpenSafety,
                    modifier = Modifier.weight(1f),
                )
                RaahiSecondaryButton(
                    text = "Lost & Found",
                    icon = Icons.Rounded.WorkOutline,
                    onClick = onOpenLostFound,
                    modifier = Modifier.weight(1f),
                )
            }

            if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.PENDING || booking.status == BookingStatus.NEGOTIATING) {
                RaahiSecondaryButton(
                    text = "Cancel Booking",
                    onClick = { showCancelDialog = true },
                    textColor = AppColors.Error,
                    borderColor = AppColors.Error.copy(alpha = 0.4f),
                    modifier = Modifier.testTag("booking_details_cancel_button"),
                )
            }
            Spacer(Modifier.height(AppSpacing.s12))
        }
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Enter Ride Start OTP", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Ask passenger for their 4-digit start code (demo code: 1234).", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                    RaahiTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it.filter { c -> c.isDigit() }.take(4) },
                        label = "4-digit OTP",
                        placeholder = "1234",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        fieldTestTag = "ride_start_otp_input",
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = "Start Trip",
                        onClick = {
                            onVerifyStartOtp(booking.id, otpInput) {
                                showOtpDialog = false
                                onOpenLiveRide(booking.id)
                            }
                        },
                        modifier = Modifier.testTag("ride_start_otp_confirm"),
                    )
                    RaahiSecondaryButton(text = "Cancel", onClick = { showOtpDialog = false })
                }
            },
        )
    }

    if (showCancelDialog) {
        RaahiConfirmDialog(
            title = "Cancel this booking?",
            message = "Cancelling releases your reserved seats on ${ride.originCity} → ${ride.destinationCity} in the local demo.",
            confirmLabel = "Yes, cancel",
            isDestructive = true,
            onConfirm = { showCancelDialog = false; onCancelBooking(booking.id) },
            onDismiss = { showCancelDialog = false },
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            icon = { Icon(Icons.Rounded.ShareLocation, contentDescription = null, tint = AppColors.Primary) },
            title = { Text("Demo Trip Link Ready", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Text(
                    text = "Simulated share link copied:\nraahi.pk/trip/${booking.id} (${ride.originCity} → ${ride.destinationCity}, ${ride.departureDateLabel}, ${driver?.name ?: ""}).",
                    style = AppTextStyles.bodyCompact,
                    color = AppColors.TextSecondary,
                )
            },
            confirmButton = {
                RaahiPrimaryButton(text = "Done", onClick = { showShareDialog = false })
            },
        )
    }
}
