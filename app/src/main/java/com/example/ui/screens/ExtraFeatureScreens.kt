package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.data.mock.MockDataFactory
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PaymentScreen(
    bookingId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onPay: (bookingId: String, method: String, promoCode: String?, (PaymentTransaction) -> Unit) -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.bookings.find { it.id == bookingId }
    val ride = booking?.let { state.ridesById[it.rideId] }

    var selectedMethod by remember { mutableStateOf("JazzCash") }
    var promoInput by remember { mutableStateOf("") }
    var promoApplied by remember { mutableStateOf(false) }
    var paidTx by remember { mutableStateOf<PaymentTransaction?>(null) }

    if (booking == null || ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Payment", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Booking not found.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    val perSeat = booking.agreedFarePerSeatPkr ?: booking.listedFarePerSeatPkr
    val gross = perSeat * booking.seatsRequested
    val discount = if (promoApplied) (gross / 2).coerceAtMost(250) else 0
    val net = (gross - discount).coerceAtLeast(0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Demo Payment", subtitle = "No real money or gateway", onBack = onBack) },
    ) { pad ->
        if (paidTx != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AppColors.VerifiedText, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(AppSpacing.s16))
                Text("Payment Recorded (Demo)", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
                Spacer(Modifier.height(AppSpacing.s8))
                Text("${Formatters.formatPkr(paidTx!!.amountPkr)} via ${paidTx!!.method}", style = AppTextStyles.cardTitle, color = AppColors.Primary)
                Spacer(Modifier.height(AppSpacing.s24))
                RaahiPrimaryButton(text = "View Payment History", onClick = onViewHistory)
                Spacer(Modifier.height(AppSpacing.s12))
                RaahiSecondaryButton(text = "Back to Booking", onClick = onBack)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
            ) {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Text("Select payment method", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        listOf("Cash", "JazzCash", "Easypaisa").forEach { method ->
                            val selected = selectedMethod == method
                            Surface(
                                onClick = { selectedMethod = method },
                                shape = RoundedCornerShape(AppSpacing.radiusInput),
                                color = if (selected) AppColors.PrimarySoft else AppColors.Background,
                                border = BorderStroke(1.dp, if (selected) AppColors.Primary else AppColors.Border),
                                modifier = Modifier.fillMaxWidth().testTag("payment_method_${method.lowercase()}"),
                            ) {
                                Row(modifier = Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary))
                                    Spacer(Modifier.width(AppSpacing.s8))
                                    Text(method, style = AppTextStyles.body, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                }
                            }
                        }
                    }
                }

                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        RaahiTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            label = "Promo code (try FIRST50)",
                            placeholder = "FIRST50",
                            fieldTestTag = "payment_promo_input",
                        )
                        RaahiSecondaryButton(
                            text = "Apply Promo",
                            onClick = { promoApplied = promoInput.trim().equals("FIRST50", ignoreCase = true) },
                            modifier = Modifier.testTag("payment_apply_promo_btn"),
                        )
                        if (promoApplied) {
                            Text("FIRST50 applied! Saved ${Formatters.formatPkr(discount)} (demo).", style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.VerifiedText)
                        }
                    }
                }

                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                            Text(Formatters.formatPkr(gross), style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                        }
                        if (discount > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Promo (FIRST50)", style = AppTextStyles.bodyCompact, color = AppColors.VerifiedText)
                                Text("-${Formatters.formatPkr(discount)}", style = AppTextStyles.bodyCompact, color = AppColors.VerifiedText)
                            }
                        }
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total payable", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            Text(Formatters.formatPkr(net), style = AppTextStyles.price, color = AppColors.Primary)
                        }
                    }
                }

                RaahiPrimaryButton(
                    text = "Confirm Demo Payment",
                    onClick = { onPay(booking.id, selectedMethod, if (promoApplied) "FIRST50" else null) { paidTx = it } },
                    modifier = Modifier.testTag("payment_confirm_button"),
                )
            }
        }
    }
}

@Composable
fun PaymentHistoryScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Payment history", subtitle = "Cash, JazzCash & Easypaisa (demo)", onBack = onBack) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(
                items = state.payments,
                key = { it.id },
                contentType = { "payment_tx" },
            ) { tx ->
                PaymentHistoryItem(tx = tx)
            }
        }
    }
}

