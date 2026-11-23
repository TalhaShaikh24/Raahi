package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LiveRideScreen(
    bookingId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenSafetyCenter: () -> Unit,
    onCompleteTrip: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.bookings.find { it.id == bookingId }
    val ride = booking?.let { state.ridesById[it.rideId] }
    val driver = ride?.let { state.driversById[it.driverId] }

    var checkInStatus by remember { mutableStateOf<String?>(null) }
    var sosHolding by remember { mutableStateOf(false) }
    var sosProgress by remember { mutableFloatStateOf(0f) }
    var sosTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(sosHolding) {
        if (sosHolding && !sosTriggered) {
            val steps = 30
            for (i in 1..steps) {
                if (!sosHolding) break
                sosProgress = i / steps.toFloat()
                delay(100)
            }
            if (sosHolding && sosProgress >= 0.99f) {
                sosTriggered = true
                sosHolding = false
                onShowMessage("SOS Demo: Location shared with your trusted contacts")
            }
        } else if (!sosHolding && !sosTriggered) {
            sosProgress = 0f
        }
    }

    if (booking == null || ride == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Live ride", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Trip not found.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Live Ride • Ongoing",
                subtitle = "${ride.originCity} → ${ride.destinationCity}",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onOpenSafetyCenter, modifier = Modifier.testTag("live_ride_safety_center_btn")) {
                        Icon(Icons.Rounded.Shield, contentDescription = "Safety Center", tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                    }
                },
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
            // Static rounded route map card
            RaahiCard(
                containerColor = AppColors.PrimarySoft,
                borderColor = AppColors.Primary.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth().height(170.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(AppSpacing.cardPadding)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = AppColors.Primary,
                            start = Offset(36f, size.height * 0.72f),
                            end = Offset(size.width - 36f, size.height * 0.28f),
                            strokeWidth = 9f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 14f), 0f),
                        )
                        drawCircle(color = AppColors.PrimaryDark, radius = 14f, center = Offset(36f, size.height * 0.72f))
                        drawCircle(color = AppColors.Primary, radius = 16f, center = Offset(size.width * 0.55f, size.height * 0.48f))
                        drawCircle(color = AppColors.Accent, radius = 14f, center = Offset(size.width - 36f, size.height * 0.28f))
                    }
                    RaahiPill(
                        text = "Route Map • Auto-share ${if (state.user.autoShareLiveRide) "ON" else "OFF"}",
                        containerColor = AppColors.Surface,
                        contentColor = AppColors.TextPrimary,
                    )
                    Text(
                        text = "${booking.pickupPoint} → ${booking.dropoffPoint}",
                        style = AppTextStyles.cardTitle,
                        color = AppColors.PrimaryDark,
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
            }

            // Safety check-in card
            RaahiCard(
                borderColor = AppColors.Primary.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(AppSpacing.s8))
                        Text("Safety Check-in: Are you okay?", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    }
                    if (checkInStatus != null) {
                        VerificationBadgeChip(
                            isVerified = checkInStatus == "Fine",
                            label = if (checkInStatus == "Fine") "Checked in: I'm Fine" else "SOS Triggered",
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                        RaahiPrimaryButton(
                            text = "I'm Fine",
                            onClick = {
                                checkInStatus = "Fine"
                                onShowMessage("Thanks! Check-in recorded locally.")
                            },
                            modifier = Modifier.weight(1f).testTag("live_ride_im_fine_button"),
                        )
                        RaahiSecondaryButton(
                            text = "SOS",
                            onClick = {
                                sosTriggered = true
                                checkInStatus = "SOS"
                                onShowMessage("SOS Demo: Location shared with trusted contacts")
                            },
                            textColor = AppColors.Error,
                            borderColor = AppColors.Error.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f).testTag("live_ride_checkin_sos_button"),
                        )
                    }
                }
            }

            // 3-second hold SOS panel (#FEF2F2 bg + #DC2626 button)
            RaahiCard(
                containerColor = AppColors.SosBg,
                borderColor = AppColors.Error.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Emergency SOS (3-Second Hold • Demo)", style = AppTextStyles.cardTitle, color = AppColors.Error)
                    Text(
                        text = if (sosTriggered) "Location shared with your trusted contacts (${state.trustedContacts.size}). Demo mode — no real emergency call is placed." else "Press and hold for 3 seconds to simulate sharing your live ride.",
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.TextPrimary,
                    )

                    if (!sosTriggered) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppSpacing.buttonHeight)
                                .clip(RoundedCornerShape(AppSpacing.radiusButton))
                                .background(AppColors.Error)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            sosHolding = true
                                            tryAwaitRelease()
                                            sosHolding = false
                                        },
                                    )
                                }
                                .testTag("live_ride_hold_sos_button"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(sosProgress)
                                    .align(Alignment.CenterStart)
                                    .background(Color.White.copy(alpha = 0.25f)),
                            )
                            Text(
                                text = if (sosHolding) "Keep holding… ${(sosProgress * 100).toInt()}%" else "Hold 3s for SOS",
                                style = AppTextStyles.button,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(AppSpacing.radiusInput), color = AppColors.VerifiedBg, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(AppSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AppColors.VerifiedText, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(AppSpacing.s8))
                                Text("Location shared with trusted contacts", style = AppTextStyles.cardTitle, color = AppColors.VerifiedText)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                        RaahiSecondaryButton(
                            text = "Call 15",
                            icon = Icons.Rounded.LocalPolice,
                            onClick = { onShowMessage("Demo UI: Quick-call Police 15 (not placed)") },
                            textColor = AppColors.Error,
                            borderColor = AppColors.Error.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f).testTag("live_ride_call_15_button"),
                        )
                        RaahiSecondaryButton(
                            text = "Call 1122",
                            icon = Icons.Rounded.MedicalServices,
                            onClick = { onShowMessage("Demo UI: Quick-call Rescue 1122 (not placed)") },
                            textColor = AppColors.Error,
                            borderColor = AppColors.Error.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f).testTag("live_ride_call_1122_button"),
                        )
                    }
                }
            }

            if (driver != null) {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        DriverMiniProfile(driver = driver, vehicle = state.vehiclesById[ride.vehicleId])
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                            RaahiSecondaryButton(
                                text = "Chat",
                                icon = Icons.Rounded.ChatBubbleOutline,
                                onClick = { onOpenChat(booking.id) },
                                modifier = Modifier.weight(1f),
                            )
                            RaahiSecondaryButton(
                                text = "Call",
                                icon = Icons.Rounded.Call,
                                onClick = { onShowMessage("Calling via app…") },
                                modifier = Modifier.weight(1f).testTag("live_ride_masked_call"),
                            )
                        }
                    }
                }
            }

            RaahiPrimaryButton(
                text = "Complete Trip",
                onClick = {
                    onCompleteTrip(booking.id)
                    onBack()
                },
                modifier = Modifier.testTag("live_ride_complete_button"),
            )
            Spacer(Modifier.height(AppSpacing.s12))
        }
    }
}

