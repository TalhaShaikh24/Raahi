package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------------------------
// Splash
// ---------------------------------------------------------------------------------------------

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoScale = remember { Animatable(0.4f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) {
        delay(250)
        contentAlpha.animateTo(1f, tween(RaahiMotion.LONG))
    }
    LaunchedEffect(Unit) {
        delay(1600)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppGradients.Hero)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished,
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .size(96.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(AppGradients.Brand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = "Raahi logo",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp),
                )
            }
            Spacer(Modifier.height(AppSpacing.s24))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha.value },
            ) {
                Text("Raahi", style = AppTextStyles.display, color = Color.White)
                Spacer(Modifier.height(AppSpacing.s8))
                Text(
                    text = "Share the ride. Split the cost.",
                    style = AppTextStyles.body,
                    color = AppColors.Mint,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(AppSpacing.s32))
                DrivingRoad(modifier = Modifier.width(180.dp))
            }
        }
    }
}

/** A tiny car that drives along a dashed road, looping while the app loads. */
@Composable
private fun DrivingRoad(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "road")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "carProgress",
    )
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.height(28.dp)) {
        val travelPx = with(density) { (maxWidth - 24.dp).toPx() }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(9) {
                Box(
                    Modifier
                        .width(12.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.25f)),
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.DirectionsCar,
            contentDescription = null,
            tint = AppColors.Mint,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(24.dp)
                .graphicsLayer {
                    translationX = progress * travelPx
                    alpha = when {
                        progress < 0.1f -> progress / 0.1f
                        progress > 0.9f -> (1f - progress) / 0.1f
                        else -> 1f
                    }
                },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Welcome (single screen, replaces the old 3-slide carousel)
// ---------------------------------------------------------------------------------------------

private data class WelcomePoint(val icon: ImageVector, val title: String, val subtitle: String)

@Composable
fun OnboardingScreen(
    onFinishSlides: () -> Unit,
    onExploreDemoDirectly: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = remember {
        listOf(
            WelcomePoint(Icons.Rounded.Route, "Find a ride", "Drivers already going your way"),
            WelcomePoint(Icons.Rounded.Forum, "Chat & agree", "Talk and settle the fare in one chat"),
            WelcomePoint(Icons.Rounded.Savings, "Save money", "Split fuel costs fairly"),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Ink),
    ) {
        // Hero image with a dark gradient so text stays readable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_onboarding),
                contentDescription = "People sharing a car ride",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = 1.08f; scaleY = 1.08f }
                    .floating(amplitude = 5.dp, periodMillis = 5200),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to AppColors.Ink.copy(alpha = 0.55f),
                            0.45f to Color.Transparent,
                            1f to AppColors.Ink,
                        )
                    ),
            )
            Row(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16)
                    .enterAnimation(index = 0),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppGradients.Brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(AppSpacing.s8))
                Text("Raahi", style = AppTextStyles.sectionTitle, color = Color.White)
            }
        }

        // Content panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s20),
        ) {
            Text(
                text = "Going the same way?\nRide together.",
                style = AppTextStyles.display,
                color = Color.White,
                modifier = Modifier.enterAnimation(index = 1),
            )
            Spacer(Modifier.height(AppSpacing.s24))

            points.forEachIndexed { idx, point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.s8)
                        .enterAnimation(index = idx + 2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(point.icon, contentDescription = null, tint = AppColors.Mint, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(AppSpacing.s16))
                    Column {
                        Text(point.title, style = AppTextStyles.cardTitle, color = Color.White)
                        Text(point.subtitle, style = AppTextStyles.caption, color = Color.White.copy(alpha = 0.65f))
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.s24))

            RaahiPrimaryButton(
                text = "Get started",
                icon = Icons.Rounded.ArrowForward,
                onClick = onFinishSlides,
                modifier = Modifier
                    .enterAnimation(index = 5)
                    .clip(RoundedCornerShape(AppSpacing.radiusButton))
                    .shimmerSweep()
                    .testTag("onboarding_next_button"),
            )
            Spacer(Modifier.height(AppSpacing.s8))
            TextButton(
                onClick = onExploreDemoDirectly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .enterAnimation(index = 6)
                    .testTag("onboarding_explore_demo_button"),
            ) {
                Text(
                    text = "Just look around first",
                    style = AppTextStyles.button,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Shared sign-up scaffold (step indicator + title)
// ---------------------------------------------------------------------------------------------

@Composable
private fun SignUpStepScaffold(
    step: Int,
    totalSteps: Int,
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = step / totalSteps.toFloat(),
        animationSpec = tween(RaahiMotion.LONG, easing = FastOutSlowInEasing),
        label = "stepProgress",
    )
    Surface(color = AppColors.Background, modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppGradients.SoftMint)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s20),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Step $step of $totalSteps",
                        style = AppTextStyles.chip,
                        color = AppColors.PrimaryDark,
                    )
                    Spacer(Modifier.width(AppSpacing.s12))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.Border),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(50))
                                .background(AppGradients.BrandHorizontal),
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacing.s32))
                Box(
                    modifier = Modifier
                        .enterAnimation(index = 0)
                        .size(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppGradients.Brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(AppSpacing.s20))
                Text(
                    text = title,
                    style = AppTextStyles.display,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.enterAnimation(index = 1),
                )
                Spacer(Modifier.height(AppSpacing.s8))
                Text(
                    text = subtitle,
                    style = AppTextStyles.body,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.enterAnimation(index = 2),
                )
                Spacer(Modifier.height(AppSpacing.s24))
                Column(
                    modifier = Modifier.enterAnimation(index = 3),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s16),
                    content = content,
                )
                Spacer(Modifier.weight(1f).heightIn(min = AppSpacing.s24))
                footer()
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Step 1: phone number + code
// ---------------------------------------------------------------------------------------------

