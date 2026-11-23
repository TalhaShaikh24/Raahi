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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.mock.MockDataFactory
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    state: CarPoolUiState,
    onSwitchRole: (AppRole) -> Unit,
    onUpdateQuery: ((RideSearchQuery) -> RideSearchQuery) -> Unit,
    onSwapCities: () -> Unit,
    onSearchClicked: () -> Unit,
    onQuickSearch: (String, String) -> Unit,
    onRideClick: (String) -> Unit,
    onOpenSafety: () -> Unit,
    onOpenOfferRide: () -> Unit,
    onOpenNegotiation: (String) -> Unit,
    onOpenVerification: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLiveRide: (String) -> Unit,
    onOpenFavRoutes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingOrigin by remember { mutableStateOf(false) }
    var pickingDestination by remember { mutableStateOf(false) }

    val activeNegotiation = remember(state.passengerBookings) {
        state.passengerBookings.firstOrNull { it.status == BookingStatus.NEGOTIATING }
    }
    val ongoingBooking = remember(state.bookings) {
        state.bookings.firstOrNull { it.status == BookingStatus.ONGOING }
    }
    val unreadNotifCount = state.unreadNotificationCount
    val upcomingRides = state.upcomingExploreRides

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
    ) {
        // Gradient hero: greeting, notifications and mode switch
        item(key = "explore_header", contentType = "header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .enterAnimation(index = 0)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppGradients.Brand),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-40).dp)
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                )
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = AppSpacing.s16, bottom = AppSpacing.s8)
                        .size(72.dp)
                        .floating(amplitude = 4.dp),
                )
                Column(modifier = Modifier.padding(AppSpacing.s20)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleModePill(
                            activeRole = state.user.activeRole,
                            onToggle = onSwitchRole,
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("explore_notifications_button"),
                        ) {
                            if (unreadNotifCount > 0) {
                                BadgedBox(badge = { Badge(containerColor = AppColors.Accent, contentColor = AppColors.TextPrimary) { Text("$unreadNotifCount") } }) {
                                    Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.s16))
                    Text(
                        text = "Assalam-o-Alaikum, ${state.user.name.substringBefore(" ")} 👋",
                        style = AppTextStyles.screenTitle,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Where are you heading today?",
                        style = AppTextStyles.body,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(AppSpacing.s12))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onOpenVerification)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("explore_verification_badge"),
                    ) {
                        Icon(
                            imageVector = if (state.user.isEffectivelyVerified) Icons.Rounded.Verified else Icons.Rounded.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (state.user.isEffectivelyVerified) state.user.effectiveVerificationLevel.label else "Not verified • Tap to verify",
                            style = AppTextStyles.chip,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Ongoing Live Ride banner
        if (ongoingBooking != null) {
            val ride = state.ridesById[ongoingBooking.rideId]
            item(key = "explore_ongoing_banner", contentType = "banner") {
                RaahiCard(
                    onClick = { onOpenLiveRide(ongoingBooking.id) },
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary,
                    elevated = false,
                    modifier = Modifier.fillMaxWidth().testTag("explore_live_ride_banner"),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AppColors.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ongoing Ride • Live Safety Active", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                            Text(
                                text = "${ride?.originCity ?: ""} → ${ride?.destinationCity ?: ""} • Tap for SOS & Check-in",
                                style = AppTextStyles.caption,
                                color = AppColors.PrimaryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AppColors.PrimaryDark)
                    }
                }
            }
        }

        // ONE Clean Unified White Search Card
        item(key = "explore_search_card", contentType = "search_card") {
            RaahiCard(modifier = Modifier.fillMaxWidth().enterAnimation(index = 1)) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
                    Text(
                        text = "Where are you going?",
                        style = AppTextStyles.sectionTitle,
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = "Find planned carpools across Pakistan",
                        style = AppTextStyles.caption,
                        color = AppColors.TextSecondary,
                    )
                    Spacer(Modifier.height(AppSpacing.s16))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppSpacing.radiusInput))
                                .background(AppColors.Background),
                        ) {
                            SearchLocationRow(
                                label = "From",
                                value = state.searchQuery.originCity.ifBlank { "Any origin city" },
                                icon = Icons.Rounded.TripOrigin,
                                iconTint = AppColors.Primary,
                                onClick = { pickingOrigin = true },
                                testTag = "explore_origin_field",
                            )
                            HorizontalDivider(
                                color = AppColors.Border,
                                thickness = 1.dp,
                                modifier = Modifier.padding(start = 44.dp, end = 52.dp),
                            )
                            SearchLocationRow(
                                label = "To",
                                value = state.searchQuery.destinationCity.ifBlank { "Any destination" },
                                icon = Icons.Rounded.LocationOn,
                                iconTint = AppColors.Error,
                                onClick = { pickingDestination = true },
                                testTag = "explore_destination_field",
                            )
                        }

                        Surface(
                            onClick = onSwapCities,
                            shape = CircleShape,
                            color = AppColors.Surface,
                            border = BorderStroke(1.dp, AppColors.Border),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = AppSpacing.s12)
                                .size(40.dp)
                                .testTag("explore_swap_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.SwapVert,
                                    contentDescription = "Swap origin and destination",
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = AppColors.Border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AppSpacing.s12),
                    )

                    Text("Travel date", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(AppSpacing.s8))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        val dateOptions = listOf(
                            null to "Any date",
                            0 to "Today",
                            1 to "Tomorrow",
                            2 to MockDataFactory.dateLabelForOffset(2),
                            3 to MockDataFactory.dateLabelForOffset(3),
                        )
                        dateOptions.forEach { (offset, label) ->
                            SelectablePill(
                                text = label,
                                selected = state.searchQuery.dateDayOffset == offset,
                                onClick = { onUpdateQuery { it.copy(dateDayOffset = offset) } },
                            )
                        }
                    }

                    HorizontalDivider(
                        color = AppColors.Border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AppSpacing.s12),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AirlineSeatReclineNormal,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(AppSpacing.s8))
                            Text("Seats needed", style = AppTextStyles.body, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        }
                        SeatQuantityStepper(
                            seats = state.searchQuery.minSeats,
                            minSeats = 1,
                            maxSeats = 4,
                            onSeatsChange = { count -> onUpdateQuery { it.copy(minSeats = count) } },
                        )
                    }

                    Spacer(Modifier.height(AppSpacing.s16))

                    RaahiPrimaryButton(
                        text = "Find rides",
                        icon = Icons.Rounded.Search,
                        onClick = onSearchClicked,
                        modifier = Modifier.testTag("explore_search_button"),
                    )
                }
            }
        }

        // Active counter-offer shortcut banner
        if (activeNegotiation != null) {
            val ride = state.ridesById[activeNegotiation.rideId]
            item(key = "explore_negotiation_banner", contentType = "banner") {
                RaahiCard(
                    onClick = { onOpenNegotiation(activeNegotiation.id) },
                    containerColor = AppColors.AccentSoft,
                    borderColor = AppColors.Accent,
                    elevated = false,
                    modifier = Modifier.fillMaxWidth().testTag("explore_active_negotiation_banner"),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Handshake, contentDescription = null, tint = AppColors.AccentOnSoft, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("New fare offer — reply in chat", style = AppTextStyles.cardTitle, color = AppColors.AccentOnSoft)
                            Text(
                                text = "${ride?.originCity ?: ""} → ${ride?.destinationCity ?: ""} • ${activeNegotiation.offerExpiryLabel}",
                                style = AppTextStyles.caption,
                                color = AppColors.AccentOnSoft,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Open negotiation", tint = AppColors.AccentOnSoft)
                    }
                }
            }
        }

        // Favourite Routes & Recent searches
        item(key = "explore_saved_routes", contentType = "chips_row") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                SectionHeader(
                    title = "Saved & recent routes",
                    actionLabel = "Manage",
                    onAction = onOpenFavRoutes,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                ) {
                    state.favouriteRoutes.forEach { fav ->
                        SuggestionChip(
                            onClick = { onQuickSearch(fav.originCity, fav.destinationCity) },
                            icon = { Icon(Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppColors.Primary) },
                            label = { Text("${fav.originCity} → ${fav.destinationCity}", style = AppTextStyles.chip, maxLines = 1) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                    MockDataFactory.quickRecentSearches.forEach { (from, to) ->
                        SuggestionChip(
                            onClick = { onQuickSearch(from, to) },
                            icon = { Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppColors.TextSecondary) },
                            label = { Text("$from → $to", style = AppTextStyles.chip, maxLines = 1) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
            }
        }

        // Publish Ride CTA + Safety Center cards
        item(key = "explore_ctas", contentType = "cta_cards") {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
                RaahiCard(
                    onClick = onOpenOfferRide,
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary.copy(alpha = 0.25f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth().testTag("explore_offer_ride_card"),
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.AddRoad, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Driving somewhere soon?", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                            Text("Publish empty seats & share fuel costs.", style = AppTextStyles.caption, color = AppColors.PrimaryDark)
                        }
                        Spacer(Modifier.width(AppSpacing.s8))
                        RaahiPill(text = "Publish", containerColor = AppColors.Primary, contentColor = Color.White)
                    }
                }

                RaahiCard(
                    onClick = onOpenSafety,
                    elevated = false,
                    modifier = Modifier.fillMaxWidth().testTag("explore_safety_card"),
                ) {
                    RaahiListRow(
                        icon = Icons.Rounded.Shield,
                        title = "Safety Center",
                        subtitle = "Trusted contacts, 3s SOS hold & Trip OTP",
                        onClick = onOpenSafety,
                    )
                }
            }
        }

        item(key = "explore_upcoming_header", contentType = "section_header") {
            SectionHeader(
                title = "Upcoming rides",
                actionLabel = "See all",
                onAction = {
                    onUpdateQuery { RideSearchQuery() }
                    onSearchClicked()
                },
            )
        }

        items(upcomingRides, key = { it.id }, contentType = { "ride_card" }) { ride: Ride ->
            val clickHandler = remember(ride.id, onRideClick) { { onRideClick(ride.id) } }
            RideResultCard(
                ride = ride,
                driver = state.driversById[ride.driverId],
                vehicle = state.vehiclesById[ride.vehicleId],
                onClick = clickHandler,
                elevated = false,
            )
        }

        item(key = "explore_bottom_spacer", contentType = "spacer") { Spacer(Modifier.height(AppSpacing.s12)) }
    }

    if (pickingOrigin) {
        CityPickerSheet(
            title = "Select origin",
            allowAnyOption = true,
            onSelect = { city, _ ->
                onUpdateQuery { it.copy(originCity = city) }
                pickingOrigin = false
            },
            onDismiss = { pickingOrigin = false },
        )
    }

    if (pickingDestination) {
        CityPickerSheet(
            title = "Select destination",
            allowAnyOption = true,
            onSelect = { city, _ ->
                onUpdateQuery { it.copy(destinationCity = city) }
                pickingDestination = false
            },
            onDismiss = { pickingDestination = false },
        )
    }
}

@Composable
private fun SearchLocationRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.s12, vertical = AppSpacing.s12)
            .padding(end = 44.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(AppSpacing.s12))
        Column {
            Text(label, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            Text(
                text = value,
                style = AppTextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
