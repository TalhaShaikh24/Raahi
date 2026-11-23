package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
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
                        text = "Offer a ride",
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
                            text = "Chat",
                            icon = Icons.Rounded.Forum,
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

    // Essentials
    var originCity by remember { mutableStateOf("Karachi") }
    var originArea by remember { mutableStateOf("Clifton") }
    var destCity by remember { mutableStateOf("Hyderabad") }
    var destArea by remember { mutableStateOf("Latifabad") }
    var dateOffset by remember { mutableIntStateOf(1) }
    val timeSlots = remember { listOf("07:30 AM" to 450, "09:00 AM" to 540, "02:00 PM" to 840, "06:00 PM" to 1080) }
    var selectedTimeIdx by remember { mutableIntStateOf(1) }
    var selectedVehicleId by remember(myVehicles) {
        mutableStateOf(myVehicles.firstOrNull { it.isActive }?.id ?: myVehicles.firstOrNull()?.id ?: "")
    }
    val selectedVehicle = myVehicles.find { it.id == selectedVehicleId } ?: myVehicles.firstOrNull()
    val maxVehicleSeats = (selectedVehicle?.seatCapacity ?: 3).coerceAtLeast(1)
    var seats by remember(maxVehicleSeats) { mutableIntStateOf(maxVehicleSeats.coerceAtMost(3)) }
    var price by remember { mutableIntStateOf(950) }

    // Optional extras (folded away under "More options")
    var showMoreOptions by remember { mutableStateOf(false) }
    var optionalStop by remember { mutableStateOf("") }
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
    var swapRotation by remember { mutableFloatStateOf(0f) }
    val animatedSwapRotation by animateFloatAsState(swapRotation, tween(RaahiMotion.MEDIUM), label = "swapRotation")

    val suggestedPrice = if (originCity.equals(destCity, ignoreCase = true)) 400 else 950

    publishedSuccessRide?.let { ride ->
        PublishSuccessView(ride = ride, onDone = onDoneNavigateDashboard)
        return
    }

    fun publish() {
        if (originCity.equals(destCity, ignoreCase = true) && originArea.equals(destArea, ignoreCase = true)) {
            validationError = "Pick a different destination."
            return
        }
        if (price <= 0) {
            validationError = "Set a price above PKR 0."
            return
        }
        val vehicle = selectedVehicle
        if (vehicle == null) {
            validationError = "Add your car first."
            showAddVehicleDialog = true
            return
        }
        val (timeLabel, minutesOfDay) = timeSlots[selectedTimeIdx]
        val stops = if (optionalStop.isNotBlank()) listOf(RideStop(originCity, optionalStop.trim(), "En route")) else emptyList()
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
                vehicleId = vehicle.id,
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
                audience = if (womenOnly) RideAudience.VERIFIED_ONLY else audience,
                notes = notes,
            )
        ) { created -> publishedSuccessRide = created }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Offer a ride",
                subtitle = "Just 3 quick steps",
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(AppColors.Surface)) {
                HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                ) {
                    AnimatedVisibility(visible = validationError != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(AppSpacing.s8))
                            Text(validationError.orEmpty(), style = AppTextStyles.caption, color = AppColors.Error)
                        }
                    }
                    RaahiPrimaryButton(
                        text = "Publish ride",
                        icon = Icons.Rounded.RocketLaunch,
                        onClick = ::publish,
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
            // 1. Route
            PublishStepCard(number = 1, title = "Where are you going?", modifier = Modifier.enterAnimation(index = 0)) {
                Box {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                        RoutePointRow(
                            label = "From",
                            value = "$originArea, $originCity",
                            dotColor = AppColors.Primary,
                            onClick = { pickingField = "origin" },
                            testTag = "publish_origin_field",
                        )
                        RoutePointRow(
                            label = "To",
                            value = "$destArea, $destCity",
                            dotColor = AppColors.Accent,
                            onClick = { pickingField = "destination" },
                            testTag = "publish_destination_field",
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            val c = originCity
                            val a = originArea
                            originCity = destCity
                            originArea = destArea
                            destCity = c
                            destArea = a
                            swapRotation += 180f
                            validationError = null
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = AppColors.Surface, contentColor = AppColors.Primary),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = AppSpacing.s8)
                            .shadow(4.dp, CircleShape)
                            .size(40.dp)
                            .testTag("publish_swap_button"),
                    ) {
                        Icon(
                            Icons.Rounded.SwapVert,
                            contentDescription = "Swap from and to",
                            modifier = Modifier.rotate(animatedSwapRotation),
                        )
                    }
                }
            }

            // 2. When
            PublishStepCard(number = 2, title = "When are you leaving?", modifier = Modifier.enterAnimation(index = 1)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                ) {
                    (1..4).forEach { offset ->
                        SelectablePill(
                            text = MockDataFactory.dateLabelForOffset(offset),
                            selected = dateOffset == offset,
                            onClick = { dateOffset = offset },
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                ) {
                    timeSlots.forEachIndexed { idx, (label, _) ->
                        SelectablePill(
                            text = label,
                            selected = selectedTimeIdx == idx,
                            onClick = { selectedTimeIdx = idx },
                            icon = Icons.Rounded.Schedule,
                        )
                    }
                }
            }

            // 3. Seats & price
            PublishStepCard(number = 3, title = "Seats & price", modifier = Modifier.enterAnimation(index = 2)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Empty seats", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        Text(
                            text = selectedVehicle?.let { "${it.makeModel} • max $maxVehicleSeats" } ?: "Add your car below",
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    SeatQuantityStepper(seats = seats, minSeats = 1, maxSeats = maxVehicleSeats, onSeatsChange = { seats = it })
                }
                HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Price per seat", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        Text(
                            text = "Use suggested ${Formatters.formatPkr(suggestedPrice)}",
                            style = AppTextStyles.caption,
                            color = AppColors.Primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { price = suggestedPrice }
                                .padding(vertical = 2.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.s4)) {
                        FilledTonalIconButton(
                            onClick = {
                                price = (price - 50).coerceAtLeast(50)
                                validationError = null
                            },
                            modifier = Modifier.size(40.dp).testTag("publish_price_minus"),
                        ) { Icon(Icons.Rounded.Remove, contentDescription = "Lower price") }
                        Box(modifier = Modifier.widthIn(min = 92.dp), contentAlignment = Alignment.Center) {
                            AnimatedPkrText(amount = price, style = AppTextStyles.price, color = AppColors.TextPrimary)
                        }
                        FilledTonalIconButton(
                            onClick = {
                                price += 50
                                validationError = null
                            },
                            modifier = Modifier.size(40.dp).testTag("publish_price_plus"),
                        ) { Icon(Icons.Rounded.Add, contentDescription = "Raise price") }
                    }
                }
                // Earnings preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppGradients.BrandHorizontal)
                        .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12),
                ) {
                    Icon(Icons.Rounded.Savings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(AppSpacing.s12))
                    Text("You could earn up to", style = AppTextStyles.bodyCompact, color = Color.White, modifier = Modifier.weight(1f))
                    AnimatedPkrText(amount = price * seats, style = AppTextStyles.price, color = Color.White)
                }
            }

            // Car prompt (only shown up front when the driver has none yet)
            if (myVehicles.isEmpty()) {
                RaahiCard(
                    onClick = { showAddVehicleDialog = true },
                    containerColor = AppColors.AccentSoft,
                    borderColor = AppColors.Accent.copy(alpha = 0.5f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DirectionsCar, contentDescription = null, tint = AppColors.AccentOnSoft)
                        Spacer(Modifier.width(AppSpacing.s12))
                        Text("Add your car to publish", style = AppTextStyles.cardTitle, color = AppColors.AccentOnSoft, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = AppColors.AccentOnSoft)
                    }
                }
            }

            // More options
            val chevron by animateFloatAsState(if (showMoreOptions) 180f else 0f, tween(RaahiMotion.MEDIUM), label = "moreOptionsChevron")
            RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth().enterAnimation(index = 3)) {
                Column {
                    RaahiListRow(
                        icon = Icons.Rounded.Tune,
                        title = "More options",
                        subtitle = if (showMoreOptions) null else "Car, stops, luggage, preferences (optional)",
                        onClick = { showMoreOptions = !showMoreOptions },
                        iconTint = AppColors.TextPrimary,
                        iconBg = AppColors.SurfaceSubtle,
                        trailing = {
                            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.rotate(chevron))
                        },
                        testTag = "publish_more_options_toggle",
                    )
                    AnimatedVisibility(
                        visible = showMoreOptions,
                        enter = expandVertically(tween(RaahiMotion.MEDIUM)) + fadeIn(),
                        exit = shrinkVertically(tween(RaahiMotion.MEDIUM)) + fadeOut(),
                    ) {
                        PublishMoreOptions(
                            vehicles = myVehicles,
                            selectedVehicleId = selectedVehicle?.id,
                            onSelectVehicle = { vehicle ->
                                selectedVehicleId = vehicle.id
                                if (seats > vehicle.seatCapacity) seats = vehicle.seatCapacity
                            },
                            onAddVehicle = { showAddVehicleDialog = true },
                            optionalStop = optionalStop,
                            onStopChange = { optionalStop = it },
                            price = price,
                            onPriceChange = {
                                price = it
                                validationError = null
                            },
                            luggage = luggage,
                            onLuggageChange = { luggage = it },
                            acAvailable = acAvailable,
                            onToggleAc = { acAvailable = !acAvailable },
                            musicAllowed = musicAllowed,
                            onToggleMusic = { musicAllowed = !musicAllowed },
                            chattyFriendly = chattyFriendly,
                            onToggleChatty = { chattyFriendly = !chattyFriendly },
                            petsAllowed = petsAllowed,
                            onTogglePets = { petsAllowed = !petsAllowed },
                            smokingAllowed = smokingAllowed,
                            onToggleSmoking = { smokingAllowed = !smokingAllowed },
                            womenOnly = womenOnly,
                            onToggleWomenOnly = {
                                womenOnly = !womenOnly
                                if (womenOnly) audience = RideAudience.VERIFIED_ONLY
                            },
                            audience = if (womenOnly) RideAudience.VERIFIED_ONLY else audience,
                            onAudienceChange = { if (!womenOnly) audience = it },
                            notes = notes,
                            onNotesChange = { notes = it },
                        )
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.s8))
        }
    }

    if (pickingField != null) {
        CityPickerSheet(
            title = if (pickingField == "origin") "Leaving from" else "Going to",
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
                validationError = null
            },
            onDismiss = { showAddVehicleDialog = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublishMoreOptions(
    vehicles: List<Vehicle>,
    selectedVehicleId: String?,
    onSelectVehicle: (Vehicle) -> Unit,
    onAddVehicle: () -> Unit,
    optionalStop: String,
    onStopChange: (String) -> Unit,
    price: Int,
    onPriceChange: (Int) -> Unit,
    luggage: LuggageCapacity,
    onLuggageChange: (LuggageCapacity) -> Unit,
    acAvailable: Boolean,
    onToggleAc: () -> Unit,
    musicAllowed: Boolean,
    onToggleMusic: () -> Unit,
    chattyFriendly: Boolean,
    onToggleChatty: () -> Unit,
    petsAllowed: Boolean,
    onTogglePets: () -> Unit,
    smokingAllowed: Boolean,
    onToggleSmoking: () -> Unit,
    womenOnly: Boolean,
    onToggleWomenOnly: () -> Unit,
    audience: RideAudience,
    onAudienceChange: (RideAudience) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = AppSpacing.s16, end = AppSpacing.s16, bottom = AppSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
    ) {
        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Car", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            TextButton(onClick = onAddVehicle) {
                Text("+ Add car", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
        ) {
            vehicles.forEach { vehicle ->
                SelectablePill(
                    text = "${vehicle.makeModel} (${vehicle.seatCapacity} seats)",
                    selected = vehicle.id == selectedVehicleId,
                    onClick = { onSelectVehicle(vehicle) },
                    icon = Icons.Rounded.DirectionsCar,
                )
            }
        }
        RaahiTextField(
            value = optionalStop,
            onValueChange = onStopChange,
            label = "Stop along the way",
            placeholder = "e.g. Shahrah-e-Faisal",
        )
        RaahiTextField(
            value = price.toString(),
            onValueChange = { txt -> txt.filter { it.isDigit() }.take(6).toIntOrNull()?.let(onPriceChange) },
            label = "Exact price per seat",
            prefixText = "PKR ",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            fieldTestTag = "publish_price_input",
        )
        Text("Luggage space", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
        ) {
            LuggageCapacity.entries.forEach { cap ->
                SelectablePill(text = cap.label, selected = luggage == cap, onClick = { onLuggageChange(cap) })
            }
        }
        Text("Ride preferences", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
        ) {
            SelectablePill(text = "AC", selected = acAvailable, onClick = onToggleAc, icon = Icons.Rounded.AcUnit)
            SelectablePill(text = "Music", selected = musicAllowed, onClick = onToggleMusic, icon = Icons.Rounded.MusicNote)
            SelectablePill(text = if (chattyFriendly) "Chatty" else "Quiet ride", selected = true, onClick = onToggleChatty, icon = Icons.Rounded.Forum)
            SelectablePill(text = "Pets OK", selected = petsAllowed, onClick = onTogglePets, icon = Icons.Rounded.Pets)
            SelectablePill(text = "Smoking OK", selected = smokingAllowed, onClick = onToggleSmoking, icon = Icons.Rounded.SmokingRooms)
            SelectablePill(
                text = "Women only",
                selected = womenOnly,
                onClick = onToggleWomenOnly,
                icon = Icons.Rounded.Woman,
                selectedColor = AppColors.FemaleOnlyText,
                modifier = Modifier.testTag("publish_female_only_chip"),
            )
        }
        Text("Who can book?", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
            SelectablePill(
                text = "Everyone",
                selected = audience == RideAudience.ALL_USERS,
                onClick = { onAudienceChange(RideAudience.ALL_USERS) },
                modifier = Modifier.testTag("publish_audience_all"),
            )
            SelectablePill(
                text = "Verified only",
                selected = audience == RideAudience.VERIFIED_ONLY,
                onClick = { onAudienceChange(RideAudience.VERIFIED_ONLY) },
                icon = Icons.Rounded.VerifiedUser,
                modifier = Modifier.testTag("publish_audience_verified"),
            )
        }
        if (womenOnly) {
            Text(
                text = "Women-only rides are always Verified only.",
                style = AppTextStyles.caption,
                color = AppColors.FemaleOnlyText,
            )
        }
        RaahiTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Note for passengers",
            placeholder = "Pickup instructions, stops, etc.",
            singleLine = false,
            minLines = 2,
            maxLines = 4,
        )
    }
}

