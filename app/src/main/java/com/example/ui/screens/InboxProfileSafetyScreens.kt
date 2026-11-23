package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun InboxScreen(
    state: CarPoolUiState,
    onOpenConversation: (String) -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val unreadChats = state.unreadChatCount
    val unreadNotifs = state.unreadNotificationCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Inbox", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
                Spacer(Modifier.height(AppSpacing.s4))
                Text(
                    text = "Conversations & ride activity",
                    style = AppTextStyles.bodyCompact,
                    color = AppColors.TextSecondary,
                )
            }
            if (selectedTab == 1 && unreadNotifs > 0) {
                TextButton(
                    onClick = onMarkAllNotificationsRead,
                    modifier = Modifier.testTag("inbox_mark_all_read"),
                ) {
                    Text("Mark all read", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppColors.Surface,
            contentColor = AppColors.Primary,
            divider = { HorizontalDivider(color = AppColors.Border, thickness = 1.dp) },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (unreadChats > 0) "Messages ($unreadChats)" else "Messages", style = AppTextStyles.button, maxLines = 1) },
                modifier = Modifier.testTag("inbox_messages_tab"),
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (unreadNotifs > 0) "Activity ($unreadNotifs)" else "Activity", style = AppTextStyles.button, maxLines = 1) },
                modifier = Modifier.testTag("inbox_activity_tab"),
            )
        }

        if (selectedTab == 0) {
            if (state.conversations.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Rounded.ChatBubbleOutline,
                        title = "No messages yet",
                        subtitle = "Message drivers or passengers directly from any booking.",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    items(
                        items = state.conversations,
                        key = { it.id },
                        contentType = { "conversation_row" },
                    ) { convo ->
                        ConversationListItem(
                            convo = convo,
                            onClick = { onOpenConversation(convo.id) },
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(
                    items = state.notifications,
                    key = { it.id },
                    contentType = { "notification_row" },
                ) { notif ->
                    NotificationListItem(
                        notification = notif,
                        onClick = { onNotificationClick(notif) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    convo: ChatConversation,
    onClick: () -> Unit,
) {
    val lastMsg = convo.messages.lastOrNull()?.text ?: ""
    RaahiCard(
        onClick = onClick,
        elevated = false,
        modifier = Modifier.fillMaxWidth().testTag("conversation_item_${convo.id}"),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBadge(
                initials = convo.counterpartInitials,
                backgroundColor = AppColors.PrimarySoft,
                isVerified = convo.counterpartVerified,
                size = 48.dp,
            )
            Spacer(Modifier.width(AppSpacing.s12))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                    ) {
                        Text(
                            text = convo.counterpartName,
                            style = AppTextStyles.cardTitle,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        VerificationBadgeChip(isVerified = convo.counterpartVerified)
                    }
                    Text(
                        text = convo.lastMessageTime,
                        style = AppTextStyles.caption,
                        color = AppColors.TextSecondary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = convo.rideContext,
                    style = AppTextStyles.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppSpacing.s4))
                Text(
                    text = lastMsg,
                    style = AppTextStyles.bodyCompact,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (convo.unreadCount > 0) {
                Spacer(Modifier.width(AppSpacing.s8))
                Badge(containerColor = AppColors.Primary) { Text("${convo.unreadCount}") }
            }
        }
    }
}

@Composable
private fun NotificationListItem(
    notification: AppNotification,
    onClick: () -> Unit,
) {
    val (icon, tint, bg) = when (notification.type) {
        NotificationType.COUNTER_OFFER -> Triple(Icons.Rounded.Handshake, AppColors.AccentOnSoft, AppColors.AccentSoft)
        NotificationType.BOOKING_REQUEST -> Triple(Icons.Rounded.PersonAdd, AppColors.Primary, AppColors.PrimarySoft)
        NotificationType.OFFER_ACCEPTED -> Triple(Icons.Rounded.CheckCircle, AppColors.VerifiedText, AppColors.VerifiedBg)
        NotificationType.RIDE_REMINDER -> Triple(Icons.Rounded.Schedule, AppColors.PrimaryDark, AppColors.PrimarySoft)
        NotificationType.BOOKING_CANCELLED -> Triple(Icons.Rounded.Cancel, AppColors.Error, AppColors.SosBg)
        NotificationType.REVIEW_REQUEST -> Triple(Icons.Rounded.Star, AppColors.AccentOnSoft, AppColors.AccentSoft)
        NotificationType.VERIFICATION_UPDATE -> Triple(Icons.Rounded.VerifiedUser, AppColors.VerifiedText, AppColors.VerifiedBg)
        NotificationType.SAFETY_ALERT -> Triple(Icons.Rounded.Shield, AppColors.Error, AppColors.SosBg)
    }

    RaahiCard(
        onClick = onClick,
        borderColor = if (notification.isUnread) AppColors.Primary.copy(alpha = 0.45f) else AppColors.Border,
        elevated = false,
        modifier = Modifier.fillMaxWidth().testTag("notification_item_${notification.id}"),
    ) {
        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(AppSpacing.s12))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = notification.title,
                        style = AppTextStyles.cardTitle,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(notification.timestampLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
                Spacer(Modifier.height(AppSpacing.s4))
                Text(notification.body, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
            }
            if (notification.isUnread) {
                Spacer(Modifier.width(AppSpacing.s8))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    state: CarPoolUiState,
    onSwitchRole: (AppRole) -> Unit,
    onTogglePreviewMode: (Boolean) -> Unit,
    onUpdatePreferences: ((UserPreferences) -> UserPreferences) -> Unit,
    onAddSavedPlace: (String, String, String) -> Unit,
    onOpenVerificationWizard: () -> Unit,
    onOpenVerificationStatus: () -> Unit,
    onOpenPublicProfile: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenMyVehicles: () -> Unit,
    onOpenSafety: () -> Unit,
    onOpenEmergencyContacts: () -> Unit,
    onOpenTrustedContacts: () -> Unit,
    onOpenFavouriteRoutes: () -> Unit,
    onOpenFavouriteDrivers: () -> Unit,
    onOpenWaitlist: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenDriverEarnings: () -> Unit,
    onOpenPaymentHistory: () -> Unit,
    onOpenReferralPromo: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onResetDemoData: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSavedPlacesSheet by remember { mutableStateOf(false) }
    var showPreferencesSheet by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var moreExpanded by rememberSaveable { mutableStateOf(false) }

    val user = state.user
    val isDriver = user.activeRole == AppRole.DRIVER
    val demoBadgeText = stringResource(id = R.string.demo_badge_label)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item(key = "profile_hero", contentType = "hero") {
            ProfileHeroCard(
                user = user,
                onOpenEditProfile = onOpenEditProfile,
                onOpenSettings = onOpenSettings,
                onOpenPublicProfile = onOpenPublicProfile,
                modifier = Modifier.enterAnimation(index = 0),
            )
        }

        item(key = "profile_role_switch", contentType = "role_switch") {
            RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth().enterAnimation(index = 1)) {
                Row(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                        Text("I'm using Raahi as", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        Text(
                            text = if (isDriver) "Offering rides" else "Finding rides",
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                        )
                    }
                    RoleModePill(activeRole = user.activeRole, onToggle = onSwitchRole)
                }
            }
        }

        if (!user.isEffectivelyVerified) {
            item(key = "profile_verify_nudge", contentType = "verify") {
                RaahiCard(
                    onClick = onOpenVerificationWizard,
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary.copy(alpha = 0.25f),
                    elevated = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .enterAnimation(index = 2)
                        .testTag("profile_get_verified_button"),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppGradients.Brand),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Get the Verified badge", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                            Text("Takes 2 minutes • unlocks Verified-only rides", style = AppTextStyles.caption, color = AppColors.PrimaryDark)
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AppColors.PrimaryDark)
                    }
                }
            }
        }

        item(key = "profile_essentials", contentType = "section_card") {
            RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth().enterAnimation(index = 3)) {
                Column {
                    if (isDriver || state.myVehicles.isNotEmpty()) {
                        RaahiListRow(
                            icon = Icons.Rounded.DirectionsCar,
                            title = "My car",
                            subtitle = "${state.myVehicles.size} vehicle(s)",
                            onClick = onOpenMyVehicles,
                            testTag = "profile_vehicles_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    }
                    RaahiListRow(
                        icon = Icons.Rounded.Shield,
                        title = "Safety & SOS",
                        subtitle = "Emergency tools for every trip",
                        iconTint = AppColors.Error,
                        iconBg = AppColors.SosBg,
                        onClick = onOpenSafety,
                        testTag = "profile_safety_item",
                    )
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    RaahiListRow(
                        icon = Icons.Rounded.ContactPhone,
                        title = "Emergency contacts",
                        subtitle = if (state.emergencyContacts.isEmpty()) "Add someone we can alert" else "${state.emergencyContacts.size} saved",
                        onClick = onOpenEmergencyContacts,
                        testTag = "profile_emergency_contacts_item",
                    )
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    RaahiListRow(
                        icon = Icons.Rounded.HelpOutline,
                        title = "Help",
                        subtitle = "Questions & support",
                        onClick = onOpenHelpCenter,
                        testTag = "profile_help_item",
                    )
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    RaahiListRow(
                        icon = Icons.Rounded.Settings,
                        title = "Settings",
                        subtitle = "Language, notifications, blocked users",
                        onClick = onOpenSettings,
                        testTag = "profile_settings_item",
                    )
                }
            }
        }

        item(key = "profile_more", contentType = "more_card") {
            ProfileMoreCard(
                expanded = moreExpanded,
                onToggle = { moreExpanded = !moreExpanded },
                modifier = Modifier.enterAnimation(index = 4),
            ) {
                RaahiListRow(Icons.Rounded.BookmarkBorder, "Favourite routes", onOpenFavouriteRoutes, subtitle = "${state.favouriteRoutes.size} saved", testTag = "profile_fav_routes_item")
                RaahiListRow(Icons.Rounded.FavoriteBorder, "Favourite drivers", onOpenFavouriteDrivers, subtitle = "${state.followedDriverIds.size} followed", testTag = "profile_fav_drivers_item")
                RaahiListRow(Icons.Rounded.HourglassEmpty, "Waitlist", onOpenWaitlist, subtitle = "${state.waitlist.size} full ride(s) watched", testTag = "profile_waitlist_item")
                RaahiListRow(Icons.Rounded.Place, "Saved places", { showSavedPlacesSheet = true }, subtitle = "${user.savedPlaces.size} saved", testTag = "profile_saved_places_item")
                RaahiListRow(Icons.Rounded.Tune, "Ride preferences", { showPreferencesSheet = true }, subtitle = "AC, quiet ride, pets…", testTag = "profile_preferences_item")
                RaahiListRow(Icons.Rounded.ShareLocation, "Trusted contacts", onOpenTrustedContacts, subtitle = "${state.trustedContacts.size}/3 • share live trips", testTag = "profile_trusted_contacts_item")
                RaahiListRow(Icons.Rounded.Badge, "Verification status", onOpenVerificationStatus, subtitle = user.verificationStatus.label, testTag = "profile_verification_status_btn")
                RaahiListRow(Icons.Rounded.SupportAgent, "Support & disputes", onOpenSupport, subtitle = "${state.complaints.size} ticket(s)", testTag = "profile_support_item")
                RaahiListRow(Icons.Rounded.Savings, "My savings", onOpenSavings, testTag = "profile_savings_item")
                RaahiListRow(Icons.Rounded.Payments, "Driver earnings", onOpenDriverEarnings, testTag = "profile_earnings_item")
                RaahiListRow(Icons.Rounded.ReceiptLong, "Payment history", onOpenPaymentHistory, subtitle = "${state.payments.size} payment(s)", testTag = "profile_payment_history_item")
                RaahiListRow(Icons.Rounded.CardGiftcard, "Invite friends & promos", onOpenReferralPromo, subtitle = "Code FIRST50 = 50% off", testTag = "profile_referral_item")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demo: preview as Verified", style = AppTextStyles.body, color = AppColors.TextPrimary)
                        Text("Switch to test Verified-only features", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    }
                    Switch(
                        checked = !user.previewAsBasic,
                        onCheckedChange = { verified -> onTogglePreviewMode(!verified) },
                        modifier = Modifier.testTag("profile_preview_verified_switch"),
                    )
                }
                RaahiListRow(Icons.Rounded.RestartAlt, "Reset demo data", { showResetConfirm = true }, subtitle = "Restore sample rides & chats", testTag = "profile_reset_demo_item")
            }
        }

        item(key = "profile_logout", contentType = "logout_btn") {
            RaahiSecondaryButton(
                text = "Log out",
                icon = Icons.Rounded.Logout,
                onClick = onSignOut,
                textColor = AppColors.Error,
                modifier = Modifier.testTag("profile_sign_out_button"),
            )
        }

        item(key = "profile_demo_badge", contentType = "demo_label") {
            Text(
                text = demoBadgeText,
                style = AppTextStyles.caption,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.s8),
            )
        }
    }

    if (showResetConfirm) {
        RaahiConfirmDialog(
            title = "Reset demo data?",
            message = "This restores all sample rides, B-1001 negotiation history, bookings, and chats to their starting state.",
            confirmLabel = "Reset",
            onConfirm = {
                showResetConfirm = false
                onResetDemoData()
            },
            onDismiss = { showResetConfirm = false },
        )
    }

    if (showSavedPlacesSheet) {
        var newLabel by remember { mutableStateOf("") }
        var pickingPlace by remember { mutableStateOf(false) }

        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showSavedPlacesSheet = false },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(topStart = AppSpacing.radiusSheet, topEnd = AppSpacing.radiusSheet),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.s20).padding(bottom = AppSpacing.s24),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
            ) {
                Text("Saved places", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                state.user.savedPlaces.forEach { place ->
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Place, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(AppSpacing.s12))
                            Column {
                                Text(place.label, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                                Text("${place.area}, ${place.city}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
                RaahiTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    label = "New place label (e.g. University)",
                    placeholder = "University",
                )
                RaahiPrimaryButton(
                    text = "Pick City & Area",
                    onClick = { pickingPlace = true },
                )
            }
        }

        if (pickingPlace) {
            CityPickerSheet(
                title = "Choose city & area",
                onSelect = { city, area ->
                    onAddSavedPlace(newLabel.ifBlank { "Favourite" }, city, area)
                    newLabel = ""
                    pickingPlace = false
                },
                onDismiss = { pickingPlace = false },
            )
        }
    }

    if (showPreferencesSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showPreferencesSheet = false },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(topStart = AppSpacing.radiusSheet, topEnd = AppSpacing.radiusSheet),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.s20).padding(bottom = AppSpacing.s24),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
            ) {
                Text("Default ride preferences", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                PreferenceToggleRow("Prefer AC vehicles", state.user.preferences.acPreferred) { checked ->
                    onUpdatePreferences { it.copy(acPreferred = checked) }
                }
                PreferenceToggleRow("Chatty companion", state.user.preferences.chattyPreferred) { checked ->
                    onUpdatePreferences { it.copy(chattyPreferred = checked, quietRide = !checked) }
                }
                PreferenceToggleRow("Silent / Quiet ride", state.user.preferences.quietRide) { checked ->
                    onUpdatePreferences { it.copy(quietRide = checked, chattyPreferred = !checked) }
                }
                PreferenceToggleRow("Pets allowed", state.user.preferences.petsAllowed) { checked ->
                    onUpdatePreferences { it.copy(petsAllowed = checked) }
                }
                PreferenceToggleRow("Smoking allowed", state.user.preferences.smokingAllowed) { checked ->
                    onUpdatePreferences { it.copy(smokingAllowed = checked) }
                }
                PreferenceToggleRow("Women-only matching", state.user.preferences.womenOnlyPreferred) { checked ->
                    onUpdatePreferences { it.copy(womenOnlyPreferred = checked) }
                }
                PreferenceToggleRow("Demo activity alerts", state.user.preferences.notificationsEnabled) { checked ->
                    onUpdatePreferences { it.copy(notificationsEnabled = checked) }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    user: AppUser,
    onOpenEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPublicProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppGradients.Brand),
    ) {
        // Decorative soft circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-50).dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 40.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Column(modifier = Modifier.padding(AppSpacing.s20)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Profile", style = AppTextStyles.sectionTitle, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenEditProfile, modifier = Modifier.testTag("profile_edit_btn")) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit profile", tint = Color.White)
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_btn")) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            Spacer(Modifier.height(AppSpacing.s8))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(user.initials, style = AppTextStyles.screenTitle, color = AppColors.Primary)
                }
                Spacer(Modifier.width(AppSpacing.s16))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = AppTextStyles.screenTitle,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${user.rating} • ${user.homeCity}", style = AppTextStyles.caption, color = Color.White.copy(alpha = 0.85f))
                    }
                    Spacer(Modifier.height(AppSpacing.s8))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("profile_verification_badge"),
                    ) {
                        Icon(
                            imageVector = if (user.isEffectivelyVerified) Icons.Rounded.Verified else Icons.Rounded.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(user.effectiveVerificationLevel.label, style = AppTextStyles.chip, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.s12))
            Text(
                text = "See how others view you →",
                style = AppTextStyles.chip,
                color = AppColors.Mint,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenPublicProfile)
                    .padding(vertical = 4.dp)
                    .testTag("profile_public_view_btn"),
            )
        }
    }
}

