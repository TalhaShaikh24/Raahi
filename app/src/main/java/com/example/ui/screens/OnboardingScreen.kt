package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(900)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable { onFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(AppSpacing.s32),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppColors.Primary,
                modifier = Modifier.size(84.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = "Raahi logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.s20))
            Text(
                text = "Raahi",
                style = AppTextStyles.screenTitle,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(AppSpacing.s8))
            Text(
                text = "Planned Carpooling Across Pakistan",
                style = AppTextStyles.bodyCompact,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppSpacing.s24))
            LinearProgressIndicator(
                color = AppColors.Primary,
                trackColor = AppColors.Border,
                modifier = Modifier
                    .width(140.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String,
)

@Composable
fun OnboardingScreen(
    onFinishSlides: () -> Unit,
    onExploreDemoDirectly: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Share planned intercity journeys",
                subtitle = "Connect with drivers heading between Karachi, Hyderabad, Lahore, and Islamabad at fair shared fuel fares.",
                icon = Icons.Rounded.DirectionsCar,
                badgeText = "Community Carpooling",
            ),
            OnboardingSlide(
                title = "Agree on a fair PKR fare",
                subtitle = "Book at the listed price or propose a counter-offer with a clear step-by-step offer timeline.",
                icon = Icons.Rounded.Handshake,
                badgeText = "Counter-Offer Negotiation",
            ),
            OnboardingSlide(
                title = "Travel with Basic or Verified",
                subtitle = "Start immediately as Basic or complete ID & vehicle verification to unlock Verified-Only rides and Live SOS tools.",
                icon = Icons.Rounded.VerifiedUser,
                badgeText = "Safety & Verification",
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Surface(
        color = AppColors.Background,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Raahi",
                        style = AppTextStyles.screenTitle,
                        color = AppColors.Primary,
                    )
                    TextButton(
                        onClick = onFinishSlides,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("onboarding_skip_button"),
                    ) {
                        Text(
                            text = "Skip",
                            style = AppTextStyles.button,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_onboarding),
                            contentDescription = "Raahi carpooling illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(AppColors.PrimarySoft),
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.s24))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val slide = slides[page]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.s4),
                        ) {
                            RaahiPill(
                                text = slide.badgeText,
                                containerColor = AppColors.PrimarySoft,
                                contentColor = AppColors.PrimaryDark,
                                icon = slide.icon,
                            )
                            Spacer(Modifier.height(AppSpacing.s12))
                            Text(
                                text = slide.title,
                                style = AppTextStyles.screenTitle,
                                textAlign = TextAlign.Center,
                                color = AppColors.TextPrimary,
                            )
                            Spacer(Modifier.height(AppSpacing.s8))
                            Text(
                                text = slide.subtitle,
                                style = AppTextStyles.bodyCompact,
                                textAlign = TextAlign.Center,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }

                    Spacer(Modifier.height(AppSpacing.s20))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(slides.size) { idx ->
                            val selected = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = AppSpacing.s4)
                                    .height(8.dp)
                                    .width(if (selected) 24.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) AppColors.Primary else AppColors.Border),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
                    RaahiPrimaryButton(
                        text = if (pagerState.currentPage < slides.lastIndex) "Next" else "Continue to Sign In",
                        onClick = {
                            if (pagerState.currentPage < slides.lastIndex) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                onFinishSlides()
                            }
                        },
                        modifier = Modifier.testTag("onboarding_next_button"),
                    )

                    TextButton(
                        onClick = onExploreDemoDirectly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_explore_demo_button"),
                    ) {
                        Text(
                            text = "Explore demo directly",
                            style = AppTextStyles.button,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtpLoginScreen(
    initialPhone: String,
    onOtpVerified: (String) -> Unit,
    onSkipToDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phone by remember { mutableStateOf(initialPhone.ifBlank { "+92 300 5550192" }) }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("1234") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Surface(
        color = AppColors.Background,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s24),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s16)) {
                    RaahiPill(
                        text = "UI-Only OTP Demo • Code: 1234",
                        containerColor = AppColors.PrimarySoft,
                        contentColor = AppColors.PrimaryDark,
                        icon = Icons.Rounded.LockOpen,
                    )

                    Text(
                        text = "Welcome to Raahi",
                        style = AppTextStyles.screenTitle,
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = "Sign in with your mobile number. No real SMS or backend verification is performed in this prototype.",
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.TextSecondary,
                    )

                    RaahiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
                        ) {
                            RaahiTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it
                                    errorMsg = null
                                },
                                label = "Mobile number",
                                placeholder = "+92 300 5550192",
                                leadingIcon = Icons.Rounded.Phone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                fieldTestTag = "otp_phone_input",
                            )

                            if (!otpSent) {
                                RaahiPrimaryButton(
                                    text = "Send demo OTP",
                                    onClick = {
                                        if (phone.trim().length < 7) {
                                            errorMsg = "Please enter a valid phone number."
                                        } else {
                                            otpSent = true
                                            errorMsg = null
                                        }
                                    },
                                    modifier = Modifier.testTag("otp_send_button"),
                                )
                            } else {
                                RaahiTextField(
                                    value = otpCode,
                                    onValueChange = {
                                        otpCode = it.filter { c -> c.isDigit() }.take(4)
                                        errorMsg = null
                                    },
                                    label = "4-digit OTP code (demo: 1234)",
                                    placeholder = "1234",
                                    leadingIcon = Icons.Rounded.Pin,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    fieldTestTag = "otp_code_input",
                                )

                                RaahiPrimaryButton(
                                    text = "Verify & Continue",
                                    onClick = {
                                        if (otpCode.length < 4) {
                                            errorMsg = "Enter any 4-digit code (e.g. 1234)."
                                        } else {
                                            onOtpVerified(phone)
                                        }
                                    },
                                    modifier = Modifier.testTag("otp_verify_button"),
                                )
                            }

                            if (errorMsg != null) {
                                Text(
                                    text = errorMsg!!,
                                    style = AppTextStyles.caption,
                                    color = AppColors.Error,
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onSkipToDemo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("otp_skip_demo_button"),
                ) {
                    Text(
                        text = "Skip straight to demo home",
                        style = AppTextStyles.button,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    initialName: String,
    initialDob: String,
    initialGender: String,
    initialEmergencyName: String,
    initialEmergencyPhone: String,
    onComplete: (name: String, dob: String, gender: String, emergencyName: String, emergencyPhone: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(initialName) }
    var birthYear by remember { mutableStateOf(initialDob.substringBefore("-").ifBlank { "1998" }) }
    var birthMonthDay by remember { mutableStateOf(initialDob.substringAfter("-", "05-14")) }
    var gender by remember { mutableStateOf(initialGender) }
    var emergencyName by remember { mutableStateOf(initialEmergencyName) }
    var emergencyPhone by remember { mutableStateOf(initialEmergencyPhone) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val parsedYear = birthYear.toIntOrNull()
    val ageApprox = if (parsedYear != null) 2026 - parsedYear else 0

    Surface(
        color = AppColors.Background,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s20),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            Text("Set up your profile", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
            Text(
                text = "Required for both Basic and Verified members to keep trips accountable.",
                style = AppTextStyles.bodyCompact,
                color = AppColors.TextSecondary,
            )

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
                ) {
                    RaahiTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = null },
                        label = "Full name",
                        placeholder = "Enter your full name",
                        fieldTestTag = "profile_setup_name",
                    )

                    Text("Date of Birth (18+ required)", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12), modifier = Modifier.fillMaxWidth()) {
                        RaahiTextField(
                            value = birthYear,
                            onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4); errorMsg = null },
                            label = "Birth year",
                            placeholder = "1998",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            fieldTestTag = "profile_setup_birth_year",
                            modifier = Modifier.weight(1f),
                        )
                        RaahiTextField(
                            value = birthMonthDay,
                            onValueChange = { birthMonthDay = it },
                            label = "MM-DD",
                            placeholder = "05-14",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (parsedYear != null && ageApprox < 18) {
                        Surface(
                            shape = RoundedCornerShape(AppSpacing.radiusInput),
                            color = AppColors.SosBg,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "You must be at least 18 years old to join Raahi (age check: $ageApprox yrs).",
                                style = AppTextStyles.caption,
                                color = AppColors.Error,
                                modifier = Modifier.padding(AppSpacing.s12),
                            )
                        }
                    }

                    Text("Gender", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    ) {
                        listOf("Male", "Female", "Prefer not to say").forEach { opt ->
                            FilterChip(
                                selected = gender == opt,
                                onClick = { gender = opt },
                                label = { Text(opt, style = AppTextStyles.chip, maxLines = 1) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                }
            }

            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
                ) {
                    Text("Emergency Contact (required)", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    RaahiTextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it; errorMsg = null },
                        label = "Contact name",
                        placeholder = "e.g. Tariq Mahmood",
                        fieldTestTag = "profile_setup_emergency_name",
                    )
                    RaahiTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it; errorMsg = null },
                        label = "Contact phone",
                        placeholder = "+92 300 0000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        fieldTestTag = "profile_setup_emergency_phone",
                    )
                }
            }

            if (errorMsg != null) {
                Text(errorMsg!!, style = AppTextStyles.caption, color = AppColors.Error)
            }

            RaahiPrimaryButton(
                text = "Continue",
                onClick = {
                    val y = birthYear.toIntOrNull()
                    when {
                        name.isBlank() -> errorMsg = "Please enter your full name."
                        y == null || (2026 - y) < 18 -> errorMsg = "Must be 18+ years old (enter birth year 2008 or earlier)."
                        emergencyName.isBlank() || emergencyPhone.isBlank() -> errorMsg = "Emergency contact name and phone are required."
                        else -> onComplete(name, "$y-$birthMonthDay", gender, emergencyName, emergencyPhone)
                    }
                },
                modifier = Modifier.testTag("profile_setup_continue"),
            )
            Spacer(Modifier.height(AppSpacing.s16))
        }
    }
}

