package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Chat with the driver or passenger. When the chat belongs to a booking, the fare negotiation
 * happens right here: a deal panel pinned at the top shows the current offer with
 * Accept / Counter / Decline, and every offer also appears as a card inside the thread.
 */
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
    onRespondToOffer: (bookingId: String, action: OfferAction) -> Unit = { _, _ -> },
    onSubmitCounterOffer: (bookingId: String, actor: OfferActor, amount: Int, note: String) -> Unit = { _, _, _, _ -> },
    onFindAnotherRide: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val conversation = state.conversations.find { it.id == conversationId }
    var draft by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showOfferSheet by remember { mutableStateOf(false) }
    // Prototype helper: lets a tester answer as the other side of the deal from the same phone.
    var actAsOtherSide by remember { mutableStateOf(false) }

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

    val booking = conversation.bookingId?.let { state.bookingsById[it] }
    val ride = booking?.let { state.ridesById[it.rideId] }
    val myRole = if (booking?.isIncomingDriverRequest == true) OfferActor.DRIVER else OfferActor.PASSENGER
    val actingRole = if (actAsOtherSide) myRole.other() else myRole
    val dealIsOpen = booking != null && (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.NEGOTIATING)
    val pendingOffer = booking?.offers?.lastOrNull { it.status == CounterOfferStatus.PENDING }

    val listState = rememberLazyListState()
    val messageCount = conversation.messages.size
    LaunchedEffect(messageCount) {
        if (messageCount > 0) listState.animateScrollToItem(messageCount)
    }

    fun sendDraft(text: String = draft) {
        if (text.isNotBlank()) {
            onSendMessage(conversation.id, text)
            draft = ""
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            ChatTopBar(
                conversation = conversation,
                onBack = onBack,
                onCall = { onShowMessage("Calling via app… (number stays private)") },
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                canViewProfile = state.driversById.containsKey(conversation.counterpartId),
                onOpenPublicProfile = { onOpenPublicProfile(conversation.counterpartId) },
                onOpenBooking = conversation.bookingId?.let { id -> { onOpenBooking(id) } },
                onReport = { onReportUser(conversation.counterpartName) },
                onBlock = { showBlockDialog = true },
            )
        },
        bottomBar = {
            ChatComposer(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = { sendDraft() },
                onQuickReply = { sendDraft(it) },
                showOfferButton = dealIsOpen,
                onOfferClick = { showOfferSheet = true },
                quickReplies = remember(myRole) {
                    if (myRole == OfferActor.DRIVER) {
                        listOf("Salam! Seat is available 👍", "Where should I pick you up?", "Please be on time")
                    } else {
                        listOf("Salam! Is the seat available?", "Where is the pickup point?", "Can you do a lower fare?")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (booking != null && ride != null) {
                DealPanel(
                    booking = booking,
                    ride = ride,
                    counterpartName = conversation.counterpartName,
                    actingRole = actingRole,
                    actAsOtherSide = actAsOtherSide,
                    onToggleActAsOtherSide = { actAsOtherSide = !actAsOtherSide },
                    onAccept = {
                        onRespondToOffer(booking.id, OfferAction.ACCEPT)
                        actAsOtherSide = false
                    },
                    onDecline = {
                        onRespondToOffer(booking.id, OfferAction.REJECT)
                        actAsOtherSide = false
                    },
                    onCounter = { showOfferSheet = true },
                    onOpenTrip = { onOpenBooking(booking.id) },
                    onFindAnotherRide = onFindAnotherRide,
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = AppSpacing.s16, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                item(key = "privacy_note", contentType = "system") {
                    SystemPill(
                        text = "Your phone number stays private",
                        icon = Icons.Rounded.Lock,
                    )
                }
                items(
                    items = conversation.messages,
                    key = { it.id },
                    contentType = {
                        when {
                            it.offerId != null -> "offer"
                            it.isSystem -> "system"
                            it.isFromMe -> "msg_me"
                            else -> "msg_other"
                        }
                    },
                ) { msg ->
                    val offer = msg.offerId?.let { id -> booking?.offers?.find { it.id == id } }
                    Box(modifier = Modifier.animateItem()) {
                        when {
                            offer != null && booking != null -> OfferBubble(
                                offer = offer,
                                isFromMe = msg.isFromMe,
                                seats = booking.seatsRequested,
                                listedFare = booking.listedFarePerSeatPkr,
                            )
                            msg.isSystem -> SystemPill(text = msg.text, icon = Icons.Rounded.Handshake)
                            else -> ChatMessageBubble(msg = msg)
                        }
                    }
                }
            }
        }
    }

    if (showOfferSheet && booking != null) {
        FareOfferSheet(
            listedFare = booking.listedFarePerSeatPkr,
            latestOffer = pendingOffer?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr,
            seats = booking.seatsRequested,
            onSubmit = { amount, note ->
                onSubmitCounterOffer(booking.id, actingRole, amount, note)
                showOfferSheet = false
                actAsOtherSide = false
            },
            onDismiss = { showOfferSheet = false },
        )
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

private fun OfferActor.other(): OfferActor = if (this == OfferActor.DRIVER) OfferActor.PASSENGER else OfferActor.DRIVER

// ---------------------------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    conversation: ChatConversation,
    onBack: () -> Unit,
    onCall: () -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    canViewProfile: Boolean,
    onOpenPublicProfile: () -> Unit,
    onOpenBooking: (() -> Unit)?,
    onReport: () -> Unit,
    onBlock: () -> Unit,
) {
    Column(modifier = Modifier.background(AppColors.Surface)) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("top_bar_back")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Navigate back", tint = AppColors.TextPrimary)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = canViewProfile, onClick = onOpenPublicProfile)
                        .padding(vertical = 4.dp),
                ) {
                    AvatarBadge(
                        initials = conversation.counterpartInitials,
                        backgroundColor = AppColors.PrimarySoft,
                        isVerified = conversation.counterpartVerified,
                        size = 40.dp,
                    )
                    Spacer(Modifier.width(AppSpacing.s12))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = conversation.counterpartName,
                                style = AppTextStyles.cardTitle,
                                color = AppColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (conversation.counterpartVerified) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Rounded.Verified, contentDescription = "Verified", tint = AppColors.VerifiedText, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = conversation.rideContext,
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onCall, modifier = Modifier.testTag("chat_masked_call_button")) {
                    Icon(Icons.Rounded.Call, contentDescription = "Private in-app call", tint = AppColors.Primary)
                }
                Box {
                    IconButton(onClick = { onMenuExpandedChange(true) }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More chat actions", tint = AppColors.TextPrimary)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuExpandedChange(false) }) {
                        if (onOpenBooking != null) {
                            DropdownMenuItem(
                                text = { Text("Trip details", style = AppTextStyles.bodyCompact) },
                                leadingIcon = { Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null) },
                                onClick = {
                                    onMenuExpandedChange(false)
                                    onOpenBooking()
                                },
                            )
                        }
                        if (canViewProfile) {
                            DropdownMenuItem(
                                text = { Text("View profile", style = AppTextStyles.bodyCompact) },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                onClick = {
                                    onMenuExpandedChange(false)
                                    onOpenPublicProfile()
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Report user", style = AppTextStyles.bodyCompact) },
                            leadingIcon = { Icon(Icons.Rounded.Flag, contentDescription = null) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onReport()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Block user", style = AppTextStyles.bodyCompact, color = AppColors.Error) },
                            leadingIcon = { Icon(Icons.Rounded.Block, contentDescription = null, tint = AppColors.Error) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onBlock()
                            },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface),
        )
        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
    }
}

// ---------------------------------------------------------------------------------------------
// Deal panel
// ---------------------------------------------------------------------------------------------

private enum class DealPhase { MY_TURN, WAITING, AGREED, CLOSED }

@Composable
private fun DealPanel(
    booking: Booking,
    ride: Ride,
    counterpartName: String,
    actingRole: OfferActor,
    actAsOtherSide: Boolean,
    onToggleActAsOtherSide: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCounter: () -> Unit,
    onOpenTrip: () -> Unit,
    onFindAnotherRide: () -> Unit,
) {
    val pendingOffer = booking.offers.lastOrNull { it.status == CounterOfferStatus.PENDING }
    val phase = when (booking.status) {
        BookingStatus.CONFIRMED, BookingStatus.ONGOING, BookingStatus.COMPLETED -> DealPhase.AGREED
        BookingStatus.CANCELLED, BookingStatus.REJECTED -> DealPhase.CLOSED
        BookingStatus.PENDING, BookingStatus.NEGOTIATING ->
            if (pendingOffer != null && pendingOffer.actor != actingRole) DealPhase.MY_TURN else DealPhase.WAITING
    }
    val currentAmount = booking.agreedFarePerSeatPkr ?: pendingOffer?.amountPerSeatPkr ?: booking.listedFarePerSeatPkr
    val otherSideLabel = if (actingRole == OfferActor.DRIVER) "passenger" else "driver"
    var historyOpen by remember { mutableStateOf(false) }

    val accent = when (phase) {
        DealPhase.MY_TURN -> AppColors.Accent
        DealPhase.WAITING -> AppColors.Primary
        DealPhase.AGREED -> AppColors.VerifiedText
        DealPhase.CLOSED -> AppColors.Error
    }

    Surface(
        color = AppColors.Surface,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), spotColor = AppColors.Ink.copy(alpha = 0.12f))
            .testTag("chat_deal_panel"),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
        ) {
            // Header: route + current fare
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
                        label = "dealIcon",
                    ) { p ->
                        Icon(
                            imageVector = when (p) {
                                DealPhase.MY_TURN -> Icons.Rounded.Handshake
                                DealPhase.WAITING -> Icons.Rounded.HourglassTop
                                DealPhase.AGREED -> Icons.Rounded.CheckCircle
                                DealPhase.CLOSED -> Icons.Rounded.Cancel
                            },
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.width(AppSpacing.s12))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${ride.originCity} → ${ride.destinationCity}",
                        style = AppTextStyles.cardTitle,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${ride.departureDateLabel} • ${ride.departureTimeLabel} • ${Formatters.formatSeats(booking.seatsRequested)}",
                        style = AppTextStyles.caption,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedPkrText(amount = currentAmount, style = AppTextStyles.price, color = accent)
                    Text("per seat", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
            }

            // Phase-specific message and actions
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(RaahiMotion.MEDIUM)).togetherWith(fadeOut(tween(RaahiMotion.SHORT))) },
                label = "dealPhase",
            ) { p ->
                when (p) {
                    DealPhase.MY_TURN -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PulsingDot(color = AppColors.Accent)
                            Spacer(Modifier.width(AppSpacing.s4))
                            Text(
                                text = "${pendingOffer?.actorName?.substringBefore(" (") ?: counterpartName} offered ${Formatters.formatPkr(pendingOffer?.amountPerSeatPkr ?: currentAmount)}/seat. Your turn!",
                                style = AppTextStyles.bodyCompact,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.AccentOnSoft,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                            RaahiPrimaryButton(
                                text = "Accept",
                                icon = Icons.Rounded.Check,
                                onClick = onAccept,
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("negotiation_accept_button"),
                            )
                            RaahiSecondaryButton(
                                text = "Counter",
                                onClick = onCounter,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("negotiation_counter_button"),
                            )
                            OutlinedIconButton(
                                onClick = onDecline,
                                border = BorderStroke(1.dp, AppColors.Error.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .size(AppSpacing.buttonHeight)
                                    .testTag("negotiation_reject_button"),
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Decline offer", tint = AppColors.Error)
                            }
                        }
                    }

                    DealPhase.WAITING -> Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = AppColors.Primary)
                        Spacer(Modifier.width(AppSpacing.s4))
                        Text(
                            text = "Waiting for the $otherSideLabel to reply…",
                            style = AppTextStyles.bodyCompact,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onCounter, modifier = Modifier.testTag("negotiation_counter_button")) {
                            Text("Change offer", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                        }
                    }

                    DealPhase.AGREED -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.VerifiedBg)
                            .padding(horizontal = AppSpacing.s12, vertical = AppSpacing.s8),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Deal agreed 🎉", style = AppTextStyles.cardTitle, color = AppColors.VerifiedText)
                            Text(
                                text = "Total ${Formatters.formatPkr(currentAmount * booking.seatsRequested)} • Ride code ${booking.rideStartOtp}",
                                style = AppTextStyles.caption,
                                color = AppColors.TextPrimary,
                            )
                        }
                        TextButton(onClick = onOpenTrip, modifier = Modifier.testTag("chat_view_trip_button")) {
                            Text("View trip", style = AppTextStyles.button, color = AppColors.VerifiedText, maxLines = 1)
                        }
                    }

                    DealPhase.CLOSED -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "This request was closed.",
                            style = AppTextStyles.bodyCompact,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onFindAnotherRide) {
                            Text("Find another ride", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                        }
                    }
                }
            }

            // Footer: history toggle + prototype "answer as other side"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { historyOpen = !historyOpen }
                        .padding(horizontal = AppSpacing.s8, vertical = 4.dp)
                        .testTag("chat_offer_history_toggle"),
                ) {
                    Text(
                        text = "Offer history (${booking.offers.size})",
                        style = AppTextStyles.chip,
                        color = AppColors.TextSecondary,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (historyOpen) 180f else 0f),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (phase == DealPhase.MY_TURN || phase == DealPhase.WAITING) {
                    Text(
                        text = if (actAsOtherSide) "Back to me" else "Demo: reply as $otherSideLabel",
                        style = AppTextStyles.chip,
                        color = AppColors.Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onToggleActAsOtherSide)
                            .padding(horizontal = AppSpacing.s8, vertical = 4.dp)
                            .testTag("negotiation_switch_role_button"),
                    )
                }
            }

            AnimatedVisibility(
                visible = historyOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    booking.offers.forEach { offer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("offer_timeline_item_${offer.sequence}"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (offer.actor == OfferActor.DRIVER) AppColors.PrimaryDark else AppColors.Primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${offer.sequence}", style = AppTextStyles.chip, color = Color.White)
                            }
                            Spacer(Modifier.width(AppSpacing.s8))
                            Text(
                                text = offer.actorName,
                                style = AppTextStyles.caption,
                                color = AppColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = Formatters.formatPkr(offer.amountPerSeatPkr),
                                style = AppTextStyles.chip,
                                color = AppColors.TextPrimary,
                            )
                            Spacer(Modifier.width(AppSpacing.s8))
                            OfferStatusChip(status = offer.status)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Thread items
// ---------------------------------------------------------------------------------------------

@Composable
private fun ChatMessageBubble(msg: ChatMessage) {
    val bubbleShape = remember(msg.isFromMe) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = if (msg.isFromMe) 20.dp else 6.dp,
            bottomEnd = if (msg.isFromMe) 6.dp else 20.dp,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(bubbleShape)
                .then(
                    if (msg.isFromMe) Modifier.background(AppGradients.BrandHorizontal)
                    else Modifier.background(AppColors.Surface).border(1.dp, AppColors.Border, bubbleShape)
                )
                .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12),
        ) {
            Text(
                text = msg.text,
                style = AppTextStyles.bodyCompact,
                color = if (msg.isFromMe) Color.White else AppColors.TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = msg.timestampLabel,
                style = AppTextStyles.caption,
                color = if (msg.isFromMe) Color.White.copy(alpha = 0.78f) else AppColors.TextSecondary,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun OfferBubble(
    offer: CounterOffer,
    isFromMe: Boolean,
    seats: Int,
    listedFare: Int,
) {
    val isPending = offer.status == CounterOfferStatus.PENDING
    val diff = offer.amountPerSeatPkr - listedFare
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 270.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
                .border(
                    width = if (isPending) 2.dp else 1.dp,
                    color = if (isPending) AppColors.Accent else AppColors.Border,
                    shape = RoundedCornerShape(20.dp),
                )
                .testTag("chat_offer_bubble_${offer.sequence}"),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isPending) AppColors.AccentSoft else AppColors.SurfaceSubtle)
                    .padding(horizontal = AppSpacing.s12, vertical = 6.dp),
            ) {
                Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = AppColors.AccentOnSoft, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isFromMe) "Your offer" else "${offer.actorName.substringBefore(" (")}'s offer",
                    style = AppTextStyles.chip,
                    color = AppColors.AccentOnSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(offer.status.label, style = AppTextStyles.chip, color = AppColors.TextSecondary)
            }
            Column(modifier = Modifier.padding(AppSpacing.s12)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(Formatters.formatPkr(offer.amountPerSeatPkr), style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
                    Text(" /seat", style = AppTextStyles.caption, color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                }
                Text(
                    text = buildString {
                        append("Total ${Formatters.formatPkr(offer.amountPerSeatPkr * seats)}")
                        if (diff != 0) append(" • ${if (diff < 0) "" else "+"}${Formatters.formatPkr(diff)} vs listed")
                    },
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                )
                if (offer.note.isNotBlank()) {
                    Spacer(Modifier.height(AppSpacing.s8))
                    Text("“${offer.note}”", style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SystemPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(AppColors.PrimarySoft)
                .padding(horizontal = AppSpacing.s12, vertical = 6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.PrimaryDark, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = AppTextStyles.chip, color = AppColors.PrimaryDark, textAlign = TextAlign.Center)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Composer
// ---------------------------------------------------------------------------------------------

@Composable
private fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onQuickReply: (String) -> Unit,
    showOfferButton: Boolean,
    onOfferClick: () -> Unit,
    quickReplies: List<String>,
) {
    Column(
        modifier = Modifier
            .background(AppColors.Surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
    ) {
        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
        AnimatedVisibility(visible = draft.isEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = AppSpacing.s16, end = AppSpacing.s16, top = AppSpacing.s8),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                quickReplies.forEach { reply ->
                    Text(
                        text = reply,
                        style = AppTextStyles.chip,
                        color = AppColors.PrimaryDark,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.PrimarySoft)
                            .clickable { onQuickReply(reply) }
                            .padding(horizontal = AppSpacing.s12, vertical = AppSpacing.s8),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.s12, vertical = AppSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
        ) {
            AnimatedVisibility(visible = showOfferButton, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                FilledTonalIconButton(
                    onClick = onOfferClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AppColors.AccentSoft,
                        contentColor = AppColors.AccentOnSoft,
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_make_offer_button"),
                ) {
                    Icon(Icons.Rounded.LocalOffer, contentDescription = "Make a fare offer", modifier = Modifier.size(22.dp))
                }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = { Text("Message…", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.Background,
                    unfocusedContainerColor = AppColors.Background,
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = Color.Transparent,
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
            )
            val canSend = draft.isNotBlank()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) AppGradients.BrandHorizontal else androidx.compose.ui.graphics.SolidColor(AppColors.SurfaceSubtle))
                    .clickable(enabled = canSend, onClick = onSend)
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send message",
                    tint = if (canSend) Color.White else AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Fare offer sheet
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FareOfferSheet(
    listedFare: Int,
    latestOffer: Int,
    seats: Int,
    onSubmit: (amount: Int, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableIntStateOf(latestOffer) }
    var note by remember { mutableStateOf("") }
    val step = 50
    val suggestions = remember(listedFare, latestOffer) {
        listOf(
            "Listed" to listedFare,
            "Latest" to latestOffer,
            "−10%" to ((listedFare * 0.9).toInt() / 10 * 10),
        ).distinctBy { it.second }
    }

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Make an offer", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            Text(
                text = "Listed fare is ${Formatters.formatPkr(listedFare)} per seat",
                style = AppTextStyles.caption,
                color = AppColors.TextSecondary,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s16),
            ) {
                FilledTonalIconButton(
                    onClick = { amount = (amount - step).coerceAtLeast(step) },
                    modifier = Modifier.size(52.dp).testTag("offer_minus_button"),
                ) { Icon(Icons.Rounded.Remove, contentDescription = "Lower by $step") }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 150.dp)) {
                    AnimatedPkrText(amount = amount, style = AppTextStyles.display, color = AppColors.Primary)
                    Text("per seat", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
                FilledTonalIconButton(
                    onClick = { amount += step },
                    modifier = Modifier.size(52.dp).testTag("offer_plus_button"),
                ) { Icon(Icons.Rounded.Add, contentDescription = "Raise by $step") }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                suggestions.forEach { (label, value) ->
                    FilterChip(
                        selected = amount == value,
                        onClick = { amount = value },
                        label = { Text("$label ${Formatters.formatPkr(value)}", style = AppTextStyles.chip, maxLines = 1) },
                        shape = RoundedCornerShape(50),
                    )
                }
            }

            RaahiTextField(
                value = amount.toString(),
                onValueChange = { txt -> txt.filter { it.isDigit() }.take(6).toIntOrNull()?.let { amount = it } },
                label = "Or type an amount",
                prefixText = "PKR ",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                fieldTestTag = "counter_composer_amount",
            )
            RaahiTextField(
                value = note,
                onValueChange = { note = it },
                label = "Add a note (optional)",
                placeholder = "e.g. I can be at the pickup early",
            )
            Text(
                text = "Total for ${Formatters.formatSeats(seats)}: ${Formatters.formatPkr(amount * seats)}",
                style = AppTextStyles.cardTitle,
                color = AppColors.TextPrimary,
            )
            RaahiPrimaryButton(
                text = "Send offer",
                icon = Icons.AutoMirrored.Rounded.Send,
                enabled = amount > 0,
                onClick = { onSubmit(amount, note) },
                modifier = Modifier.testTag("counter_composer_submit"),
            )
        }
    }
}