@Composable
private fun PublishStepCard(
    number: Int,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RaahiCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppGradients.Brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$number", style = AppTextStyles.chip, color = Color.White)
                }
                Spacer(Modifier.width(AppSpacing.s12))
                Text(title, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            }
            content()
        }
    }
}

@Composable
private fun RoutePointRow(
    label: String,
    value: String,
    dotColor: Color,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppSpacing.radiusInput))
            .background(AppColors.Background)
            .clickable(onClick = onClick)
            .padding(start = AppSpacing.s12, top = AppSpacing.s12, bottom = AppSpacing.s12, end = 60.dp)
            .testTag(testTag),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.25f))
                .padding(3.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(AppSpacing.s12))
        Column {
            Text(label, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            Text(value, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Rounded choice pill with an animated selected state. */
@Composable
fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    selectedColor: Color = AppColors.Primary,
) {
    val bg by animateColorAsState(if (selected) selectedColor else AppColors.Surface, tween(RaahiMotion.SHORT), label = "pillBg")
    val fg by animateColorAsState(if (selected) Color.White else AppColors.TextPrimary, tween(RaahiMotion.SHORT), label = "pillFg")
    val border by animateColorAsState(if (selected) selectedColor else AppColors.Border, tween(RaahiMotion.SHORT), label = "pillBorder")
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AppTextStyles.chip, color = fg, maxLines = 1)
    }
}