@Composable
fun ChooseVerificationModeScreen(
    onContinueAsBasic: () -> Unit,
    onGetVerified: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = AppColors.Background,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s24),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
        ) {
            Text("Choose how to continue", style = AppTextStyles.screenTitle, color = AppColors.TextPrimary)
            Text(
                text = "Start exploring immediately as a Basic member, or verify your ID to unlock Verified-Only rides and driver publishing.",
                style = AppTextStyles.bodyCompact,
                color = AppColors.TextSecondary,
            )

            RaahiCard(
                onClick = onGetVerified,
                borderColor = AppColors.Primary,
                borderWidth = 1.5.dp,
                modifier = Modifier.fillMaxWidth().testTag("choose_verified_card"),
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RaahiPill(
                            text = "Recommended",
                            containerColor = AppColors.PrimarySoft,
                            contentColor = AppColors.PrimaryDark,
                        )
                        Icon(
                            Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text("Get Verified", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text(
                        text = "Complete CNIC + Selfie (passengers) or add Licence & Vehicle docs (drivers). Unlocks Verified-Only rides & green Verified badge.",
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.TextSecondary,
                    )
                    RaahiPrimaryButton(
                        text = "Start Verification",
                        onClick = onGetVerified,
                    )
                }
            }

            RaahiCard(
                onClick = onContinueAsBasic,
                modifier = Modifier.fillMaxWidth().testTag("choose_basic_card"),
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RaahiPill(
                            text = "Not Verified Badge",
                            containerColor = AppColors.UnverifiedBg,
                            contentColor = AppColors.UnverifiedText,
                        )
                        Icon(
                            Icons.Rounded.PersonOutline,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text("Continue as Basic", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                    Text(
                        text = "Enter the app immediately with a grey 'Not Verified' badge. Book any 'All Users' ride, and verify anytime later from Profile.",
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.TextSecondary,
                    )
                    RaahiSecondaryButton(
                        text = "Enter as Basic",
                        onClick = onContinueAsBasic,
                    )
                }
            }
        }
    }
}
