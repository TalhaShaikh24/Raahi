package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.VerificationStatus
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
private data class LocalDocSlot(
    val key: String,
    val label: String,
    val bitmap: Bitmap? = null,
    val sampleSelected: Boolean = false,
) {
    val isReady: Boolean get() = bitmap != null || sampleSelected
}

private suspend fun decodeScaledBitmap(context: Context, uri: Uri, maxDimPx: Int = 900): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val w = info.size.width
                    val h = info.size.height
                    val longest = maxOf(w, h)
                    if (longest > maxDimPx) {
                        val scale = maxDimPx.toFloat() / longest.toFloat()
                        decoder.setTargetSize(
                            (w * scale).toInt().coerceAtLeast(1),
                            (h * scale).toInt().coerceAtLeast(1),
                        )
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (maxOf(bounds.outWidth / sample, bounds.outHeight / sample) > maxDimPx) {
                    sample *= 2
                }
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            }
        }.getOrNull()
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerificationWizardScreen(
    initialDriverMode: Boolean = true,
    onBack: () -> Unit,
    onSubmitted: (isDriver: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDriverWizard by remember { mutableStateOf(initialDriverMode) }
    val totalSteps = if (isDriverWizard) 6 else 3
    var currentStep by remember { mutableIntStateOf(1) }

    var slots by remember {
        mutableStateOf(
            mapOf(
                "cnic_front" to LocalDocSlot("cnic_front", "CNIC Front"),
                "cnic_back" to LocalDocSlot("cnic_back", "CNIC Back"),
                "selfie" to LocalDocSlot("selfie", "Live Selfie"),
                "licence_front" to LocalDocSlot("licence_front", "Driving Licence Front"),
                "licence_back" to LocalDocSlot("licence_back", "Driving Licence Back"),
                "veh_reg" to LocalDocSlot("veh_reg", "Vehicle Registration Book/Card"),
                "veh_front" to LocalDocSlot("veh_front", "Vehicle Front Photo"),
                "veh_back" to LocalDocSlot("veh_back", "Vehicle Back Photo"),
                "veh_sides" to LocalDocSlot("veh_sides", "Vehicle Sides Photo"),
                "veh_interior" to LocalDocSlot("veh_interior", "Vehicle Interior Photo"),
            )
        )
    }
    var licenceExpiry by remember { mutableStateOf("2029-08-15") }
    var activePickerSlot by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        val slotKey = activePickerSlot
        if (uri != null && slotKey != null) {
            scope.launch {
                val bmp = decodeScaledBitmap(context, uri, maxDimPx = 900)
                slots = slots.toMutableMap().apply {
                    val old = this[slotKey] ?: return@apply
                    this[slotKey] = old.copy(bitmap = bmp, sampleSelected = bmp == null)
                }
            }
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bmp: Bitmap? ->
        val slotKey = activePickerSlot
        if (bmp != null && slotKey != null) {
            slots = slots.toMutableMap().apply {
                val old = this[slotKey] ?: return@apply
                this[slotKey] = old.copy(bitmap = bmp, sampleSelected = false)
            }
        }
    }

    fun markSample(key: String) {
        slots = slots.toMutableMap().apply {
            val old = this[key] ?: return@apply
            this[key] = old.copy(sampleSelected = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = if (isDriverWizard) "Driver Verification" else "Passenger Verification",
                subtitle = "Step $currentStep of $totalSteps • Local preview only",
                onBack = {
                    if (currentStep > 1) currentStep-- else onBack()
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
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    if (currentStep > 1) {
                        RaahiSecondaryButton(
                            text = "Back",
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    RaahiPrimaryButton(
                        text = if (currentStep < totalSteps) "Next Step" else "Submit Review",
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                onSubmitted(isDriverWizard)
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentStep > 1) 1.35f else 1f)
                            .testTag("wizard_next_submit_button"),
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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                FilterChip(
                    selected = !isDriverWizard,
                    onClick = { isDriverWizard = false; currentStep = 1 },
                    label = { Text("Passenger (3 Steps)", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
                FilterChip(
                    selected = isDriverWizard,
                    onClick = { isDriverWizard = true; currentStep = 1 },
                    label = { Text("Driver (6 Steps)", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
            }

            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = AppColors.Primary,
                trackColor = AppColors.Border,
            )

            when {
                currentStep == 1 -> {
                    Text("Step 1: National ID (CNIC)", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text("Pick from gallery, camera, or tap 'Use sample preview' to test without files.", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    DocumentUploadCard(
                        slot = slots["cnic_front"]!!,
                        onPickGallery = { activePickerSlot = "cnic_front"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "cnic_front"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("cnic_front") },
                    )
                    DocumentUploadCard(
                        slot = slots["cnic_back"]!!,
                        onPickGallery = { activePickerSlot = "cnic_back"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "cnic_back"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("cnic_back") },
                    )
                }
                currentStep == 2 -> {
                    Text("Step 2: Selfie Check", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text("Take a clear selfie or pick a photo locally. Nothing is uploaded to any server.", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    DocumentUploadCard(
                        slot = slots["selfie"]!!,
                        onPickGallery = { activePickerSlot = "selfie"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "selfie"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("selfie") },
                    )
                }
                isDriverWizard && currentStep == 3 -> {
                    Text("Step 3: Driving Licence & Expiry", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    DocumentUploadCard(
                        slot = slots["licence_front"]!!,
                        onPickGallery = { activePickerSlot = "licence_front"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "licence_front"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("licence_front") },
                    )
                    DocumentUploadCard(
                        slot = slots["licence_back"]!!,
                        onPickGallery = { activePickerSlot = "licence_back"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "licence_back"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("licence_back") },
                    )
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
                            RaahiTextField(
                                value = licenceExpiry,
                                onValueChange = { licenceExpiry = it },
                                label = "Licence expiry date (YYYY-MM-DD)",
                                placeholder = "2029-08-15",
                            )
                        }
                    }
                }
                isDriverWizard && currentStep == 4 -> {
                    Text("Step 4: Vehicle Registration Document", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    DocumentUploadCard(
                        slot = slots["veh_reg"]!!,
                        onPickGallery = { activePickerSlot = "veh_reg"; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onTakeCamera = { activePickerSlot = "veh_reg"; cameraPreviewLauncher.launch(null) },
                        onUseSample = { markSample("veh_reg") },
                    )
                }
                isDriverWizard && currentStep == 5 -> {
                    Text("Step 5: Vehicle Photos", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    listOf("veh_front", "veh_back", "veh_sides", "veh_interior").forEach { key ->
                        DocumentUploadCard(
                            slot = slots[key]!!,
                            onPickGallery = { activePickerSlot = key; galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onTakeCamera = { activePickerSlot = key; cameraPreviewLauncher.launch(null) },
                            onUseSample = { markSample(key) },
                        )
                    }
                }
                else -> {
                    Text("Review & Submit", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text("Check your attached documents below before submitting locally.", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                    val requiredKeys = if (isDriverWizard) {
                        listOf("cnic_front", "cnic_back", "selfie", "licence_front", "licence_back", "veh_reg", "veh_front", "veh_back", "veh_sides", "veh_interior")
                    } else {
                        listOf("cnic_front", "cnic_back", "selfie")
                    }
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                            requiredKeys.forEach { k ->
                                val s = slots[k]!!
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(s.label, style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (s.isReady) "Attached locally" else "Sample auto-filled",
                                        style = AppTextStyles.chip,
                                        color = if (s.isReady) AppColors.VerifiedText else AppColors.TextSecondary,
                                    )
                                }
                            }
                            if (isDriverWizard) {
                                HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                                Text("Licence Expiry: $licenceExpiry", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppSpacing.s12))
        }
    }
}

@Composable
private fun DocumentUploadCard(
    slot: LocalDocSlot,
    onPickGallery: () -> Unit,
    onTakeCamera: () -> Unit,
    onUseSample: () -> Unit,
) {
    RaahiCard(
        borderColor = if (slot.isReady) AppColors.Primary else AppColors.Border,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(slot.label, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                if (slot.isReady) {
                    VerificationBadgeChip(isVerified = true, label = "Preview ready")
                }
            }

            if (slot.bitmap != null) {
                Image(
                    bitmap = slot.bitmap.asImageBitmap(),
                    contentDescription = "${slot.label} preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(128.dp).clip(RoundedCornerShape(AppSpacing.radiusInput)),
                )
            } else if (slot.sampleSelected) {
                Surface(
                    shape = RoundedCornerShape(AppSpacing.radiusInput),
                    color = AppColors.PrimarySoft,
                    modifier = Modifier.fillMaxWidth().height(76.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(AppSpacing.s12),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Badge, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column {
                            Text("Sample ${slot.label} loaded", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                            Text("Stored in local memory only", style = AppTextStyles.caption, color = AppColors.PrimaryDark)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                RaahiSecondaryButton(
                    text = "Gallery",
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = onPickGallery,
                    modifier = Modifier.weight(1f),
                )
                RaahiSecondaryButton(
                    text = "Camera",
                    icon = Icons.Rounded.CameraAlt,
                    onClick = onTakeCamera,
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(
                onClick = onUseSample,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text("Use sample preview", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
            }
        }
    }
}

@Composable
fun VerificationStatusScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onSimulateOutcome: (VerificationStatus, Boolean) -> Unit,
    onReuploadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = state.user
    val status = user.verificationStatus

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = "Verification status",
                subtitle = user.effectiveVerificationLevel.label,
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
            val (bg, icon, tint, heading, desc) = when (status) {
                VerificationStatus.APPROVED -> StatusVisual(
                    AppColors.VerifiedBg, Icons.Rounded.VerifiedUser, AppColors.VerifiedText,
                    "Verification Approved",
                    "Your profile carries the green Verified badge (${user.verificationLevel.label}).",
                )
                VerificationStatus.REJECTED -> StatusVisual(
                    AppColors.SosBg, Icons.Rounded.ErrorOutline, AppColors.Error,
                    "Verification Rejected",
                    user.verificationRejectionReason ?: "One of your uploaded documents could not be verified clearly.",
                )
                VerificationStatus.PENDING -> StatusVisual(
                    AppColors.AccentSoft, Icons.Rounded.HourglassTop, AppColors.AccentOnSoft,
                    "Verification Pending",
                    "Your documents are queued for review in this prototype. Use the demo controls below to preview Approved or Rejected states.",
                )
                VerificationStatus.NOT_STARTED -> StatusVisual(
                    AppColors.SurfaceSubtle, Icons.Rounded.PersonOutline, AppColors.TextSecondary,
                    "Not Verified Yet (Basic)",
                    "Complete CNIC & Selfie (or full Driver docs) to unlock Verified-Only rides.",
                )
            }

            RaahiCard(containerColor = bg, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(36.dp))
                    Text(heading, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text(desc, style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Verification badges", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    BadgeStatusRow("CNIC Identity", user.cnicVerified)
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    BadgeStatusRow("Driving Licence", user.licenceVerified)
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    BadgeStatusRow("Vehicle Documents", user.vehicleVerified)
                }
            }

            if (status == VerificationStatus.REJECTED || status == VerificationStatus.NOT_STARTED) {
                RaahiPrimaryButton(
                    text = if (status == VerificationStatus.REJECTED) "Re-upload Documents" else "Start Verification",
                    onClick = onReuploadClick,
                    modifier = Modifier.testTag("verification_reupload_button"),
                )
            }

            RaahiCard(containerColor = AppColors.PrimarySoft, borderColor = AppColors.Primary.copy(alpha = 0.25f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                    Text("Prototype Status Simulator", style = AppTextStyles.cardTitle, color = AppColors.PrimaryDark)
                    Text("Preview all three verification states locally:", style = AppTextStyles.caption, color = AppColors.PrimaryDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                        RaahiSecondaryButton(
                            text = "Approve",
                            onClick = { onSimulateOutcome(VerificationStatus.APPROVED, true) },
                            textColor = AppColors.VerifiedText,
                            borderColor = AppColors.VerifiedText.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f),
                        )
                        RaahiSecondaryButton(
                            text = "Pending",
                            onClick = { onSimulateOutcome(VerificationStatus.PENDING, true) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    RaahiSecondaryButton(
                        text = "Reject (Demo)",
                        onClick = { onSimulateOutcome(VerificationStatus.REJECTED, true) },
                        textColor = AppColors.Error,
                        borderColor = AppColors.Error.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

private data class StatusVisual(
    val bg: Color,
    val icon: ImageVector,
    val tint: Color,
    val heading: String,
    val desc: String,
)

@Composable
private fun BadgeStatusRow(label: String, verified: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = AppTextStyles.bodyCompact, color = AppColors.TextPrimary)
        VerificationBadgeChip(isVerified = verified, label = if (verified) "Verified" else "Not Verified")
    }
}

@Composable
fun PublicProfileScreen(
    driverId: String,
    state: CarPoolUiState,
    onBack: () -> Unit,
    onToggleFollow: (String) -> Unit,
    onReportUser: (String, String) -> Unit,
    onBlockUser: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val driver = state.driversById[driverId]
    if (driver == null) {
        Scaffold(
            containerColor = AppColors.Background,
            topBar = { RaahiTopBar(title = "Public profile", onBack = onBack) },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Profile not found.", style = AppTextStyles.body, color = AppColors.TextSecondary)
            }
        }
        return
    }

    val isFollowed = driver.id in state.followedDriverIds
    val reviews = remember(state.reviews, driver.id) { state.reviews.filter { it.driverId == driver.id } }
    var showBlockConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = driver.name,
                subtitle = "Member since ${driver.memberSince}",
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item(key = "public_profile_card", contentType = "profile_header") {
                RaahiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s12)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarBadge(
                                initials = driver.initials,
                                backgroundColor = AppColors.PrimarySoft,
                                isVerified = driver.isVerified,
                                size = 64.dp,
                            )
                            Spacer(Modifier.width(AppSpacing.s16))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(driver.name, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                                Spacer(Modifier.height(AppSpacing.s4))
                                VerificationBadgeChip(
                                    isVerified = driver.isVerified,
                                    label = driver.verificationLevel.label,
                                )
                            }
                        }

                        Text(driver.bio, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatCol("${driver.rating} ★", "${driver.reviewCount} reviews")
                            StatCol("${driver.tripsCompleted}", "Rides completed")
                            StatCol("${driver.reliabilityScore}%", "Reliability")
                        }

                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        BadgeStatusRow("CNIC Verified", driver.cnicVerified)
                        BadgeStatusRow("Licence Verified", driver.licenceVerified)
                        BadgeStatusRow("Vehicle Verified", driver.vehicleVerified)

                        if (driver.id != "D-ME") {
                            Spacer(Modifier.height(AppSpacing.s4))
                            if (isFollowed) {
                                RaahiSecondaryButton(
                                    text = "Following Driver",
                                    icon = Icons.Rounded.Favorite,
                                    onClick = { onToggleFollow(driver.id) },
                                    textColor = AppColors.Primary,
                                    borderColor = AppColors.Primary,
                                    modifier = Modifier.testTag("public_profile_follow_button"),
                                )
                            } else {
                                RaahiPrimaryButton(
                                    text = "Follow Driver",
                                    icon = Icons.Rounded.FavoriteBorder,
                                    onClick = { onToggleFollow(driver.id) },
                                    modifier = Modifier.testTag("public_profile_follow_button"),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                            ) {
                                RaahiSecondaryButton(
                                    text = "Report",
                                    onClick = { onReportUser(driver.id, driver.name) },
                                    modifier = Modifier.weight(1f),
                                )
                                RaahiSecondaryButton(
                                    text = "Block",
                                    onClick = { showBlockConfirm = true },
                                    textColor = AppColors.Error,
                                    borderColor = AppColors.Error.copy(alpha = 0.4f),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            item(key = "reviews_header", contentType = "section_header") {
                Text("Passenger reviews (${reviews.size})", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            }
            items(reviews, key = { it.id }, contentType = { "review" }) { rev ->
                RaahiCard(elevated = false, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(rev.authorName, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                            Text("${rev.rating} ★ • ${rev.dateLabel}", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                        }
                        Text(rev.comment, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }

    if (showBlockConfirm) {
        RaahiConfirmDialog(
            title = "Block ${driver.name}?",
            message = "Their rides will be hidden from search results. You can unblock them anytime in Settings → Blocked Users.",
            confirmLabel = "Block",
            isDestructive = true,
            onConfirm = {
                showBlockConfirm = false
                onBlockUser(driver.id, driver.name, driver.initials)
                onBack()
            },
            onDismiss = { showBlockConfirm = false },
        )
    }
}

@Composable
private fun StatCol(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AppTextStyles.cardTitle, fontWeight = FontWeight.Bold, color = AppColors.Primary)
        Text(label, style = AppTextStyles.caption, color = AppColors.TextSecondary)
    }
}