@Composable
private fun PublishSuccessView(ride: Ride, onDone: () -> Unit) {
    val checkScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        checkScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.SoftMint)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(AppSpacing.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                PulsingDot(color = AppColors.Primary.copy(alpha = 0.35f), size = 60.dp)
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = checkScale.value
                            scaleY = checkScale.value
                        }
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AppGradients.Brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
            }
            Spacer(Modifier.height(AppSpacing.s24))
            Text("Your ride is live!", style = AppTextStyles.display, color = AppColors.TextPrimary, modifier = Modifier.enterAnimation(index = 2))
            Spacer(Modifier.height(AppSpacing.s8))
            Text(
                text = "${ride.originCity} → ${ride.destinationCity}\n${ride.departureDateLabel} at ${ride.departureTimeLabel} • ${Formatters.formatSeats(ride.availableSeats)} • ${Formatters.formatPkr(ride.pricePerSeatPkr)}/seat",
                style = AppTextStyles.body,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterAnimation(index = 3),
            )
            Spacer(Modifier.height(AppSpacing.s8))
            Text(
                text = "Passengers will message you in Inbox. Chat and agree on the fare there.",
                style = AppTextStyles.caption,
                color = AppColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterAnimation(index = 4),
            )
            Spacer(Modifier.height(AppSpacing.s32))
            RaahiPrimaryButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .enterAnimation(index = 5)
                    .testTag("publish_done_button"),
            )
        }
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