@Composable
fun EmergencyContactsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onUpsert: (id: String?, name: String, phone: String, relation: String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<EmergencyContact?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Emergency contacts",
                subtitle = "Local prototype list",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { showAdd = true }, modifier = Modifier.testTag("emergency_add_btn")) {
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
            if (state.emergencyContacts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Rounded.ContactPhone,
                        title = "No emergency contacts",
                        subtitle = "Add someone we should display in your safety toolkit.",
                        actionLabel = "Add contact",
                        onAction = { showAdd = true },
                    )
                }
            } else {
                items(state.emergencyContacts, key = { it.id }) { contact ->
                    ContactCard(
                        name = contact.name,
                        phone = contact.phone,
                        relation = contact.relation,
                        onEdit = { editing = contact },
                        onDelete = { onDelete(contact.id) },
                    )
                }
            }
        }
    }

    if (showAdd || editing != null) {
        ContactEditorDialog(
            title = if (editing == null) "Add emergency contact" else "Edit emergency contact",
            initialName = editing?.name ?: "",
            initialPhone = editing?.phone ?: "",
            initialRelation = editing?.relation ?: "Family",
            onSave = { name, phone, rel ->
                onUpsert(editing?.id, name, phone, rel)
                editing = null
                showAdd = false
            },
            onDismiss = { editing = null; showAdd = false },
        )
    }
}

