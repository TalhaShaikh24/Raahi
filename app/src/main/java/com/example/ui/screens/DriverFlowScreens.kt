package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.data.mock.MockDataFactory
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DriverDashboardScreen(
    state: CarPoolUiState,
    onSwitchRole: (AppRole) -> Unit,
    onPublishRideClick: () -> Unit,
    onOpenRequests: () -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onOpenNegotiation: (String) -> Unit,
    onRideClick: (String) -> Unit,
    onOpenVerification: () -> Unit,
    onOpenEarnings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val driverRides = state.driverOwnedRides
    val pendingRequests = state.pendingDriverRequests
    val seatsBooked = remember(driverRides) { driverRides.sumOf { it.totalSeats - it.availableSeats } }
    val totalEarningsLabel = remember(state.totalDriverEarningsPkr) {
        Formatters.formatPkr(state.totalDriverEarningsPkr)
    }

    var requestToReject by remember { mutableStateOf<Booking?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
    ) {
        item(key = "driver_header", contentType = "driver_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                    Text(
                        text = "Driver Hub",
                        style = AppTextStyles.screenTitle,
                        color = AppColors.TextPrimary,
                    )
                    Spacer(Modifier.height(AppSpacing.s4))
                    VerificationBadgeChip(
                        isVerified = state.user.isEffectivelyVerified,
                        label = state.user.effectiveVerificationLevel.label,
                        modifier = Modifier.clickable(onClick = onOpenVerification),
                    )
                }
                RoleModePill(
                    activeRole = state.user.activeRole,
                    onToggle = onSwitchRole,
                )
            }
        }

        // Quick stats row
        item(key = "driver_stats", contentType = "driver_stats") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
            ) {
                DriverStatCard(
                    label = "Active rides",
                    value = "${driverRides.size}",
                    accentColor = AppColors.Primary,
                    modifier = Modifier.weight(1f),
                )
                DriverStatCard(
                    label = "Seats booked",
                    value = "$seatsBooked",
                    accentColor = AppColors.PrimaryDark,
                    modifier = Modifier.weight(1f),
                )
                DriverStatCard(
                    label = "Earnings",
                    value = totalEarningsLabel,
                    accentColor = AppColors.Primary,
                    modifier = Modifier
                        .weight(1.3f)
                        .clickable(onClick = onOpenEarnings)
                        .testTag("driver_earnings_stat_card"),
                )
            }
        }

        // Primary Publish a Ride CTA
        item(key = "driver_publish_cta", contentType = "driver_cta") {
            RaahiCard(
                containerColor = AppColors.PrimarySoft,
                borderColor = AppColors.Primary.copy(alpha = 0.25f),
                elevated = false,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    Text(
                        text = "Heading out soon?",
                        style = AppTextStyles.sectionTitle,
                        color = AppColors.PrimaryDark,
                    )
                    Text(
                        text = "Publish empty seats and review passenger offers in seconds.",
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.PrimaryDark,
                    )
                    RaahiPrimaryButton(
                        text = "Publish a Ride",
                        icon = Icons.Rounded.Add,
                        onClick = onPublishRideClick,
                        modifier = Modifier.testTag("driver_publish_ride_button"),
                    )
                }
            }
        }

        item(key = "requests_header", contentType = "section_header") {
            SectionHeader(
                title = "Booking requests (${pendingRequests.size})",
                actionLabel = "View all",
                onAction = onOpenRequests,
            )
        }

        if (pendingRequests.isEmpty()) {
            item(key = "requests_empty", contentType = "empty_state") {
                EmptyStateView(
                    icon = Icons.Rounded.Inbox,
                    title = "No pending requests",
                    subtitle = "New passenger seat requests and fare offers will appear here.",
                )
            }
        } else {
            items(
                items = pendingRequests,
                key = { it.id },
                contentType = { "driver_request_card" },
            ) { req ->
                val ride = state.ridesById[req.rideId]
                DriverRequestCard(
                    booking = req,
                    ride = ride,
                    onAccept = { onAcceptRequest(req.id) },
                    onReject = { requestToReject = req },
                    onNegotiate = { onOpenNegotiation(req.id) },
                )
            }
        }

        item(key = "upcoming_rides_header", contentType = "section_header") {
            SectionHeader(title = "Your upcoming rides (${driverRides.size})")
        }

        if (driverRides.isEmpty()) {
            item(key = "rides_empty", contentType = "empty_state") {
                EmptyStateView(
                    icon = Icons.Rounded.DirectionsCar,
                    title = "No rides published yet",
                    subtitle = "Publish your upcoming route so verified passengers can request seats.",
                    actionLabel = "Publish a ride",
                    onAction = onPublishRideClick,
                )
            }
        } else {
            items(
                items = driverRides,
                key = { it.id },
                contentType = { "ride_card" },
            ) { ride ->
                val clickHandler = remember(ride.id, onRideClick) { { onRideClick(ride.id) } }
                RideResultCard(
                    ride = ride,
                    driver = state.driversById[ride.driverId],
                    vehicle = state.vehiclesById[ride.vehicleId],
                    onClick = clickHandler,
                    elevated = false,
                )
            }
        }

        item(key = "driver_bottom_spacer", contentType = "spacer") { Spacer(Modifier.height(AppSpacing.s12)) }
    }

    requestToReject?.let { req ->
        RaahiConfirmDialog(
            title = "Decline passenger request?",
            message = "Decline ${req.passengerName}'s request for ${Formatters.formatSeats(req.seatsRequested)}?",
            confirmLabel = "Decline",
            isDestructive = true,
            onConfirm = {
                onRejectRequest(req.id)
                requestToReject = null
            },
            onDismiss = { requestToReject = null },
        )
    }
}

