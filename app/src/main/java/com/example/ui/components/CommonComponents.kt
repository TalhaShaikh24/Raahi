package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.mock.MockDataFactory
import com.example.model.AppRole
import com.example.model.BookingStatus
import com.example.model.CounterOfferStatus
import com.example.ui.theme.*

@Composable
fun RaahiCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.Surface,
    borderColor: Color = AppColors.Border,
    borderWidth: Dp = 1.dp,
    elevated: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppSpacing.radiusCard)
    val cardModifier = if (elevated) {
        modifier.shadow(
            elevation = 4.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
        )
    } else {
        modifier
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = cardModifier,
            content = content,
        )
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = cardModifier,
            content = content,
        )
    }
}

@Composable
fun RaahiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = AppColors.Primary,
) {
    val shape = RoundedCornerShape(AppSpacing.radiusButton)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = AppColors.SurfaceSubtle,
            disabledContentColor = AppColors.TextSecondary,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.s16),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 2.dp else 0.dp,
            pressedElevation = 0.dp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AppSpacing.buttonHeight),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(AppSpacing.s8))
            }
            Text(
                text = text,
                style = AppTextStyles.button,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RaahiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    textColor: Color = AppColors.TextPrimary,
    borderColor: Color = AppColors.Border,
) {
    val shape = RoundedCornerShape(AppSpacing.radiusButton)
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        border = BorderStroke(1.dp, if (enabled) borderColor else AppColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.Surface,
            contentColor = textColor,
            disabledContentColor = AppColors.TextSecondary,
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.s16),
        modifier = modifier
            .fillMaxWidth()
            .height(AppSpacing.buttonHeight),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(AppSpacing.s8))
        }
        Text(
            text = text,
            style = AppTextStyles.button,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RaahiDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    RaahiPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        containerColor = AppColors.Error,
    )
}

@Composable
fun RaahiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    prefixText: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    fieldTestTag: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.s8)) {
        Text(
            text = label,
            style = AppTextStyles.inputLabel,
            color = AppColors.TextPrimary,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotBlank()) {
                {
                    Text(
                        text = placeholder,
                        style = AppTextStyles.bodyCompact,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else null,
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp)) }
            },
            prefix = prefixText?.let { p -> { Text(p, style = AppTextStyles.bodyCompact, color = AppColors.TextSecondary) } },
            isError = errorText != null,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(AppSpacing.radiusInput),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
                errorContainerColor = AppColors.Surface,
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Error,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppSpacing.inputHeight)
                .let { mod -> if (fieldTestTag != null) mod.testTag(fieldTestTag) else mod },
        )
        if (!errorText.isNullOrBlank()) {
            Text(
                text = errorText,
                style = AppTextStyles.caption,
                color = AppColors.Error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaahiTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = Modifier.background(AppColors.Surface)) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = AppTextStyles.sectionTitle,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .padding(start = AppSpacing.s12)
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBack)
                            .testTag("top_bar_back"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppColors.SurfaceSubtle),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = AppColors.TextPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.Surface,
                scrolledContainerColor = AppColors.Surface,
                titleContentColor = AppColors.TextPrimary,
            ),
        )
        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
    }
}

@Composable
fun RaahiListRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = AppColors.Primary,
    iconBg: Color = AppColors.PrimarySoft,
    trailing: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    },
    testTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.s16, vertical = AppSpacing.s12)
            .let { mod -> if (testTag != null) mod.testTag(testTag) else mod },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(AppSpacing.s12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTextStyles.body,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(AppSpacing.s8))
        trailing()
    }
}

@Composable
fun AvatarBadge(
    initials: String,
    backgroundColor: Color = AppColors.PrimarySoft,
    isVerified: Boolean = false,
    size: Dp = 48.dp,
    contentColor: Color = Color.White,
) {
    val useLightTint = backgroundColor == AppColors.PrimarySoft
    val resolvedTextColor = if (useLightTint) AppColors.Primary else contentColor
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = if (size >= 60.dp) AppTextStyles.sectionTitle else AppTextStyles.cardTitle,
                color = resolvedTextColor,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isVerified) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.36f)
                    .clip(CircleShape)
                    .background(AppColors.Surface)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(AppColors.VerifiedText),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Verified profile",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f),
                )
            }
        }
    }
}