@Composable
private fun ProfileMoreCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(RaahiMotion.MEDIUM),
        label = "moreChevron",
    )
    RaahiCard(elevated = false, modifier = modifier.fillMaxWidth()) {
        Column {
            RaahiListRow(
                icon = Icons.Rounded.GridView,
                title = if (expanded) "Fewer options" else "More",
                subtitle = if (expanded) null else "Favourites, payments, promos & more",
                onClick = onToggle,
                iconTint = AppColors.TextPrimary,
                iconBg = AppColors.SurfaceSubtle,
                trailing = {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotation),
                    )
                },
                testTag = "profile_more_toggle",
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(RaahiMotion.MEDIUM)) + fadeIn(tween(RaahiMotion.MEDIUM)),
                exit = shrinkVertically(tween(RaahiMotion.MEDIUM)) + fadeOut(tween(RaahiMotion.SHORT)),
            ) {
                Column {
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    content()
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppTextStyles.body, color = AppColors.TextPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SafetySupportScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onOpenEmergencyContacts: () -> Unit,
    onOpenTrustedContacts: () -> Unit,
    onOpenReportUser: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onOpenLostFound: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSosDialog by remember { mutableStateOf(false) }
    val sosDisclaimer = stringResource(id = R.string.sos_disclaimer)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Safety Center",
                subtitle = "Prototype safety & dispute toolkit",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            item(key = "sos_card", contentType = "sos_card") {
                RaahiCard(
                    containerColor = AppColors.SosBg,
                    borderColor = AppColors.Error.copy(alpha = 0.35f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(AppSpacing.s8))
                            Text("Emergency SOS (Demo)", style = AppTextStyles.cardTitle, color = AppColors.Error)
                        }
                        Text(
                            text = sosDisclaimer,
                            style = AppTextStyles.bodyCompact,
                            color = AppColors.TextPrimary,
                        )
                        RaahiDangerButton(
                            text = "Preview SOS Confirmation",
                            onClick = { showSosDialog = true },
                            modifier = Modifier.testTag("safety_sos_demo_button"),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                            RaahiSecondaryButton(
                                text = "Police 15",
                                onClick = { onShowMessage("Demo UI: Police 15 shortcut (not placed)") },
                                textColor = AppColors.Error,
                                borderColor = AppColors.Error.copy(alpha = 0.4f),
                                modifier = Modifier.weight(1f),
                            )
                            RaahiSecondaryButton(
                                text = "Rescue 1122",
                                onClick = { onShowMessage("Demo UI: Rescue 1122 shortcut (not placed)") },
                                textColor = AppColors.Error,
                                borderColor = AppColors.Error.copy(alpha = 0.4f),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            item(key = "contacts_card", contentType = "contacts_card") {
                RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        RaahiListRow(
                            icon = Icons.Rounded.ContactPhone,
                            title = "Emergency Contacts",
                            subtitle = "${state.emergencyContacts.size} contacts saved",
                            onClick = onOpenEmergencyContacts,
                            testTag = "safety_emergency_contacts_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        RaahiListRow(
                            icon = Icons.Rounded.ShareLocation,
                            title = "Trusted Contacts (Max 3)",
                            subtitle = "${state.trustedContacts.size}/3 • Auto-share ${if (state.user.autoShareLiveRide) "ON" else "OFF"}",
                            onClick = onOpenTrustedContacts,
                            testTag = "safety_trusted_contacts_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        Box(modifier = Modifier.padding(AppSpacing.cardPadding)) {
                            RaahiSecondaryButton(
                                text = "Share Trip Link",
                                icon = Icons.Rounded.ShareLocation,
                                onClick = {
                                    val firstTrusted = state.trustedContacts.firstOrNull()?.name ?: "your trusted contacts"
                                    onShowMessage("Demo trip link shared with $firstTrusted.")
                                },
                            )
                        }
                    }
                }
            }

            item(key = "reporting_card", contentType = "reporting_card") {
                RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        RaahiListRow(
                            icon = Icons.Rounded.Flag,
                            title = "Report a user or ride",
                            subtitle = "Log a local safety or fare concern",
                            onClick = onOpenReportUser,
                            testTag = "safety_report_button",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        RaahiListRow(
                            icon = Icons.Rounded.Block,
                            title = "Blocked users",
                            subtitle = "${state.blockedUsers.size} blocked",
                            onClick = onOpenBlockedUsers,
                            testTag = "safety_blocked_users_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        RaahiListRow(
                            icon = Icons.Rounded.WorkOutline,
                            title = "Lost & Found",
                            subtitle = "${state.lostFoundItems.size} item(s) logged",
                            onClick = onOpenLostFound,
                            testTag = "safety_lost_found_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        RaahiListRow(
                            icon = Icons.Rounded.SupportAgent,
                            title = "Support & My Complaints",
                            subtitle = "${state.complaints.size} active/past tickets",
                            onClick = onOpenSupport,
                            testTag = "safety_support_item",
                        )
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        RaahiListRow(
                            icon = Icons.Rounded.HelpOutline,
                            title = "Help Center & FAQ",
                            subtitle = "Read how Ride Start OTP and verification work",
                            onClick = onOpenHelpCenter,
                            testTag = "safety_help_center_item",
                        )
                    }
                }
            }
        }
    }

    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = AppColors.Error) },
            title = { Text("SOS Demo Confirmation", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Text(
                    text = "This is a UI prototype simulation. No emergency service, police, or SMS alert was sent. In a live release, this action would call 15 / Rescue 1122 and alert your emergency contacts.",
                    style = AppTextStyles.bodyCompact,
                    color = AppColors.TextSecondary,
                )
            },
            confirmButton = {
                RaahiPrimaryButton(text = "Understood", onClick = { showSosDialog = false })
            },
        )
    }
}
