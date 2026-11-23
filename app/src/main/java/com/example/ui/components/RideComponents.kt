package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun DriverMiniProfile(
    driver: DriverProfile,
    vehicle: Vehicle?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.let { mod ->
            if (onClick != null) mod.clip(RoundedCornerShape(AppSpacing.radiusInput)).clickable(onClick = onClick) else mod
        },
    ) {
        AvatarBadge(
            initials = driver.initials,
            backgroundColor = AppColors.PrimarySoft,
            isVerified = driver.isVerified,
            size = 48.dp,
        )
        Spacer(Modifier.width(AppSpacing.s12))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                Text(
                    text = driver.name,
                    style = AppTextStyles.cardTitle,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                VerificationBadgeChip(
                    isVerified = driver.isVerified,
                    label = if (driver.isVerified) "Verified" else "Not Verified",
                )
            }
            if (vehicle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${vehicle.makeModel} • ${vehicle.color}",
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(AppSpacing.s8))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Rating",
                tint = AppColors.Accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(AppSpacing.s4))
            Text(
                text = "${driver.rating}",
                style = AppTextStyles.bodyCompact,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RideResultCard(
    ride: Ride,
    driver: DriverProfile?,
    vehicle: Vehicle?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
) {
    val priceFormatted = androidx.compose.runtime.remember(ride.pricePerSeatPkr) {
        Formatters.formatPkr(ride.pricePerSeatPkr)
    }
    val durationFormatted = androidx.compose.runtime.remember(ride.departureDateLabel, ride.durationMinutes) {
        "${ride.departureDateLabel} • ${Formatters.formatDuration(ride.durationMinutes)}"
    }

    RaahiCard(
        onClick = onClick,
        elevated = elevated,
        modifier = modifier
            .fillMaxWidth()
            .testTag("ride_card_${ride.id}"),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
            // Top Row: Avatar + Name + Verified badge + Rating (right)
            if (driver != null) {
                DriverMiniProfile(driver = driver, vehicle = vehicle)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(ride.routeMatchLabel, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                    Text(ride.departureDateLabel, style = AppTextStyles.caption, color = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(AppSpacing.s16))

            // Middle: Route timeline (primary dot -> vertical line -> outline dot) with From/To + times
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = AppSpacing.s12),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AppColors.Primary),
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(26.dp)
                            .background(AppColors.Border),
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AppColors.Surface)
                            .border(2.dp, AppColors.Primary, CircleShape),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s12),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${ride.originCity} • ${ride.originArea}",
                            style = AppTextStyles.body,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                        )
                        Text(
                            text = ride.departureTimeLabel,
                            style = AppTextStyles.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${ride.destinationCity} • ${ride.destinationArea}",
                            style = AppTextStyles.body,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                        )
                        Text(
                            text = durationFormatted,
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppSpacing.s12),
                color = AppColors.Border,
                thickness = 1.dp,
            )

            // Bottom Row: Seats chip, Ride type chip, Bold Price right-aligned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                    modifier = Modifier.weight(1f).padding(end = AppSpacing.s8),
                ) {
                    RaahiPill(
                        text = if (ride.availableSeats == 0) "Full • Waitlist" else "${ride.availableSeats} of ${ride.totalSeats} seats",
                        containerColor = if (ride.availableSeats == 0) AppColors.SosBg else AppColors.SurfaceSubtle,
                        contentColor = if (ride.availableSeats == 0) AppColors.Error else AppColors.TextPrimary,
                    )
                    if (ride.preferences.womenOnly) {
                        RaahiPill(
                            text = "Female-Only",
                            containerColor = AppColors.FemaleOnlyBg,
                            contentColor = AppColors.FemaleOnlyText,
                        )
                    } else if (ride.audience == RideAudience.VERIFIED_ONLY) {
                        RaahiPill(
                            text = "Verified Only",
                            containerColor = AppColors.PrimarySoft,
                            contentColor = AppColors.PrimaryDark,
                        )
                    } else {
                        RaahiPill(
                            text = "All Users",
                            containerColor = AppColors.SurfaceSubtle,
                            contentColor = AppColors.TextSecondary,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = priceFormatted,
                        style = AppTextStyles.price,
                        color = AppColors.Primary,
                        maxLines = 1,
                    )
                    Text(
                        text = "per seat",
                        style = AppTextStyles.caption,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun MetaIconText(
    icon: ImageVector,
    text: String,
    tint: Color = AppColors.TextSecondary,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(AppSpacing.s4))
        Text(
            text = text,
            style = AppTextStyles.caption,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RouteTimelineCard(
    stops: List<RideStop>,
    selectedPickup: String? = null,
    selectedDropoff: String? = null,
    onStopClick: ((RideStop, Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    RaahiCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
            Text(
                text = if (onStopClick != null) "Route & stops (tap to pick stop)" else "Route & stops",
                style = AppTextStyles.cardTitle,
                color = AppColors.TextPrimary,
            )
            Spacer(Modifier.height(AppSpacing.s12))
            stops.forEachIndexed { index, stop ->
                val isFirst = index == 0
                val isLast = index == stops.lastIndex
                val isSelectedPickup = selectedPickup == stop.area
                val isSelectedDropoff = selectedDropoff == stop.area

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { mod ->
                            if (onStopClick != null) {
                                mod
                                    .clip(RoundedCornerShape(AppSpacing.s8))
                                    .clickable { onStopClick(stop, index) }
                                    .padding(vertical = AppSpacing.s4, horizontal = AppSpacing.s4)
                            } else {
                                mod
                            }
                        },
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = stop.timeLabel,
                        style = AppTextStyles.caption,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.width(68.dp),
                        maxLines = 1,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = AppSpacing.s8),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isFirst || isLast) 12.dp else 9.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelectedPickup || isFirst -> AppColors.Primary
                                        isSelectedDropoff || isLast -> AppColors.Surface
                                        else -> AppColors.Border
                                    }
                                )
                                .let { m ->
                                    if (isLast || isSelectedDropoff) {
                                        m.border(2.dp, AppColors.Primary, CircleShape)
                                    } else {
                                        m
                                    }
                                },
                        )
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(26.dp)
                                    .background(AppColors.Border),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stop.area,
                            style = AppTextStyles.bodyCompact,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stop.city,
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isSelectedPickup || isSelectedDropoff) {
                        Spacer(Modifier.width(AppSpacing.s8))
                        RaahiPill(
                            text = if (isSelectedPickup) "Pickup" else "Drop-off",
                            containerColor = if (isSelectedPickup) AppColors.PrimarySoft else AppColors.VerifiedBg,
                            contentColor = if (isSelectedPickup) AppColors.PrimaryDark else AppColors.VerifiedText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleInfoCard(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
) {
    RaahiCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.PrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = "Vehicle",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(AppSpacing.s12))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.makeModel,
                    style = AppTextStyles.cardTitle,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${vehicle.color} • ${vehicle.plateNumber}",
                    style = AppTextStyles.caption,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppSpacing.s4))
                Text(
                    text = "${vehicle.seatCapacity} passenger seats • ${vehicle.luggageCapacity.label}",
                    style = AppTextStyles.chip,
                    color = AppColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
