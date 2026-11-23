package com.example.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun ChatScreen(
    conversationId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onMarkRead: (String) -> Unit,
    onOpenBooking: (String) -> Unit,
    onOpenPublicProfile: (String) -> Unit,
    onReportUser: (String) -> Unit,
    onBlockUser: (userId: String, name: String, initials: String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversation = state.conversations.find { it.id == conversationId }
    var draft by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(conversationId) {
        onMarkRead(conversationId)
    }

    if (conversation == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Chat", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Conversation not found.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = conversation.counterpartName,
                subtitle = conversation.rideContext,
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { onShowMessage("Calling via app…") },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("chat_masked_call_button"),
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = "Masked in-app call", tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                    }
                    if (conversation.bookingId != null) {
                        TextButton(onClick = { onOpenBooking(conversation.bookingId) }) {
                            Text("Booking", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                        }
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More chat actions", tint = AppColors.TextPrimary)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (state.driversById.containsKey(conversation.counterpartId)) {
                                DropdownMenuItem(
                                    text = { Text("View Public Profile", style = AppTextStyles.bodyCompact) },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onOpenPublicProfile(conversation.counterpartId)
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Report user", style = AppTextStyles.bodyCompact) },
                                leadingIcon = { Icon(Icons.Rounded.Flag, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onReportUser(conversation.counterpartName)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Block user", style = AppTextStyles.bodyCompact, color = AppColors.Error) },
                                leadingIcon = { Icon(Icons.Rounded.Block, contentDescription = null, tint = AppColors.Error) },
                                onClick = {
                                    menuExpanded = false
                                    showBlockDialog = true
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(AppColors.Surface)) {
                HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .imePadding()
                        .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Write a message...", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.Background,
                            unfocusedContainerColor = AppColors.Background,
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.Border,
                        ),
                        modifier = Modifier.weight(1f).testTag("chat_message_input"),
                    )
                    FilledIconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSendMessage(conversation.id, draft)
                                draft = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppColors.Primary,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier.size(48.dp).testTag("chat_send_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send message", modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
        ) {
            item(key = "privacy_banner", contentType = "privacy_banner") {
                RaahiCard(
                    containerColor = AppColors.PrimarySoft,
                    borderColor = AppColors.Primary.copy(alpha = 0.25f),
                    elevated = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.s12),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                            Text(
                                text = "${conversation.counterpartRole} • Personal number masked",
                                style = AppTextStyles.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.PrimaryDark,
                            )
                            Text(
                                text = "Use chat or masked Call button above.",
                                style = AppTextStyles.caption,
                                color = AppColors.PrimaryDark,
                            )
                        }
                        VerificationBadgeChip(isVerified = conversation.counterpartVerified)
                    }
                }
            }
            items(
                items = conversation.messages,
                key = { it.id },
                contentType = { if (it.isFromMe) "msg_me" else "msg_other" },
            ) { msg ->
                ChatMessageBubble(msg = msg)
            }
        }
    }

    if (showBlockDialog) {
        RaahiConfirmDialog(
            title = "Block ${conversation.counterpartName}?",
            message = "Their rides will be hidden from search. You can unblock them anytime from Settings.",
            confirmLabel = "Block",
            isDestructive = true,
            onConfirm = {
                showBlockDialog = false
                onBlockUser(conversation.counterpartId, conversation.counterpartName, conversation.counterpartInitials)
                onBack()
            },
            onDismiss = { showBlockDialog = false },
        )
    }
}

@Composable
private fun ChatMessageBubble(msg: ChatMessage) {
    val bubbleShape = remember(msg.isFromMe) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (msg.isFromMe) 16.dp else 4.dp,
            bottomEnd = if (msg.isFromMe) 4.dp else 16.dp,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = bubbleShape,
            color = if (msg.isFromMe) AppColors.Primary else AppColors.SurfaceSubtle,
            modifier = Modifier.widthIn(max = 290.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12)) {
                Text(
                    text = msg.text,
                    style = AppTextStyles.bodyCompact,
                    color = if (msg.isFromMe) Color.White else AppColors.TextPrimary,
                )
                Spacer(Modifier.height(AppSpacing.s4))
                Text(
                    text = msg.timestampLabel,
                    style = AppTextStyles.caption,
                    color = if (msg.isFromMe) Color.White.copy(alpha = 0.78f) else AppColors.TextSecondary,
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

    val myVehiclesCount = state.myVehicles.size
    val demoBadgeText = stringResource(id = R.string.demo_badge_label)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item(key = "profile_header", contentType = "header") {
            ProfileHeaderBar(
                onOpenPublicProfile = onOpenPublicProfile,
                onOpenEditProfile = onOpenEditProfile,
                onOpenSettings = onOpenSettings,
            )
        }

        item(key = "profile_user_card", contentType = "user_card") {
            ProfileUserCard(user = state.user)
        }

        item(key = "profile_preview_switch", contentType = "preview_switch") {
            ProfilePreviewSwitchCard(
                previewAsBasic = state.user.previewAsBasic,
                onTogglePreviewMode = onTogglePreviewMode,
            )
        }

        item(key = "profile_verification_cta", contentType = "verification_cta") {
            ProfileVerificationCard(
                user = state.user,
                onOpenVerificationStatus = onOpenVerificationStatus,
                onOpenVerificationWizard = onOpenVerificationWizard,
            )
        }

        item(key = "profile_role_switch", contentType = "role_switch") {
            ProfileRoleSwitchCard(
                activeRole = state.user.activeRole,
                onSwitchRole = onSwitchRole,
            )
        }

        item(key = "profile_travel_garage", contentType = "section_card") {
            ProfileTravelGarageCard(
                vehiclesCount = myVehiclesCount,
                favouriteRoutesCount = state.favouriteRoutes.size,
                followedDriversCount = state.followedDriverIds.size,
                waitlistCount = state.waitlist.size,
                savedPlacesCount = state.user.savedPlaces.size,
                onOpenMyVehicles = onOpenMyVehicles,
                onOpenFavouriteRoutes = onOpenFavouriteRoutes,
                onOpenFavouriteDrivers = onOpenFavouriteDrivers,
                onOpenWaitlist = onOpenWaitlist,
                onOpenSavedPlaces = { showSavedPlacesSheet = true },
                onOpenPreferences = { showPreferencesSheet = true },
            )
        }

        item(key = "profile_safety_contacts", contentType = "section_card") {
            ProfileSafetyContactsCard(
                emergencyCount = state.emergencyContacts.size,
                trustedCount = state.trustedContacts.size,
                autoShareLiveRide = state.user.autoShareLiveRide,
                complaintsCount = state.complaints.size,
                onOpenSafety = onOpenSafety,
                onOpenEmergencyContacts = onOpenEmergencyContacts,
                onOpenTrustedContacts = onOpenTrustedContacts,
                onOpenSupport = onOpenSupport,
            )
        }

        item(key = "profile_finance_help", contentType = "section_card") {
            ProfileFinanceHelpCard(
                paymentsCount = state.payments.size,
                language = state.user.language,
                onOpenSavings = onOpenSavings,
                onOpenDriverEarnings = onOpenDriverEarnings,
                onOpenPaymentHistory = onOpenPaymentHistory,
                onOpenReferralPromo = onOpenReferralPromo,
                onOpenHelpCenter = onOpenHelpCenter,
                onOpenSettings = onOpenSettings,
                onRequestResetDemo = { showResetConfirm = true },
            )
        }

        item(key = "profile_logout", contentType = "logout_btn") {
            RaahiSecondaryButton(
                text = "Log Out",
                icon = Icons.Rounded.Logout,
                onClick = onSignOut,
                modifier = Modifier.testTag("profile_sign_out_button"),
            )
        }

        item(key = "profile_demo_badge", contentType = "demo_label") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = demoBadgeText,
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                )
                Spacer(Modifier.height(AppSpacing.s8))
            }
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
private fun ProfileHeaderBar(
    onOpenPublicProfile: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Profile", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s4), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onOpenPublicProfile, modifier = Modifier.testTag("profile_public_view_btn")) {
                Text("Public View", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
            }
            IconButton(onClick = onOpenEditProfile, modifier = Modifier.testTag("profile_edit_btn")) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit profile", tint = AppColors.TextPrimary, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_btn")) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = AppColors.TextPrimary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun ProfileUserCard(user: AppUser) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBadge(
                initials = user.initials,
                backgroundColor = AppColors.PrimarySoft,
                isVerified = user.isEffectivelyVerified,
                size = 68.dp,
            )
            Spacer(Modifier.width(AppSpacing.s16))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${user.homeCity} • ${user.gender} • ${user.rating} ★",
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                )
                Spacer(Modifier.height(AppSpacing.s8))
                VerificationBadgeChip(
                    isVerified = user.isEffectivelyVerified,
                    label = user.effectiveVerificationLevel.label,
                    modifier = Modifier.testTag("profile_verification_badge"),
                )
            }
        }
    }
}