@Composable
private fun DriverStatCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    RaahiCard(elevated = false, modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.s12)) {
            Text(
                text = value,
                style = AppTextStyles.sectionTitle,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(AppSpacing.s4))
            Text(
                text = label,
                style = AppTextStyles.caption,
                color = AppColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun DriverRequestCard(
    booking: Booking,
    ride: Ride?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onNegotiate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOffer = booking.offers.lastOrNull()
    val offeredFare = latestOffer?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr
    val isLowerOffer = offeredFare < booking.listedFarePerSeatPkr
    val offeredFareFormatted = remember(offeredFare) { "${Formatters.formatPkr(offeredFare)}/seat" }
    val listedFareFormatted = remember(booking.listedFarePerSeatPkr) { "${Formatters.formatPkr(booking.listedFarePerSeatPkr)}/seat" }
    val passengerMetaLabel = remember(booking.passengerRating, booking.seatsRequested) {
        "${booking.passengerRating} ★ • ${Formatters.formatSeats(booking.seatsRequested)}"
    }

    RaahiCard(
        elevated = false,
        modifier = modifier.fillMaxWidth().testTag("driver_request_card_${booking.id}"),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                ) {
                    AvatarBadge(
                        initials = booking.passengerInitials,
                        backgroundColor = AppColors.PrimarySoft,
                        isVerified = booking.passengerVerified,
                        size = 44.dp,
                    )
                    Spacer(Modifier.width(AppSpacing.s12))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                            Text(
                                text = booking.passengerName,
                                style = AppTextStyles.cardTitle,
                                color = AppColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            VerificationBadgeChip(isVerified = booking.passengerVerified)
                        }
                        Text(
                            text = passengerMetaLabel,
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
                BookingStatusChip(status = booking.status)
            }

            if (ride != null) {
                Text(
                    text = "${ride.originCity} (${booking.pickupPoint}) → ${ride.destinationCity} (${booking.dropoffPoint})",
                    style = AppTextStyles.body,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Passenger offer", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    Text(
                        text = offeredFareFormatted,
                        style = AppTextStyles.price,
                        color = if (isLowerOffer) AppColors.AccentOnSoft else AppColors.VerifiedText,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your listed fare", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    Text(listedFareFormatted, style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                }
            }

            if (booking.note.isNotBlank()) {
                Text(
                    text = "\"${booking.note}\"",
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                )
            }

            if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.NEGOTIATING) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = "Accept Request",
                        onClick = onAccept,
                        modifier = Modifier.testTag("driver_accept_${booking.id}"),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                    ) {
                        RaahiSecondaryButton(
                            text = "Decline",
                            onClick = onReject,
                            textColor = AppColors.Error,
                            borderColor = AppColors.Error.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f).testTag("driver_reject_${booking.id}"),
                        )
                        RaahiSecondaryButton(
                            text = "Counter",
                            onClick = onNegotiate,
                            modifier = Modifier.weight(1f).testTag("driver_counter_${booking.id}"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DriverBookingRequestsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onOpenNegotiation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var requestToReject by remember { mutableStateOf<Booking?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Booking requests",
                subtitle = "Incoming passenger offers",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        if (state.incomingDriverRequests.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Rounded.Inbox,
                    title = "No requests yet",
                    subtitle = "Passengers requesting your published rides will show up here.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(
                    items = state.incomingDriverRequests,
                    key = { it.id },
                    contentType = { "driver_request_card" },
                ) { req ->
                    DriverRequestCard(
                        booking = req,
                        ride = state.ridesById[req.rideId],
                        onAccept = { onAcceptRequest(req.id) },
                        onReject = { requestToReject = req },
                        onNegotiate = { onOpenNegotiation(req.id) },
                    )
                }
            }
        }
    }

    requestToReject?.let { req ->
        RaahiConfirmDialog(
            title = "Decline request?",
            message = "Decline ${req.passengerName}'s request for ${Formatters.formatSeats(req.seatsRequested)}?",
            confirmLabel = "Decline",
            isDestructive = true,
            onConfirm = {
                onRejectRequest(req.id)
                requestToReject = null
            },
            onDismiss = { requestToReject = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublishRideScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onAddVehicle: (String, Int, String, String, Int, LuggageCapacity) -> Vehicle,
    onPublishRide: (CreateRideRequest, (Ride) -> Unit) -> Unit,
    onDoneNavigateDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myVehicles = state.myVehicles

    var originCity by remember { mutableStateOf("Karachi") }
    var originArea by remember { mutableStateOf("Clifton") }
    var destCity by remember { mutableStateOf("Hyderabad") }
    var destArea by remember { mutableStateOf("Latifabad") }
    var optionalStop by remember { mutableStateOf("Shahrah-e-Faisal") }

    var dateOffset by remember { mutableIntStateOf(1) }
    val timeSlots = listOf("07:30 AM" to 450, "09:00 AM" to 540, "02:00 PM" to 840, "06:00 PM" to 1080)
    var selectedTimeIdx by remember { mutableIntStateOf(1) }

    var selectedVehicleId by remember(myVehicles) {
        mutableStateOf(myVehicles.firstOrNull()?.id ?: "")
    }
    val selectedVehicle = myVehicles.find { it.id == selectedVehicleId } ?: myVehicles.firstOrNull()
    val maxVehicleSeats = (selectedVehicle?.seatCapacity ?: 3).coerceAtLeast(1)

    var seats by remember(maxVehicleSeats) { mutableIntStateOf(maxVehicleSeats.coerceAtMost(3)) }
    var priceInput by remember { mutableStateOf("950") }
    var luggage by remember { mutableStateOf(LuggageCapacity.MEDIUM) }
    var acAvailable by remember { mutableStateOf(true) }
    var petsAllowed by remember { mutableStateOf(false) }
    var smokingAllowed by remember { mutableStateOf(false) }
    var musicAllowed by remember { mutableStateOf(true) }
    var chattyFriendly by remember { mutableStateOf(true) }
    var womenOnly by remember { mutableStateOf(false) }
    var audience by remember { mutableStateOf(RideAudience.ALL_USERS) }
    var notes by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var pickingField by remember { mutableStateOf<String?>(null) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var publishedSuccessRide by remember { mutableStateOf<Ride?>(null) }

    if (publishedSuccessRide != null) {
        val ride = publishedSuccessRide!!
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Ride published", onBack = onDoneNavigateDashboard) },
        ) { pad ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(AppSpacing.screenPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(shape = CircleShape, color = AppColors.VerifiedBg) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.VerifiedText,
                        modifier = Modifier.padding(AppSpacing.s20).size(48.dp),
                    )
                }
                Spacer(Modifier.height(AppSpacing.s20))
                Text("Your ride is live!", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
                Spacer(Modifier.height(AppSpacing.s8))
                Text(
                    text = "${ride.originCity} (${ride.originArea}) → ${ride.destinationCity} (${ride.destinationArea})\n${ride.departureDateLabel} at ${ride.departureTimeLabel} • ${Formatters.formatSeats(ride.availableSeats)} • ${Formatters.formatPkr(ride.pricePerSeatPkr)}/seat",
                    style = AppTextStyles.bodyCompact,
                    color = AppColors.TextSecondary,
                )
                Spacer(Modifier.height(AppSpacing.s24))
                RaahiPrimaryButton(
                    text = "Go to Driver Hub",
                    onClick = onDoneNavigateDashboard,
                    modifier = Modifier.testTag("publish_done_button"),
                )
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Publish a ride",
                subtitle = "Share your planned route",
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(AppColors.Surface)) {
                HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                ) {
                    RaahiPrimaryButton(
                        text = "Publish Ride",
                        onClick = {
                            val price = priceInput.toIntOrNull()
                            if (originCity.equals(destCity, ignoreCase = true) && originArea.equals(destArea, ignoreCase = true)) {
                                validationError = "Origin and destination must be different."
                                return@RaahiPrimaryButton
                            }
                            if (price == null || price <= 0) {
                                validationError = "Please enter a valid fare greater than PKR 0."
                                return@RaahiPrimaryButton
                            }
                            if (selectedVehicle == null) {
                                validationError = "Please add or select a vehicle."
                                return@RaahiPrimaryButton
                            }
                            val (timeLabel, minutesOfDay) = timeSlots[selectedTimeIdx]
                            val stops = if (optionalStop.isNotBlank()) {
                                listOf(RideStop(originCity, optionalStop.trim(), "En route"))
                            } else {
                                emptyList()
                            }
                            val finalAudience = if (womenOnly) RideAudience.VERIFIED_ONLY else audience
                            onPublishRide(
                                CreateRideRequest(
                                    originCity = originCity,
                                    originArea = originArea,
                                    destinationCity = destCity,
                                    destinationArea = destArea,
                                    intermediateStops = stops,
                                    dateDayOffset = dateOffset,
                                    departureDateLabel = MockDataFactory.dateLabelForOffset(dateOffset),
                                    departureTimeLabel = timeLabel,
                                    departureMinutesOfDay = minutesOfDay,
                                    durationMinutes = if (originCity == destCity) 55 else 165,
                                    vehicleId = selectedVehicle.id,
                                    availableSeats = seats,
                                    pricePerSeatPkr = price,
                                    preferences = RidePreferences(
                                        acAvailable = acAvailable,
                                        petsAllowed = petsAllowed,
                                        smokingAllowed = smokingAllowed,
                                        musicAllowed = musicAllowed,
                                        chattyFriendly = chattyFriendly,
                                        womenOnly = womenOnly,
                                        luggageCapacity = luggage,
                                    ),
                                    audience = finalAudience,
                                    notes = notes,
                                )
                            ) { created ->
                                publishedSuccessRide = created
                            }
                        },
                        modifier = Modifier.testTag("publish_submit_button"),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            if (validationError != null) {
                RaahiCard(containerColor = AppColors.SosBg, borderColor = AppColors.Error.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(AppSpacing.s8))
                        Text(validationError!!, style = AppTextStyles.caption, color = AppColors.Error)
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    Text("Route", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Surface(
                        onClick = { pickingField = "origin" },
                        shape = RoundedCornerShape(AppSpacing.radiusInput),
                        color = AppColors.Background,
                        border = BorderStroke(1.dp, AppColors.Border),
                        modifier = Modifier.fillMaxWidth().testTag("publish_origin_field"),
                    ) {
                        Column(Modifier.padding(AppSpacing.s12)) {
                            Text("Origin", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            Text("$originCity • $originArea", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        }
                    }
                    Surface(
                        onClick = { pickingField = "destination" },
                        shape = RoundedCornerShape(AppSpacing.radiusInput),
                        color = AppColors.Background,
                        border = BorderStroke(1.dp, AppColors.Border),
                        modifier = Modifier.fillMaxWidth().testTag("publish_destination_field"),
                    ) {
                        Column(Modifier.padding(AppSpacing.s12)) {
                            Text("Destination", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            Text("$destCity • $destArea", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        }
                    }
                    RaahiTextField(
                        value = optionalStop,
                        onValueChange = { optionalStop = it },
                        label = "Optional stop along the way",
                        placeholder = "e.g., Shahrah-e-Faisal",
                    )
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Date & departure time", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        (1..4).forEach { offset ->
                            FilterChip(
                                selected = dateOffset == offset,
                                onClick = { dateOffset = offset },
                                label = { Text(MockDataFactory.dateLabelForOffset(offset), style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        timeSlots.forEachIndexed { idx, (label, _) ->
                            FilterChip(
                                selected = selectedTimeIdx == idx,
                                onClick = { selectedTimeIdx = idx },
                                label = { Text(label, style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Vehicle & seats", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                        TextButton(onClick = { showAddVehicleDialog = true }) {
                            Text("+ Add vehicle", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                        }
                    }
                    myVehicles.forEach { vehicle ->
                        val isSelected = vehicle.id == selectedVehicle?.id
                        Surface(
                            onClick = {
                                selectedVehicleId = vehicle.id
                                if (seats > vehicle.seatCapacity) seats = vehicle.seatCapacity
                            },
                            shape = RoundedCornerShape(AppSpacing.radiusInput),
                            color = if (isSelected) AppColors.PrimarySoft else AppColors.Background,
                            border = BorderStroke(1.dp, if (isSelected) AppColors.Primary else AppColors.Border),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(AppSpacing.s12),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary))
                                Spacer(Modifier.width(AppSpacing.s8))
                                Column {
                                    Text(vehicle.makeModel, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                                    Text(
                                        "${vehicle.color} • ${vehicle.plateNumber} • Max ${vehicle.seatCapacity} seats",
                                        style = AppTextStyles.caption,
                                        color = AppColors.TextSecondary,
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Available seats", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            Text("Max $maxVehicleSeats for selected vehicle", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                        SeatQuantityStepper(
                            seats = seats,
                            minSeats = 1,
                            maxSeats = maxVehicleSeats,
                            onSeatsChange = { seats = it },
                        )
                    }

                    RaahiTextField(
                        value = priceInput,
                        onValueChange = {
                            priceInput = it.filter { ch -> ch.isDigit() }
                            validationError = null
                        },
                        label = "Price per seat (PKR)",
                        prefixText = "PKR ",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        fieldTestTag = "publish_price_input",
                    )
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    Text("Luggage & audience", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        LuggageCapacity.entries.forEach { cap ->
                            FilterChip(
                                selected = luggage == cap,
                                onClick = { luggage = cap },
                                label = { Text(cap.label, style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }

                    val effectiveAudience = if (womenOnly) RideAudience.VERIFIED_ONLY else audience
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        FilterChip(
                            selected = effectiveAudience == RideAudience.VERIFIED_ONLY,
                            onClick = { if (!womenOnly) audience = RideAudience.VERIFIED_ONLY },
                            enabled = !womenOnly,
                            label = { Text("Verified Only", style = AppTextStyles.chip, maxLines = 1) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("publish_audience_verified"),
                        )
                        FilterChip(
                            selected = effectiveAudience == RideAudience.ALL_USERS,
                            onClick = { if (!womenOnly) audience = RideAudience.ALL_USERS },
                            enabled = !womenOnly,
                            label = { Text("All (Verified + Basic)", style = AppTextStyles.chip, maxLines = 1) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("publish_audience_all"),
                        )
                    }
                    if (womenOnly) {
                        Text(
                            text = "Female-Only is ON — audience is locked to Verified Only.",
                            style = AppTextStyles.caption,
                            color = AppColors.FemaleOnlyText,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        FilterChip(selected = acAvailable, onClick = { acAvailable = !acAvailable }, label = { Text("AC available", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(selected = musicAllowed, onClick = { musicAllowed = !musicAllowed }, label = { Text("Music OK", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(selected = chattyFriendly, onClick = { chattyFriendly = !chattyFriendly }, label = { Text(if (chattyFriendly) "Chatty" else "Silent ride", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(selected = petsAllowed, onClick = { petsAllowed = !petsAllowed }, label = { Text("Pets OK", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(selected = smokingAllowed, onClick = { smokingAllowed = !smokingAllowed }, label = { Text("Smoking OK", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(
                            selected = womenOnly,
                            onClick = {
                                womenOnly = !womenOnly
                                if (womenOnly) audience = RideAudience.VERIFIED_ONLY
                            },
                            label = { Text("Female-Only", style = AppTextStyles.chip, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.FemaleOnlyBg,
                                selectedLabelColor = AppColors.FemaleOnlyText,
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("publish_female_only_chip"),
                        )
                    }
                    RaahiTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Trip note (optional)",
                        placeholder = "Pickup instructions, motorway stops, etc.",
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.s16))
        }
    }

    if (pickingField != null) {
        CityPickerSheet(
            title = if (pickingField == "origin") "Origin city & area" else "Destination city & area",
            allowAnyOption = false,
            onSelect = { city, area ->
                if (pickingField == "origin") {
                    originCity = city
                    originArea = area
                } else {
                    destCity = city
                    destArea = area
                }
                validationError = null
                pickingField = null
            },
            onDismiss = { pickingField = null },
        )
    }

    if (showAddVehicleDialog) {
        AddVehicleDialog(
            onAdd = { make, year, color, plate, cap, lug ->
                val v = onAddVehicle(make, year, color, plate, cap, lug)
                selectedVehicleId = v.id
                seats = seats.coerceAtMost(v.seatCapacity)
                showAddVehicleDialog = false
            },
            onDismiss = { showAddVehicleDialog = false },
        )
    }
}

@Composable
fun AddVehicleDialog(
    onAdd: (makeModel: String, year: Int, color: String, plate: String, seats: Int, luggage: LuggageCapacity) -> Unit,
    onDismiss: () -> Unit,
) {
    var makeModel by remember { mutableStateOf("Toyota Yaris 2023") }
    var color by remember { mutableStateOf("Attitude Black") }
    var plate by remember { mutableStateOf("KHI-23-4190") }
    var seats by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppSpacing.radiusSheet),
        containerColor = AppColors.Surface,
        title = { Text("Add mock vehicle", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                RaahiTextField(value = makeModel, onValueChange = { makeModel = it }, label = "Make & model")
                RaahiTextField(value = color, onValueChange = { color = it }, label = "Color")
                RaahiTextField(value = plate, onValueChange = { plate = it }, label = "Plate number")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Passenger seats", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    SeatQuantityStepper(seats = seats, minSeats = 1, maxSeats = 6, onSeatsChange = { seats = it })
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                RaahiPrimaryButton(
                    text = "Save Vehicle",
                    onClick = {
                        if (makeModel.isNotBlank()) {
                            onAdd(makeModel, 2023, color, plate, seats, LuggageCapacity.MEDIUM)
                        }
                    },
                )
                RaahiSecondaryButton(text = "Cancel", onClick = onDismiss)
            }
        },
    )
}
