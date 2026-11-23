package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.model.CreateBookingRequest
import com.example.model.RideAudience
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RideDetailsScreen(
    rideId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onRequestJoin: (rideId: String, offerMode: Boolean, pickup: String, dropoff: String) -> Unit,
    onOpenSafety: () -> Unit,
    onOpenPublicProfile: (String) -> Unit,
    onToggleFollowDriver: (String) -> Unit,
    onOpenVerification: () -> Unit,
    onJoinWaitlist: (String) -> Unit,
    onLeaveWaitlist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ride = state.ridesById[rideId]
    if (ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Ride details", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Rounded.ErrorOutline,
                    title = "Ride not found",
                    subtitle = "This ride is no longer available in the demo list.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )
            }
        }
        return
    }

    val driver = state.driversById[ride.driverId]
    val vehicle = state.vehiclesById[ride.vehicleId]
    val driverReviews = remember(state.reviews, ride.driverId) {
        state.reviews.filter { it.driverId == ride.driverId }
    }
    val isFollowingDriver = driver != null && driver.id in state.followedDriverIds
    val isOnWaitlist = state.waitlist.any { it.rideId == ride.id }

    val userIsVerified = state.user.isEffectivelyVerified
    val blockedByVerifiedOnly = ride.audience == RideAudience.VERIFIED_ONLY && !userIsVerified
    val driverIsUnverified = driver != null && !driver.isVerified

    var selectedPickup by remember(ride.id) { mutableStateOf(ride.stops.firstOrNull()?.area ?: ride.originArea) }
    var selectedDropoff by remember(ride.id) { mutableStateOf(ride.stops.lastOrNull()?.area ?: ride.destinationArea) }
    var selectingPickupNext by remember { mutableStateOf(true) }
    var pendingUnverifiedOfferMode by remember { mutableStateOf<Boolean?>(null) }

    fun triggerBookingAction(offerMode: Boolean) {
        if (driverIsUnverified) {
            pendingUnverifiedOfferMode = offerMode
        } else {
            onRequestJoin(ride.id, offerMode, selectedPickup, selectedDropoff)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "${ride.originCity} → ${ride.destinationCity}",
                subtitle = "${ride.departureDateLabel} • ${ride.departureTimeLabel}",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = onOpenSafety,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("ride_details_report_button"),
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = "Safety and report", tint = AppColors.TextPrimary, modifier = Modifier.size(22.dp))
                    }
                },
            )
        },
        bottomBar = {
            if (!ride.isDriverOwned) {
                Column(modifier = Modifier.background(AppColors.Surface)) {
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        if (ride.availableSeats == 0) {
                            if (isOnWaitlist) {
                                RaahiSecondaryButton(
                                    text = "On Waitlist • Leave",
                                    onClick = { onLeaveWaitlist(ride.id) },
                                    modifier = Modifier.testTag("ride_details_waitlist_button"),
                                )
                            } else {
                                RaahiPrimaryButton(
                                    text = "Join Waitlist",
                                    onClick = { onJoinWaitlist(ride.id) },
                                    modifier = Modifier.testTag("ride_details_waitlist_button"),
                                )
                            }
                        } else if (blockedByVerifiedOnly) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Verify your account to book this ride",
                                    style = AppTextStyles.caption,
                                    color = AppColors.Error,
                                    modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                                )
                                TextButton(
                                    onClick = onOpenVerification,
                                    modifier = Modifier.testTag("ride_details_verify_link"),
                                ) {
                                    Text("Get Verified", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                                }
                            }
                            RaahiPrimaryButton(
                                text = "Verified Account Required",
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.testTag("ride_details_disabled_book_button"),
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Fare per seat", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                Text(
                                    text = Formatters.formatPkr(ride.pricePerSeatPkr),
                                    style = AppTextStyles.price,
                                    color = AppColors.Primary,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                            ) {
                                RaahiSecondaryButton(
                                    text = "Make Offer",
                                    onClick = { triggerBookingAction(true) },
                                    modifier = Modifier.weight(1f).testTag("ride_details_make_offer_button"),
                                )
                                RaahiPrimaryButton(
                                    text = "Book Seat",
                                    onClick = { triggerBookingAction(false) },
                                    modifier = Modifier.weight(1.25f).testTag("ride_details_request_join_button"),
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
            // Rounded route summary & fare breakdown hero card
            item(key = "ride_hero", contentType = "hero") {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        ) {
                            RaahiPill(
                                text = ride.audience.label,
                                containerColor = if (ride.audience == RideAudience.VERIFIED_ONLY) AppColors.PrimarySoft else AppColors.SurfaceSubtle,
                                contentColor = if (ride.audience == RideAudience.VERIFIED_ONLY) AppColors.PrimaryDark else AppColors.TextSecondary,
                            )
                            if (ride.preferences.womenOnly) {
                                RaahiPill(
                                    text = "Female-Only",
                                    containerColor = AppColors.FemaleOnlyBg,
                                    contentColor = AppColors.FemaleOnlyText,
                                )
                            }
                            RaahiPill(
                                text = if (ride.availableSeats > 0) "${ride.availableSeats} seats left" else "Full (Waitlist)",
                                containerColor = if (ride.availableSeats > 0) AppColors.VerifiedBg else AppColors.SosBg,
                                contentColor = if (ride.availableSeats > 0) AppColors.VerifiedText else AppColors.Error,
                            )
                        }

                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Listed fare per seat", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                Text("Est. ${Formatters.formatDuration(ride.durationMinutes)}", style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                            }
                            Text(
                                text = Formatters.formatPkr(ride.pricePerSeatPkr),
                                style = AppTextStyles.screenTitle,
                                color = AppColors.Primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            item(key = "route_timeline", contentType = "route_timeline") {
                RouteTimelineCard(
                    stops = ride.stops,
                    selectedPickup = selectedPickup,
                    selectedDropoff = selectedDropoff,
                    onStopClick = { stop, index ->
                        if (selectingPickupNext && index < ride.stops.lastIndex) {
                            selectedPickup = stop.area
                            selectingPickupNext = false
                        } else if (index > 0) {
                            selectedDropoff = stop.area
                            selectingPickupNext = true
                        }
                    },
                )
            }

            item(key = "pickup_summary", contentType = "banner") {
                RaahiCard(
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary.copy(alpha = 0.2f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PinDrop, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(AppSpacing.s12))
                        Text(
                            text = "Pickup: $selectedPickup → Drop-off: $selectedDropoff",
                            style = AppTextStyles.bodyCompact,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (driver != null) {
                item(key = "driver_card", contentType = "driver") {
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DriverMiniProfile(
                                    driver = driver,
                                    vehicle = null,
                                    onClick = { onOpenPublicProfile(driver.id) },
                                    modifier = Modifier.weight(1f).testTag("ride_details_driver_profile"),
                                )
                                IconButton(
                                    onClick = { onToggleFollowDriver(driver.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("ride_details_follow_driver"),
                                ) {
                                    Icon(
                                        imageVector = if (isFollowingDriver) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = if (isFollowingDriver) "Unfollow driver" else "Follow driver",
                                        tint = if (isFollowingDriver) AppColors.Error else AppColors.TextSecondary,
                                    )
                                }
                            }
                            Text(driver.bio, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                                    MetaIconText(Icons.Rounded.DirectionsCar, "${driver.tripsCompleted} trips")
                                    MetaIconText(Icons.Rounded.Shield, "${driver.reliabilityScore}% reliable")
                                }
                                TextButton(onClick = { onOpenPublicProfile(driver.id) }) {
                                    Text("Public Profile", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            if (vehicle != null) {
                item(key = "vehicle_card", contentType = "vehicle") {
                    VehicleInfoCard(vehicle = vehicle)
                }
            }

            item(key = "ride_preferences", contentType = "preferences") {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Text("Ride preferences & policy", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        ) {
                            PreferenceBadge(Icons.Rounded.AcUnit, if (ride.preferences.acAvailable) "AC available" else "No AC")
                            PreferenceBadge(Icons.Rounded.Luggage, ride.preferences.luggageCapacity.label)
                            PreferenceBadge(Icons.Rounded.SmokeFree, if (ride.preferences.smokingAllowed) "Smoking OK" else "No smoking")
                            PreferenceBadge(Icons.Rounded.Pets, if (ride.preferences.petsAllowed) "Pets allowed" else "No pets")
                            PreferenceBadge(Icons.Rounded.MusicNote, if (ride.preferences.musicAllowed) "Music friendly" else "Quiet cabin")
                            PreferenceBadge(Icons.Rounded.RecordVoiceOver, if (ride.preferences.chattyFriendly) "Chatty" else "Silent ride")
                        }
                        if (ride.notes.isNotBlank()) {
                            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                            Text("Driver note: ${ride.notes}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                    }
                }
            }

            if (driverReviews.isNotEmpty()) {
                item(key = "reviews_header", contentType = "section_header") {
                    Text(
                        text = "Recent passenger reviews",
                        style = AppTextStyles.sectionTitle,
                        color = AppColors.TextPrimary,
                    )
                }
                items(driverReviews, key = { it.id }, contentType = { "review" }) { review ->
                    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(review.authorName, style = AppTextStyles.bodyCompact, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${review.rating} • ${review.dateLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                }
                            }
                            Text(review.comment, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                    }
                }
            }

            item(key = "ride_details_spacer", contentType = "spacer") { Spacer(Modifier.height(AppSpacing.s12)) }
        }
    }

    pendingUnverifiedOfferMode?.let { offerMode ->
        RaahiConfirmDialog(
            title = "Unverified Driver Notice",
            message = "This driver is not ID verified. Continue?",
            confirmLabel = "Continue",
            isDestructive = false,
            onConfirm = {
                pendingUnverifiedOfferMode = null
                onRequestJoin(ride.id, offerMode, selectedPickup, selectedDropoff)
            },
            onDismiss = { pendingUnverifiedOfferMode = null },
        )
    }
}

@Composable
private fun PreferenceBadge(
    icon: ImageVector,
    label: String,
) {
    RaahiPill(
        text = label,
        containerColor = AppColors.SurfaceSubtle,
        contentColor = AppColors.TextPrimary,
        icon = icon,
    )
}

@Composable
fun BookingRequestScreen(
    rideId: String,
    initialOfferMode: Boolean,
    pickupPoint: String,
    dropoffPoint: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSubmitBooking: (CreateBookingRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ride = state.ridesById[rideId]
    if (ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Booking request", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Ride unavailable.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    var seats by remember { mutableIntStateOf(1) }
    var useCounterOffer by remember { mutableStateOf(initialOfferMode) }
    var offerInput by remember { mutableStateOf((ride.pricePerSeatPkr - 100).coerceAtLeast(100).toString()) }
    var passengerNote by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val parsedOffer = offerInput.toIntOrNull()
    val effectivePerSeat = if (useCounterOffer) (parsedOffer ?: 0) else ride.pricePerSeatPkr
    val totalFare = effectivePerSeat * seats

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Request seats",
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("${ride.departureDateLabel} • ${ride.departureTimeLabel}", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Text("Pickup: $pickupPoint → Drop-off: $dropoffPoint", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Seats to book", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            Text("${ride.availableSeats} available", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                        SeatQuantityStepper(
                            seats = seats,
                            minSeats = 1,
                            maxSeats = ride.availableSeats.coerceAtLeast(1),
                            onSeatsChange = { seats = it },
                        )
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Fare option", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)

                    OfferChoiceTile(
                        title = "Continue at listed fare",
                        subtitle = "${Formatters.formatPkr(ride.pricePerSeatPkr)} per seat",
                        selected = !useCounterOffer,
                        onClick = { useCounterOffer = false; validationError = null },
                        testTag = "booking_listed_fare_option",
                    )

                    OfferChoiceTile(
                        title = "Offer a different price",
                        subtitle = "Propose a per-seat fare. Driver can accept, decline, or counter.",
                        selected = useCounterOffer,
                        onClick = { useCounterOffer = true },
                        testTag = "booking_counter_offer_option",
                    )

                    if (useCounterOffer) {
                        RaahiTextField(
                            value = offerInput,
                            onValueChange = { offerInput = it.filter { ch -> ch.isDigit() }; validationError = null },
                            label = "Your offer per seat (PKR)",
                            prefixText = "PKR ",
                            errorText = validationError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            fieldTestTag = "booking_offer_amount_input",
                        )
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
                    RaahiTextField(
                        value = passengerNote,
                        onValueChange = { passengerNote = it },
                        label = "Note for driver (optional)",
                        placeholder = "e.g., 1 cabin bag, flexible pickup near main road",
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                        fieldTestTag = "booking_note_input",
                    )
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    Text("Fare summary", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Driver's listed fare", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                        Text("${Formatters.formatPkr(ride.pricePerSeatPkr)} × $seats", style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                    }
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (useCounterOffer) "Proposed total" else "Total fare", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        Text(Formatters.formatPkr(totalFare), style = AppTextStyles.price, color = AppColors.Primary)
                    }
                }
            }

            RaahiPrimaryButton(
                text = if (useCounterOffer) "Send Counter-Offer" else "Confirm Booking Request",
                onClick = {
                    if (useCounterOffer) {
                        val amount = offerInput.toIntOrNull()
                        if (amount == null || amount <= 0) {
                            validationError = "Please enter a valid fare greater than PKR 0."
                            return@RaahiPrimaryButton
                        }
                    }
                    onSubmitBooking(
                        CreateBookingRequest(
                            rideId = ride.id,
                            seatsRequested = seats,
                            proposedFarePerSeatPkr = if (useCounterOffer) parsedOffer else null,
                            pickupPoint = pickupPoint,
                            dropoffPoint = dropoffPoint,
                            note = passengerNote,
                        )
                    )
                },
                modifier = Modifier.testTag("booking_confirm_request_button"),
            )
            Spacer(Modifier.height(AppSpacing.s16))
        }
    }
}

@Composable
private fun OfferChoiceTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppSpacing.radiusInput),
        color = if (selected) AppColors.PrimarySoft else AppColors.Background,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) AppColors.Primary else AppColors.Border,
        ),
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    ) {
        Row(modifier = Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary),
            )
            Spacer(Modifier.width(AppSpacing.s8))
            Column {
                Text(title, style = AppTextStyles.body, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                Text(subtitle, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
        }
    }
}