@Composable
private fun PaymentHistoryItem(tx: PaymentTransaction) {
    val amountFormatted = remember(tx.amountPkr) { Formatters.formatPkr(tx.amountPkr) }
    val subtitle = remember(tx.method, tx.dateLabel, tx.promoCode) {
        "${tx.method} • ${tx.dateLabel}${tx.promoCode?.let { " • $it" } ?: ""}"
    }
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                Text(tx.routeSummary, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amountFormatted, style = AppTextStyles.price, color = AppColors.Primary)
                Text(tx.statusLabel, style = AppTextStyles.caption, color = AppColors.VerifiedText)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateReviewScreen(
    bookingId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSubmitReview: (bookingId: String, driverId: String, rating: Double, comment: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.bookings.find { it.id == bookingId }
    val ride = booking?.let { state.ridesById[it.rideId] }
    val driver = ride?.let { state.driversById[it.driverId] }

    var stars by remember { mutableIntStateOf(5) }
    var selectedTag by remember { mutableStateOf("Punctual & smooth") }
    var comment by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Rate & review", subtitle = driver?.name ?: "Trip feedback", onBack = onBack) },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("How was your ride with ${driver?.name ?: "your driver"}?", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                        (1..5).forEach { s ->
                            IconButton(onClick = { stars = s }) {
                                Icon(
                                    imageVector = if (s <= stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "$s stars",
                                    tint = AppColors.Accent,
                                    modifier = Modifier.size(34.dp),
                                )
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        listOf("Punctual & smooth", "Clean car", "Great conversation", "Safe motorway drive").forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag, style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                    RaahiTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = "Share feedback (optional)",
                        singleLine = false,
                        minLines = 3,
                    )
                }
            }
            RaahiPrimaryButton(
                text = "Submit Review",
                onClick = {
                    if (booking != null && driver != null) {
                        val finalComment = if (comment.isBlank()) selectedTag else "$selectedTag. $comment"
                        onSubmitReview(booking.id, driver.id, stars.toDouble(), finalComment)
                        onBack()
                    }
                },
                modifier = Modifier.testTag("rate_submit_button"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSave: ((AppUser) -> AppUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    val u = state.user
    var name by remember { mutableStateOf(u.name) }
    var city by remember { mutableStateOf(u.homeCity) }
    var gender by remember { mutableStateOf(u.gender) }
    var bio by remember { mutableStateOf(u.bio) }
    var emergencyName by remember { mutableStateOf(u.emergencyContactName) }
    var emergencyPhone by remember { mutableStateOf(u.emergencyContactPhone) }
    var chatty by remember { mutableStateOf(u.preferences.chattyPreferred) }
    var quiet by remember { mutableStateOf(u.preferences.quietRide) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Edit profile", onBack = onBack) },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    RaahiTextField(value = name, onValueChange = { name = it }, label = "Full name")
                    RaahiTextField(value = city, onValueChange = { city = it }, label = "Home city")
                    Text("Gender", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        listOf("Male", "Female", "Prefer not to say").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g, style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                    RaahiTextField(value = bio, onValueChange = { bio = it }, label = "Bio", singleLine = false, minLines = 2)
                    RaahiTextField(value = emergencyName, onValueChange = { emergencyName = it }, label = "Emergency contact name")
                    RaahiTextField(value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = "Emergency contact phone")
                    Text("Default vibe", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        FilterChip(selected = chatty, onClick = { chatty = true; quiet = false }, label = { Text("Chatty", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                        FilterChip(selected = quiet, onClick = { quiet = true; chatty = false }, label = { Text("Silent / Quiet", style = AppTextStyles.chip, maxLines = 1) }, shape = RoundedCornerShape(50))
                    }
                }
            }
            RaahiPrimaryButton(
                text = "Save Changes",
                onClick = {
                    onSave { curr ->
                        curr.copy(
                            name = name.ifBlank { curr.name },
                            homeCity = city.ifBlank { curr.homeCity },
                            gender = gender,
                            bio = bio,
                            emergencyContactName = emergencyName,
                            emergencyContactPhone = emergencyPhone,
                            preferences = curr.preferences.copy(chattyPreferred = chatty, quietRide = quiet),
                        )
                    }
                    onBack()
                },
                modifier = Modifier.testTag("edit_profile_save_btn"),
            )
            Spacer(Modifier.height(AppSpacing.s16))
        }
    }
}

@Composable
fun FavouriteRoutesScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSelectRoute: (origin: String, destination: String) -> Unit,
    onAddRoute: (origin: String, destination: String, label: String) -> Unit,
    onDeleteRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdd by remember { mutableStateOf(false) }
    var origin by remember { mutableStateOf("Karachi") }
    var dest by remember { mutableStateOf("Lahore") }
    var label by remember { mutableStateOf("Holiday Express") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Favourite routes",
                subtitle = "Tap any route to search rides",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { showAdd = true }) {
                        Text("+ Add", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(
                items = state.favouriteRoutes,
                key = { it.id },
                contentType = { "fav_route" },
            ) { route ->
                FavouriteRouteCard(
                    route = route,
                    onSelect = { onSelectRoute(route.originCity, route.destinationCity) },
                    onDelete = { onDeleteRoute(route.id) },
                )
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Add Favourite Route", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    RaahiTextField(value = origin, onValueChange = { origin = it }, label = "Origin city")
                    RaahiTextField(value = dest, onValueChange = { dest = it }, label = "Destination city")
                    RaahiTextField(value = label, onValueChange = { label = it }, label = "Label")
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = "Save Route",
                        onClick = {
                            if (origin.isNotBlank() && dest.isNotBlank()) {
                                onAddRoute(origin, dest, label)
                                showAdd = false
                            }
                        },
                    )
                    RaahiSecondaryButton(text = "Cancel", onClick = { showAdd = false })
                }
            },
        )
    }
}

@Composable
private fun FavouriteRouteCard(
    route: FavouriteRoute,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    RaahiCard(
        onClick = onSelect,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(AppSpacing.s12))
            Column(modifier = Modifier.weight(1f)) {
                Text("${route.originCity} → ${route.destinationCity}", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                Text(route.label, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove route", tint = AppColors.Error)
            }
        }
    }
}

@Composable
fun FavouriteDriversScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onOpenDriverProfile: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drivers = state.followedDrivers
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Favourite drivers", subtitle = "Drivers you follow", onBack = onBack) },
    ) { pad ->
        if (drivers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(icon = Icons.Rounded.FavoriteBorder, title = "No favourite drivers yet", subtitle = "Tap Follow on any driver's Public Profile or Ride Details.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(
                    items = drivers,
                    key = { it.id },
                    contentType = { "fav_driver" },
                ) { driver ->
                    FavouriteDriverCard(
                        driver = driver,
                        vehicle = state.vehiclesById[driver.vehicleId],
                        onOpenProfile = { onOpenDriverProfile(driver.id) },
                        onToggleFollow = { onToggleFollow(driver.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavouriteDriverCard(
    driver: DriverProfile,
    vehicle: Vehicle?,
    onOpenProfile: () -> Unit,
    onToggleFollow: () -> Unit,
) {
    RaahiCard(
        onClick = onOpenProfile,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            DriverMiniProfile(driver = driver, vehicle = vehicle)
            RaahiSecondaryButton(
                text = "Unfollow Driver",
                onClick = onToggleFollow,
                textColor = AppColors.Error,
                borderColor = AppColors.Error.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
fun WaitlistScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onOpenRideDetail: (String) -> Unit,
    onLeaveWaitlist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Ride waitlist", subtitle = "Full rides you are monitoring", onBack = onBack) },
    ) { pad ->
        if (state.waitlist.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(icon = Icons.Rounded.HourglassEmpty, title = "Waitlist empty", subtitle = "Join the waitlist on any full ride.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(
                    items = state.waitlist,
                    key = { it.id },
                    contentType = { "waitlist_entry" },
                ) { entry ->
                    val ride = state.ridesById[entry.rideId]
                    WaitlistEntryRow(
                        entry = entry,
                        ride = ride,
                        onOpenRideDetail = onOpenRideDetail,
                        onLeaveWaitlist = onLeaveWaitlist,
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitlistEntryRow(
    entry: WaitlistEntry,
    ride: Ride?,
    onOpenRideDetail: (String) -> Unit,
    onLeaveWaitlist: (String) -> Unit,
) {
    RaahiCard(
        onClick = { if (ride != null) onOpenRideDetail(ride.id) },
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                Text("${ride?.originCity ?: ""} → ${ride?.destinationCity ?: ""}", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                Text("${ride?.departureDateLabel ?: ""} • Joined ${entry.joinedAtLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            TextButton(onClick = { onLeaveWaitlist(entry.rideId) }) {
                Text("Leave", style = AppTextStyles.button, color = AppColors.Error, maxLines = 1)
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Notifications",
                subtitle = "${state.unreadNotificationCount} unread",
                onBack = onBack,
                actions = {
                    TextButton(onClick = onMarkAllRead, modifier = Modifier.testTag("notifications_mark_all_read")) {
                        Text("Mark all read", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(
                items = state.notifications,
                key = { it.id },
                contentType = { "notification_card" },
            ) { notif ->
                NotificationSimpleCard(
                    notif = notif,
                    onClick = { onNotificationClick(notif) },
                )
            }
        }
    }
}

@Composable
private fun NotificationSimpleCard(
    notif: AppNotification,
    onClick: () -> Unit,
) {
    RaahiCard(
        onClick = onClick,
        borderColor = if (notif.isUnread) AppColors.Primary else AppColors.Border,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(notif.title, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, modifier = Modifier.weight(1f).padding(end = AppSpacing.s8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(notif.timestampLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            Text(notif.body, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun ReferralPromoScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var promoTest by remember { mutableStateOf("") }
    var promoFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Referrals & Promos", onBack = onBack) },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            RaahiCard(
                containerColor = AppColors.PrimarySoft,
                borderColor = AppColors.Primary.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.s20), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Invite friends to Raahi", style = AppTextStyles.sectionTitle, color = AppColors.PrimaryDark)
                    Text("Share your demo referral code — no real wallet or cash payout in prototype.", style = AppTextStyles.bodyCompact, color = AppColors.PrimaryDark)
                    Surface(shape = RoundedCornerShape(AppSpacing.radiusInput), color = AppColors.Surface) {
                        Text(
                            text = "RAAHI-DANIYAL",
                            style = AppTextStyles.sectionTitle,
                            color = AppColors.Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12),
                        )
                    }
                    RaahiPrimaryButton(
                        text = "Share Referral Code",
                        onClick = { onShowMessage("Demo share link copied: raahi.pk/invite/RAAHI-DANIYAL") },
                    )
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Active Demo Promo: FIRST50", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Text("Test promo validation below or on any Payment screen for up to PKR 250 off.", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                    RaahiTextField(value = promoTest, onValueChange = { promoTest = it }, label = "Enter promo code", placeholder = "FIRST50")
                    RaahiSecondaryButton(
                        text = "Check Code",
                        onClick = {
                            promoFeedback = if (promoTest.trim().equals("FIRST50", ignoreCase = true)) {
                                "Valid! FIRST50 gives 50% off (max PKR 250) on demo Payment."
                            } else {
                                "Unknown code. Try FIRST50."
                            }
                        },
                    )
                    if (promoFeedback != null) {
                        Text(promoFeedback!!, style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.Primary)
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsDashboardScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Savings dashboard", subtitle = "Your carpooling impact (demo)", onBack = onBack) },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
                ImpactCard("PKR 14,800", "Money saved vs solo fuel", AppColors.Primary, Modifier.weight(1f))
                ImpactCard("18", "Shared rides completed", AppColors.PrimaryDark, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
                ImpactCard("1,420 km", "Distance carpooled", AppColors.Primary, Modifier.weight(1f))
                ImpactCard("184 kg", "CO2 emissions avoided", AppColors.VerifiedText, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ImpactCard(value: String, label: String, color: Color, modifier: Modifier) {
    RaahiCard(modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)) {
            Text(value, style = AppTextStyles.sectionTitle, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = AppTextStyles.caption, color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun DriverEarningsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalFormatted = remember(state.totalDriverEarningsPkr) {
        Formatters.formatPkr(state.totalDriverEarningsPkr)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Driver earnings", subtitle = "Fuel-split summary (demo)", onBack = onBack) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item(key = "earnings_summary", contentType = "earnings_summary") {
                RaahiCard(
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary.copy(alpha = 0.25f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.s20)) {
                        Text("Total demo earnings", style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.PrimaryDark)
                        Spacer(Modifier.height(AppSpacing.s4))
                        Text(totalFormatted, style = AppTextStyles.screenTitle, color = AppColors.Primary)
                    }
                }
            }
            items(
                items = state.driverEarnings,
                key = { it.id },
                contentType = { "earning_row" },
            ) { entry ->
                DriverEarningRow(entry = entry)
            }
        }
    }
}

@Composable
private fun DriverEarningRow(entry: DriverEarningEntry) {
    val amountFormatted = remember(entry.amountPkr) { Formatters.formatPkr(entry.amountPkr) }
    val subtitle = remember(entry.passengerName, entry.seats, entry.dateLabel) {
        "${entry.passengerName} • ${Formatters.formatSeats(entry.seats)} • ${entry.dateLabel}"
    }
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(AppSpacing.cardPadding), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                Text(entry.rideSummary, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            Text(amountFormatted, style = AppTextStyles.price, color = AppColors.Primary)
        }
    }
}

@Composable
fun MyVehiclesScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onAddVehicle: (String, Int, String, String, Int, LuggageCapacity) -> Vehicle,
    onUpdateVehicle: (Vehicle) -> Unit,
    onSetActive: (String) -> Unit,
    onDeleteVehicle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val myVehicles = state.myVehicles
    var showAdd by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "My vehicles",
                subtitle = "Local garage",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { showAdd = true }) {
                        Text("+ Add", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(
                items = myVehicles,
                key = { it.id },
                contentType = { "vehicle_card" },
            ) { v ->
                MyVehicleCard(
                    vehicle = v,
                    canDelete = myVehicles.size > 1,
                    onSetActive = { onSetActive(v.id) },
                    onEdit = { editingVehicle = v },
                    onDelete = { onDeleteVehicle(v.id) },
                )
            }
        }
    }

    if (showAdd) {
        AddVehicleDialog(
            onAdd = { m, y, c, p, s, l -> val created = onAddVehicle(m, y, c, p, s, l); showAdd = false; created },
            onDismiss = { showAdd = false },
        )
    }

    editingVehicle?.let { veh ->
        var model by remember { mutableStateOf(veh.makeModel) }
        var color by remember { mutableStateOf(veh.color) }
        var plate by remember { mutableStateOf(veh.plateNumber) }
        AlertDialog(
            onDismissRequest = { editingVehicle = null },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Edit vehicle", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    RaahiTextField(value = model, onValueChange = { model = it }, label = "Make & model")
                    RaahiTextField(value = color, onValueChange = { color = it }, label = "Color")
                    RaahiTextField(value = plate, onValueChange = { plate = it }, label = "Plate")
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = "Save Changes",
                        onClick = {
                            onUpdateVehicle(veh.copy(makeModel = model, color = color, plateNumber = plate))
                            editingVehicle = null
                        },
                    )
                    RaahiSecondaryButton(text = "Cancel", onClick = { editingVehicle = null })
                }
            },
        )
    }
}

@Composable
private fun MyVehicleCard(
    vehicle: Vehicle,
    canDelete: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    RaahiCard(
        borderColor = if (vehicle.isActive) AppColors.Primary else AppColors.Border,
        borderWidth = if (vehicle.isActive) 1.5.dp else 1.dp,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(vehicle.makeModel, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                if (vehicle.isActive) VerificationBadgeChip(isVerified = true, label = "Active")
            }
            Text("${vehicle.color} • ${vehicle.plateNumber} • ${vehicle.seatCapacity} seats • ${vehicle.luggageCapacity.label}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                if (!vehicle.isActive) {
                    RaahiPrimaryButton(
                        text = "Set Active",
                        onClick = onSetActive,
                        modifier = Modifier.weight(1f),
                    )
                }
                RaahiSecondaryButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                )
            }
            if (canDelete) {
                RaahiSecondaryButton(
                    text = "Delete Vehicle",
                    onClick = onDelete,
                    textColor = AppColors.Error,
                    borderColor = AppColors.Error.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onToggleLanguage: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onResetDemoData: () -> Unit,
    onLogout: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Settings", onBack = onBack) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            item(key = "settings_prefs_card", contentType = "prefs_card") {
                SettingsPreferencesCard(
                    language = state.user.language,
                    notificationsEnabled = state.user.preferences.notificationsEnabled,
                    onToggleLanguage = onToggleLanguage,
                    onToggleNotifications = onToggleNotifications,
                )
            }

            item(key = "settings_links_card", contentType = "links_card") {
                SettingsLinksCard(
                    blockedCount = state.blockedUsers.size,
                    onOpenBlockedUsers = onOpenBlockedUsers,
                    onOpenTerms = { showTerms = true },
                    onOpenPrivacy = { showPrivacy = true },
                )
            }

            item(key = "settings_actions", contentType = "actions") {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    RaahiSecondaryButton(text = "Log Out", onClick = onLogout)
                    RaahiSecondaryButton(
                        text = "Delete Account (Demo)",
                        onClick = { showDeleteAccount = true },
                        textColor = AppColors.Error,
                        borderColor = AppColors.Error.copy(alpha = 0.4f),
                        modifier = Modifier.testTag("settings_delete_account_btn"),
                    )
                }
            }
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Terms & Conditions (Prototype)", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = { Text("Raahi is a local frontend prototype. Rides, fares, verification badges, and bookings shown in this app are sample mock data with no real financial or transport obligations.", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary) },
            confirmButton = { RaahiPrimaryButton(text = "Close", onClick = { showTerms = false }) },
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Privacy Policy (Prototype)", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = { Text("All documents, selfies, phone numbers, and chat messages remain strictly inside local device memory during your prototype session and are never uploaded.", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary) },
            confirmButton = { RaahiPrimaryButton(text = "Close", onClick = { showPrivacy = false }) },
        )
    }

    if (showDeleteAccount) {
        RaahiConfirmDialog(
            title = "Delete demo account?",
            message = "This resets all local prototype data and returns you to the login flow.",
            confirmLabel = "Delete & Reset",
            isDestructive = true,
            onConfirm = {
                showDeleteAccount = false
                onResetDemoData()
                onShowMessage("Demo account reset locally.")
                onLogout()
            },
            onDismiss = { showDeleteAccount = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsPreferencesCard(
    language: String,
    notificationsEnabled: Boolean,
    onToggleLanguage: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Text("Language (UI preview)", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                listOf("English", "Urdu (اردو)").forEach { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { onToggleLanguage(lang) },
                        label = { Text(lang, style = AppTextStyles.chip, maxLines = 1) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("settings_lang_${lang.take(2).lowercase()}"),
                    )
                }
            }
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications", style = AppTextStyles.body, color = AppColors.TextPrimary)
                Switch(checked = notificationsEnabled, onCheckedChange = onToggleNotifications)
            }
        }
    }
}

@Composable
private fun SettingsLinksCard(
    blockedCount: Int,
    onOpenBlockedUsers: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column {
            RaahiListRow(
                icon = Icons.Rounded.Block,
                title = "Blocked users ($blockedCount)",
                onClick = onOpenBlockedUsers,
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.Description,
                title = "Terms & Conditions",
                onClick = onOpenTerms,
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.PrivacyTip,
                title = "Privacy Policy",
                onClick = onOpenPrivacy,
            )
        }
    }
}

@Composable
fun HelpCenterScreen(
    onBack: () -> Unit,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedId by remember { mutableStateOf<String?>(MockDataFactory.faqItems.firstOrNull()?.id) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Help Center & FAQ", onBack = onBack) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(
                items = MockDataFactory.faqItems,
                key = { it.id },
                contentType = { "faq_item" },
            ) { item ->
                val isOpen = expandedId == item.id
                FaqItemCard(
                    item = item,
                    isOpen = isOpen,
                    onToggle = { expandedId = if (isOpen) null else item.id },
                )
            }
            item(key = "faq_support_cta", contentType = "support_cta") {
                Spacer(Modifier.height(AppSpacing.s4))
                RaahiPrimaryButton(
                    text = "Open Support & Disputes",
                    onClick = onOpenSupport,
                )
            }
        }
    }
}

@Composable
private fun FaqItemCard(
    item: FaqItem,
    isOpen: Boolean,
    onToggle: () -> Unit,
) {
    RaahiCard(
        onClick = onToggle,
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.question, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, modifier = Modifier.weight(1f).padding(end = AppSpacing.s8))
                Icon(
                    imageVector = if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                )
            }
            AnimatedVisibility(visible = isOpen) {
                Text(item.answer, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
            }
        }
    }
}