@Composable
fun TrustedContactsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onToggleAutoShare: (Boolean) -> Unit,
    onUpsert: (id: String?, name: String, phone: String, relation: String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<TrustedContact?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Trusted contacts (${state.trustedContacts.size}/3)",
                subtitle = "Max 3 contacts for live ride sharing",
                onBack = onBack,
                actions = {
                    if (state.trustedContacts.size < 3) {
                        TextButton(onClick = { showAdd = true }, modifier = Modifier.testTag("trusted_add_btn")) {
                            Text("+ Add", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                        }
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
            item {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.cardPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                            Text("Auto-share my live ride", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            Text("Notify trusted contacts when Ride Start OTP begins a trip.", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = state.user.autoShareLiveRide,
                            onCheckedChange = onToggleAutoShare,
                            modifier = Modifier.testTag("trusted_autoshare_switch"),
                        )
                    }
                }
            }

            items(state.trustedContacts, key = { it.id }) { contact ->
                ContactCard(
                    name = contact.name,
                    phone = contact.phone,
                    relation = contact.relation,
                    onEdit = { editing = contact },
                    onDelete = { onDelete(contact.id) },
                )
            }
        }
    }

    if (showAdd || editing != null) {
        ContactEditorDialog(
            title = if (editing == null) "Add trusted contact" else "Edit trusted contact",
            initialName = editing?.name ?: "",
            initialPhone = editing?.phone ?: "",
            initialRelation = editing?.relation ?: "Family",
            onSave = { name, phone, rel ->
                onUpsert(editing?.id, name, phone, rel)
                editing = null
                showAdd = false
            },
            onDismiss = { editing = null; showAdd = false },
        )
    }
}

@Composable
private fun ContactCard(
    name: String,
    phone: String,
    relation: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    RaahiCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(AppColors.PrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(AppSpacing.s12))
            Column(modifier = Modifier.weight(1f)) {
                Text("$name • $relation", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(phone, style = AppTextStyles.caption, color = AppColors.TextSecondary)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = "Edit contact", tint = AppColors.TextSecondary) }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete contact", tint = AppColors.Error) }
        }
    }
}