@Composable
private fun ProfilePreviewSwitchCard(
    previewAsBasic: Boolean,
    onTogglePreviewMode: (Boolean) -> Unit,
) {
    RaahiCard(
        containerColor = AppColors.PrimarySoft,
        borderColor = AppColors.Primary.copy(alpha = 0.25f),
        elevated = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                Text("Preview as: Basic / Verified", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                Text(
                    text = if (previewAsBasic) "Currently Basic — Verified-Only rides locked" else "Currently Verified — Full ride & driver access",
                    style = AppTextStyles.caption,
                    color = AppColors.PrimaryDark,
                )
            }
            Switch(
                checked = !previewAsBasic,
                onCheckedChange = { verified -> onTogglePreviewMode(!verified) },
                modifier = Modifier.testTag("profile_preview_verified_switch"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileVerificationCard(
    user: AppUser,
    onOpenVerificationStatus: () -> Unit,
    onOpenVerificationWizard: () -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ID & Driver Verification", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Text("Status: ${user.verificationStatus.label}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
                TextButton(onClick = onOpenVerificationStatus, modifier = Modifier.testTag("profile_verification_status_btn")) {
                    Text("Status", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                VerificationBadgeChip(isVerified = user.cnicVerified && !user.previewAsBasic, label = "CNIC")
                VerificationBadgeChip(isVerified = user.licenceVerified && !user.previewAsBasic, label = "Licence")
                VerificationBadgeChip(isVerified = user.vehicleVerified && !user.previewAsBasic, label = "Vehicle")
            }
            RaahiSecondaryButton(
                text = if (user.isEffectivelyVerified) "Verification Wizard" else "Get Verified",
                icon = Icons.Rounded.VerifiedUser,
                onClick = onOpenVerificationWizard,
                modifier = Modifier.testTag("profile_get_verified_button"),
            )
        }
    }
}

@Composable
private fun ProfileRoleSwitchCard(
    activeRole: AppRole,
    onSwitchRole: (AppRole) -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                Text("App mode", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                Text(
                    text = if (activeRole == AppRole.DRIVER) "Driver Mode active" else "Passenger Mode active",
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                )
            }
            RoleModePill(activeRole = activeRole, onToggle = onSwitchRole)
        }
    }
}

@Composable
private fun ProfileTravelGarageCard(
    vehiclesCount: Int,
    favouriteRoutesCount: Int,
    followedDriversCount: Int,
    waitlistCount: Int,
    savedPlacesCount: Int,
    onOpenMyVehicles: () -> Unit,
    onOpenFavouriteRoutes: () -> Unit,
    onOpenFavouriteDrivers: () -> Unit,
    onOpenWaitlist: () -> Unit,
    onOpenSavedPlaces: () -> Unit,
    onOpenPreferences: () -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column {
            RaahiListRow(
                icon = Icons.Rounded.DirectionsCar,
                title = "My vehicles",
                subtitle = "$vehiclesCount registered vehicle(s)",
                onClick = onOpenMyVehicles,
                testTag = "profile_vehicles_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.BookmarkBorder,
                title = "Favourite routes",
                subtitle = "$favouriteRoutesCount saved routes",
                onClick = onOpenFavouriteRoutes,
                testTag = "profile_fav_routes_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.FavoriteBorder,
                title = "Favourite drivers",
                subtitle = "$followedDriversCount drivers followed",
                onClick = onOpenFavouriteDrivers,
                testTag = "profile_fav_drivers_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.HourglassEmpty,
                title = "Waitlist",
                subtitle = "$waitlistCount full ride(s) watched",
                onClick = onOpenWaitlist,
                testTag = "profile_waitlist_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.Place,
                title = "Saved places",
                subtitle = "$savedPlacesCount locations saved",
                onClick = onOpenSavedPlaces,
                testTag = "profile_saved_places_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.Tune,
                title = "Ride preferences",
                subtitle = "AC, chatty/silent, pets, smoking, women-only",
                onClick = onOpenPreferences,
                testTag = "profile_preferences_item",
            )
        }
    }
}