@Composable
fun OtpLoginScreen(
    initialPhone: String,
    onOtpVerified: (String) -> Unit,
    onSkipToDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phone by remember { mutableStateOf(initialPhone.ifBlank { "+92 300 5550192" }) }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val codeFocus = remember { FocusRequester() }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            delay(RaahiMotion.MEDIUM.toLong())
            runCatching { codeFocus.requestFocus() }
        }
    }

    fun verify() {
        if (otpCode.length < 4) {
            errorMsg = "Enter the 4-digit code (demo: 1234)."
        } else {
            onOtpVerified(phone)
        }
    }

    SignUpStepScaffold(
        step = 1,
        totalSteps = 2,
        icon = if (otpSent) Icons.Rounded.Sms else Icons.Rounded.PhoneAndroid,
        title = if (otpSent) "Enter your code" else "What's your number?",
        subtitle = if (otpSent) "We sent a 4-digit code to $phone. Demo code: 1234" else "We'll send you a code to sign in. No password needed.",
        modifier = modifier,
        footer = {
            TextButton(
                onClick = onSkipToDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("otp_skip_demo_button"),
            ) {
                Text("Skip and look around", style = AppTextStyles.button, color = AppColors.TextSecondary, maxLines = 1)
            }
        },
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            fieldTestTag = "otp_phone_input",
        )

        AnimatedVisibility(
            visible = otpSent,
            enter = expandVertically(tween(RaahiMotion.MEDIUM)) + fadeIn(tween(RaahiMotion.MEDIUM)),
            exit = shrinkVertically() + fadeOut(),
        ) {
            OtpCodeBoxes(
                code = otpCode,
                onCodeChange = { value ->
                    otpCode = value.filter { it.isDigit() }.take(4)
                    errorMsg = null
                    if (otpCode.length == 4) onOtpVerified(phone)
                },
                modifier = Modifier
                    .focusRequester(codeFocus)
                    .testTag("otp_code_input"),
            )
        }

        AnimatedVisibility(visible = errorMsg != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Text(errorMsg.orEmpty(), style = AppTextStyles.caption, color = AppColors.Error)
        }

        if (!otpSent) {
            RaahiPrimaryButton(
                text = "Send code",
                icon = Icons.Rounded.ArrowForward,
                onClick = {
                    if (phone.filter { it.isDigit() }.length < 7) {
                        errorMsg = "Please enter a valid phone number."
                    } else {
                        otpSent = true
                        errorMsg = null
                    }
                },
                modifier = Modifier.testTag("otp_send_button"),
            )
        } else {
            RaahiPrimaryButton(
                text = "Verify",
                icon = Icons.Rounded.Check,
                onClick = ::verify,
                modifier = Modifier.testTag("otp_verify_button"),
            )
            TextButton(onClick = { otpCode = "1234" }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Fill demo code", style = AppTextStyles.button, color = AppColors.Primary)
            }
        }
    }
}

/** Four big code boxes driven by a single hidden text field. */
@Composable
private fun OtpCodeBoxes(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                // The real text field stays invisible; the boxes below show its value.
                Box(Modifier.size(1.dp).graphicsLayer { alpha = 0f }) { innerTextField() }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    repeat(4) { index ->
                        val char = code.getOrNull(index)?.toString() ?: ""
                        val isActive = focused && index == code.length.coerceAtMost(3)
                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isActive -> AppColors.Primary
                                char.isNotEmpty() -> AppColors.Primary.copy(alpha = 0.5f)
                                else -> AppColors.Border
                            },
                            label = "otpBorder",
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (char.isNotEmpty()) 1f else 0.92f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "otpScale",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.Surface)
                                .border(BorderStroke(if (isActive) 2.dp else 1.dp, borderColor), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(char, style = AppTextStyles.display, color = AppColors.TextPrimary)
                        }
                    }
                }
            }
        },
    )
}

// ---------------------------------------------------------------------------------------------
// Step 2: name only
// ---------------------------------------------------------------------------------------------

@Composable
fun NameSetupScreen(
    initialName: String,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(initialName) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (name.isBlank()) errorMsg = "Please enter your name." else onComplete(name)
    }

    SignUpStepScaffold(
        step = 2,
        totalSteps = 2,
        icon = Icons.Rounded.WavingHand,
        title = "What should we call you?",
        subtitle = "That's all we need to get you started. Add more details later, only when you need them.",
        modifier = modifier,
    ) {
        RaahiTextField(
            value = name,
            onValueChange = {
                name = it
                errorMsg = null
            },
            label = "Your name",
            placeholder = "e.g. Ayesha Khan",
            leadingIcon = Icons.Rounded.Person,
            errorText = errorMsg,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            fieldTestTag = "profile_setup_name",
        )
        RaahiPrimaryButton(
            text = "Start riding",
            icon = Icons.Rounded.DirectionsCar,
            onClick = ::submit,
            modifier = Modifier.testTag("profile_setup_continue"),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppSpacing.radiusInput))
                .background(AppColors.Surface)
                .padding(AppSpacing.s12),
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(AppSpacing.s12))
            Text(
                text = "Want the green Verified badge? Verify your ID anytime from Profile.",
                style = AppTextStyles.caption,
                color = AppColors.TextSecondary,
            )
        }
    }
}