@Composable
fun RoleModePill(
    activeRole: AppRole,
    onToggle: (AppRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDriver = activeRole == AppRole.DRIVER
    Surface(
        shape = RoundedCornerShape(50),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = modifier.testTag("role_mode_pill"),
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeChipItem(
                label = "Passenger",
                selected = !isDriver,
                onClick = { onToggle(AppRole.PASSENGER) },
                testTag = "role_passenger_chip",
            )
            ModeChipItem(
                label = "Driver",
                selected = isDriver,
                onClick = { onToggle(AppRole.DRIVER) },
                testTag = "role_driver_chip",
            )
        }
    }
}

@Composable
private fun ModeChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) AppColors.Primary else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Text(
            text = label,
            style = AppTextStyles.chip,
            color = if (selected) Color.White else AppColors.TextSecondary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun RaahiPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = AppTextStyles.chip,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun BookingStatusChip(status: BookingStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        BookingStatus.CONFIRMED -> AppColors.VerifiedBg to AppColors.VerifiedText
        BookingStatus.ONGOING -> AppColors.PrimarySoft to AppColors.Primary
        BookingStatus.NEGOTIATING -> AppColors.AccentSoft to AppColors.AccentOnSoft
        BookingStatus.PENDING -> AppColors.PrimarySoft to AppColors.PrimaryDark
        BookingStatus.COMPLETED -> AppColors.VerifiedBg to AppColors.VerifiedText
        BookingStatus.CANCELLED, BookingStatus.REJECTED -> AppColors.SosBg to AppColors.Error
    }
    RaahiPill(text = status.label, containerColor = bg, contentColor = fg, modifier = modifier)
}

@Composable
fun VerificationBadgeChip(
    isVerified: Boolean,
    label: String = if (isVerified) "Verified" else "Not Verified",
    modifier: Modifier = Modifier,
) {
    RaahiPill(
        text = label,
        containerColor = if (isVerified) AppColors.VerifiedBg else AppColors.UnverifiedBg,
        contentColor = if (isVerified) AppColors.VerifiedText else AppColors.UnverifiedText,
        icon = if (isVerified) Icons.Rounded.VerifiedUser else Icons.Rounded.Info,
        modifier = modifier,
    )
}

@Composable
fun OfferStatusChip(status: CounterOfferStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        CounterOfferStatus.PENDING -> AppColors.AccentSoft to AppColors.AccentOnSoft
        CounterOfferStatus.ACCEPTED -> AppColors.VerifiedBg to AppColors.VerifiedText
        CounterOfferStatus.COUNTERED -> AppColors.SurfaceSubtle to AppColors.TextSecondary
        CounterOfferStatus.REJECTED, CounterOfferStatus.EXPIRED -> AppColors.SosBg to AppColors.Error
    }
    RaahiPill(text = status.label, containerColor = bg, contentColor = fg, modifier = modifier)
}