@Composable
private fun ContactEditorDialog(
    title: String,
    initialName: String,
    initialPhone: String,
    initialRelation: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var relation by remember { mutableStateOf(initialRelation) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppSpacing.radiusSheet),
        containerColor = AppColors.Surface,
        title = { Text(title, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                RaahiTextField(value = name, onValueChange = { name = it }, label = "Name")
                RaahiTextField(value = phone, onValueChange = { phone = it }, label = "Phone", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                RaahiTextField(value = relation, onValueChange = { relation = it }, label = "Relation")
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                RaahiPrimaryButton(text = "Save Contact", onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, relation) })
                RaahiSecondaryButton(text = "Cancel", onClick = onDismiss)
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportUserScreen(
    initialTargetName: String,
    onBack: () -> Unit,
    onSubmitReport: (category: String, subject: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var targetName by remember { mutableStateOf(initialTargetName.ifBlank { "Ride / User" }) }
    var category by remember { mutableStateOf("Safety concern") }
    var details by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Report user or ride", onBack = onBack) },
    ) { pad ->
        if (submitted) {
            Column(
                modifier = Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AppColors.VerifiedText, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(AppSpacing.s12))
                Text("Report submitted locally", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                Spacer(Modifier.height(AppSpacing.s4))
                Text("Logged in My Complaints for prototype review.", style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                Spacer(Modifier.height(AppSpacing.s24))
                RaahiPrimaryButton(text = "Done", onClick = onBack)
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
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                        RaahiTextField(value = targetName, onValueChange = { targetName = it }, label = "User or Ride")
                        Text("Reason", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        ) {
                            listOf("Safety concern", "No-show", "Fare dispute", "Rude behaviour", "Wrong vehicle").forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, style = AppTextStyles.chip, maxLines = 1) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                        RaahiTextField(
                            value = details,
                            onValueChange = { details = it },
                            label = "Describe what happened",
                            singleLine = false,
                            minLines = 3,
                            maxLines = 5,
                            fieldTestTag = "report_details_input",
                        )
                    }
                }
                RaahiPrimaryButton(
                    text = "Submit Report",
                    onClick = {
                        onSubmitReport(category, "Report: $targetName ($category)", details.ifBlank { "Submitted via Report User" })
                        submitted = true
                    },
                    modifier = Modifier.testTag("report_submit_button"),
                )
            }
        }
    }
}

@Composable
fun BlockedUsersScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onUnblock: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Blocked users", subtitle = "Rides from blocked users stay hidden", onBack = onBack) },
    ) { pad ->
        if (state.blockedUsers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad).padding(AppSpacing.screenPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(icon = Icons.Rounded.Block, title = "No blocked users", subtitle = "You can block a user from their Public Profile.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(state.blockedUsers, key = { it.id }) { blocked ->
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(AppSpacing.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                            AvatarBadge(initials = blocked.initials, backgroundColor = AppColors.SurfaceSubtle, contentColor = AppColors.TextSecondary, size = 44.dp)
                            Spacer(Modifier.width(AppSpacing.s12))
                            Column(modifier = Modifier.weight(1f).padding(end = AppSpacing.s8)) {
                                Text(blocked.name, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${blocked.reason} • ${blocked.blockedAtLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            }
                            TextButton(onClick = { onUnblock(blocked.id) }) {
                                Text("Unblock", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LostFoundScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onAddItem: (rideId: String, rideSummary: String, itemTitle: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var itemTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Lost & Found",
                subtitle = "Recover items left in a car",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { showForm = true }) {
                        Text("+ Report", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
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
            items(state.lostFoundItems, key = { it.id }) { item ->
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.itemTitle, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                            VerificationBadgeChip(isVerified = item.statusLabel == "Returned", label = item.statusLabel)
                        }
                        Text(item.rideSummary, style = AppTextStyles.caption, fontWeight = FontWeight.SemiBold, color = AppColors.Primary)
                        Text(item.description, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            shape = RoundedCornerShape(AppSpacing.radiusSheet),
            containerColor = AppColors.Surface,
            title = { Text("Report Lost / Found Item", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    RaahiTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = "Item name (e.g. Earbuds)")
                    RaahiTextField(value = description, onValueChange = { description = it }, label = "Details & trip", singleLine = false, minLines = 2)
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = "Submit Item",
                        onClick = {
                            if (itemTitle.isNotBlank()) {
                                onAddItem("R-001", "Hyderabad → Karachi", itemTitle, description.ifBlank { "Left in car" })
                                itemTitle = ""; description = ""; showForm = false
                            }
                        },
                    )
                    RaahiSecondaryButton(text = "Cancel", onClick = { showForm = false })
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportDisputeScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSubmitComplaint: (category: String, subject: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tabIdx by remember { mutableIntStateOf(0) }
    var category by remember { mutableStateOf("Fare & Payment") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = { RaahiTopBar(title = "Support & Disputes", onBack = onBack) },
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            TabRow(
                selectedTabIndex = tabIdx,
                containerColor = AppColors.Surface,
                contentColor = AppColors.Primary,
                divider = { HorizontalDivider(color = AppColors.Border, thickness = 1.dp) },
            ) {
                Tab(selected = tabIdx == 0, onClick = { tabIdx = 0 }, text = { Text("My Complaints (${state.complaints.size})", style = AppTextStyles.button, maxLines = 1) })
                Tab(selected = tabIdx == 1, onClick = { tabIdx = 1 }, text = { Text("New Dispute", style = AppTextStyles.button, maxLines = 1) })
            }
            if (tabIdx == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    items(state.complaints, key = { it.id }) { cmp ->
                        val (bg, fg) = when (cmp.status) {
                            ComplaintStatus.OPEN -> AppColors.AccentSoft to AppColors.AccentOnSoft
                            ComplaintStatus.IN_PROGRESS -> AppColors.PrimarySoft to AppColors.PrimaryDark
                            ComplaintStatus.RESOLVED -> AppColors.VerifiedBg to AppColors.VerifiedText
                        }
                        RaahiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(cmp.category, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                                    RaahiPill(text = cmp.status.label, containerColor = bg, contentColor = fg)
                                }
                                Text(cmp.subject, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                                Text(cmp.description, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                                Text(cmp.createdAtLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
                ) {
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                            Text("Category", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                            ) {
                                listOf("Fare & Payment", "Ride Experience", "Safety", "Verification").forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick = { category = cat },
                                        label = { Text(cat, style = AppTextStyles.chip, maxLines = 1) },
                                        shape = RoundedCornerShape(50),
                                    )
                                }
                            }
                            RaahiTextField(value = subject, onValueChange = { subject = it }, label = "Subject")
                            RaahiTextField(value = description, onValueChange = { description = it }, label = "Details", singleLine = false, minLines = 3)
                        }
                    }
                    RaahiPrimaryButton(
                        text = "Submit Ticket",
                        onClick = {
                            if (subject.isNotBlank()) {
                                onSubmitComplaint(category, subject, description.ifBlank { "Submitted from Support" })
                                subject = ""; description = ""; tabIdx = 0
                            }
                        },
                    )
                }
            }
        }
    }
}