@Composable
private fun ProfileSafetyContactsCard(
    emergencyCount: Int,
    trustedCount: Int,
    autoShareLiveRide: Boolean,
    complaintsCount: Int,
    onOpenSafety: () -> Unit,
    onOpenEmergencyContacts: () -> Unit,
    onOpenTrustedContacts: () -> Unit,
    onOpenSupport: () -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column {
            RaahiListRow(
                icon = Icons.Rounded.Shield,
                title = "Safety Center",
                subtitle = "SOS toolkit, Lost & Found, Report, Blocked",
                onClick = onOpenSafety,
                testTag = "profile_safety_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.ContactPhone,
                title = "Emergency contacts",
                subtitle = "$emergencyCount saved",
                onClick = onOpenEmergencyContacts,
                testTag = "profile_emergency_contacts_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.ShareLocation,
                title = "Trusted contacts",
                subtitle = "$trustedCount/3 • Auto-share ${if (autoShareLiveRide) "ON" else "OFF"}",
                onClick = onOpenTrustedContacts,
                testTag = "profile_trusted_contacts_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.SupportAgent,
                title = "Support & Disputes",
                subtitle = "$complaintsCount tickets",
                onClick = onOpenSupport,
                testTag = "profile_support_item",
            )
        }
    }
}

@Composable
private fun ProfileFinanceHelpCard(
    paymentsCount: Int,
    language: String,
    onOpenSavings: () -> Unit,
    onOpenDriverEarnings: () -> Unit,
    onOpenPaymentHistory: () -> Unit,
    onOpenReferralPromo: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestResetDemo: () -> Unit,
) {
    RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
        Column {
            RaahiListRow(
                icon = Icons.Rounded.Savings,
                title = "Savings dashboard",
                subtitle = "PKR 14,800 saved • CO2 avoided",
                onClick = onOpenSavings,
                testTag = "profile_savings_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.Payments,
                title = "Driver earnings",
                subtitle = "Weekly fuel-split payouts (demo)",
                onClick = onOpenDriverEarnings,
                testTag = "profile_earnings_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.ReceiptLong,
                title = "Payment history",
                subtitle = "$paymentsCount Cash / JazzCash / Easypaisa logs",
                onClick = onOpenPaymentHistory,
                testTag = "profile_payment_history_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.CardGiftcard,
                title = "Referrals & Promos",
                subtitle = "Use FIRST50 for 50% off demo fare",
                onClick = onOpenReferralPromo,
                testTag = "profile_referral_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.HelpOutline,
                title = "Help Center & FAQ",
                subtitle = "Common questions & prototype guide",
                onClick = onOpenHelpCenter,
                testTag = "profile_help_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.Settings,
                title = "Settings",
                subtitle = "Language ($language), Blocked users, Terms",
                onClick = onOpenSettings,
                testTag = "profile_settings_item",
            )
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
            RaahiListRow(
                icon = Icons.Rounded.RestartAlt,
                title = "Reset demo data",
                subtitle = "Restore initial sample rides, offers & bookings",
                onClick = onRequestResetDemo,
                testTag = "profile_reset_demo_item",
            )
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