@Composable
fun SeatQuantityStepper(
    seats: Int,
    minSeats: Int = 1,
    maxSeats: Int = 4,
    onSeatsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
        modifier = modifier,
    ) {
        Surface(
            onClick = { if (seats > minSeats) onSeatsChange(seats - 1) },
            enabled = seats > minSeats,
            shape = CircleShape,
            color = if (seats > minSeats) AppColors.PrimarySoft else AppColors.SurfaceSubtle,
            modifier = Modifier.size(40.dp).testTag("seat_decrement_button"),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = "Decrease seats",
                    tint = if (seats > minSeats) AppColors.Primary else AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "$seats",
            style = AppTextStyles.cardTitle,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 24.dp),
        )
        Surface(
            onClick = { if (seats < maxSeats) onSeatsChange(seats + 1) },
            enabled = seats < maxSeats,
            shape = CircleShape,
            color = if (seats < maxSeats) AppColors.PrimarySoft else AppColors.SurfaceSubtle,
            modifier = Modifier.size(40.dp).testTag("seat_increment_button"),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Increase seats",
                    tint = if (seats < maxSeats) AppColors.Primary else AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = AppTextStyles.sectionTitle,
            color = AppColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Text(
                    text = actionLabel,
                    style = AppTextStyles.button,
                    color = AppColors.Primary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    RaahiCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.s24),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.height(AppSpacing.s16))
            Text(
                text = title,
                style = AppTextStyles.cardTitle,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppSpacing.s8))
            Text(
                text = subtitle,
                style = AppTextStyles.bodyCompact,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(AppSpacing.s16))
                RaahiPrimaryButton(
                    text = actionLabel,
                    onClick = onAction,
                )
            }
        }
    }
}

@Composable
fun RaahiConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppSpacing.radiusSheet),
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = title,
                style = AppTextStyles.sectionTitle,
                color = AppColors.TextPrimary,
            )
        },
        text = {
            Text(
                text = message,
                style = AppTextStyles.bodyCompact,
                color = AppColors.TextSecondary,
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                if (isDestructive) {
                    RaahiDangerButton(text = confirmLabel, onClick = onConfirm)
                } else {
                    RaahiPrimaryButton(text = confirmLabel, onClick = onConfirm)
                }
                RaahiSecondaryButton(text = "Cancel", onClick = onDismiss)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    title: String,
    allowAnyOption: Boolean = false,
    onSelect: (city: String, area: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
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
        ) {
            Text(title, style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            Spacer(Modifier.height(AppSpacing.s12))
            RaahiTextField(
                value = query,
                onValueChange = { query = it },
                label = "Search city or area",
                placeholder = "e.g., Clifton, Gulberg, Latifabad",
                leadingIcon = Icons.Rounded.Search,
                fieldTestTag = "location_picker_search",
            )
            Spacer(Modifier.height(AppSpacing.s12))
            val filteredCities = remember(query) {
                MockDataFactory.pakistanLocations.filter { loc ->
                    query.isBlank() ||
                        loc.city.contains(query, ignoreCase = true) ||
                        loc.areas.any { it.contains(query, ignoreCase = true) }
                }
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                if (allowAnyOption && query.isBlank()) {
                    item(key = "anywhere_option", contentType = "anywhere") {
                        Surface(
                            shape = RoundedCornerShape(AppSpacing.radiusInput),
                            color = AppColors.PrimarySoft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AppSpacing.radiusInput))
                                .clickable { onSelect("", "") },
                        ) {
                            Row(
                                modifier = Modifier.padding(AppSpacing.s16),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Explore, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(AppSpacing.s12))
                                Text(
                                    text = "Anywhere (show all cities)",
                                    style = AppTextStyles.body,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.PrimaryDark,
                                )
                            }
                        }
                    }
                }
                items(filteredCities, key = { it.city }, contentType = { "city_group" }) { loc ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppColors.Border, RoundedCornerShape(AppSpacing.radiusInput))
                            .clip(RoundedCornerShape(AppSpacing.radiusInput))
                            .padding(AppSpacing.s12),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(loc.city, loc.areas.first()) }
                                .padding(vertical = AppSpacing.s4),
                        ) {
                            Icon(
                                Icons.Rounded.LocationCity,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(AppSpacing.s8))
                            Text(
                                text = loc.city,
                                style = AppTextStyles.cardTitle,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "Select city",
                                style = AppTextStyles.chip,
                                color = AppColors.Primary,
                            )
                        }
                        Spacer(Modifier.height(AppSpacing.s8))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                        ) {
                            loc.areas.forEach { area ->
                                AssistChip(
                                    onClick = { onSelect(loc.city, area) },
                                    label = { Text(area, style = AppTextStyles.chip, maxLines = 1) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
