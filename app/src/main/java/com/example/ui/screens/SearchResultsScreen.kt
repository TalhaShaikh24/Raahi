package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.data.mock.MockDataFactory
import com.example.model.*
import com.example.ui.CarPoolUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    state: CarPoolUiState,
    onBack: () -> Unit,
    onUpdateQuery: ((RideSearchQuery) -> RideSearchQuery) -> Unit,
    onClearAllFilters: () -> Unit,
    onRideClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFiltersSheet by remember { mutableStateOf(false) }
    var showEditSearchSheet by remember { mutableStateOf(false) }

    val query = state.searchQuery
    val routeTitle = remember(query.originCity, query.destinationCity) {
        "${query.originCity.ifBlank { "Anywhere" }} → ${query.destinationCity.ifBlank { "Anywhere" }}"
    }
    val summarySubtitle = remember(query.dateDayOffset, query.minSeats) {
        val dateText = query.dateDayOffset?.let { MockDataFactory.dateLabelForOffset(it) } ?: "Any date"
        "$dateText • ${Formatters.formatSeats(query.minSeats)}"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        topBar = {
            RaahiTopBar(
                title = routeTitle,
                subtitle = summarySubtitle,
                onBack = onBack,
                actions = {
                    TextButton(
                        onClick = { showEditSearchSheet = true },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("search_edit_button"),
                    ) {
                        Text("Edit", style = AppTextStyles.button, color = AppColors.Primary, maxLines = 1)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s12),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = query.verifiedOnly,
                    onClick = { onUpdateQuery { it.copy(verifiedOnly = !it.verifiedOnly) } },
                    leadingIcon = { Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text(if (query.verifiedOnly) "Verified Drivers Only" else "All Drivers", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("search_verified_only_chip"),
                )

                FilterChip(
                    selected = query.maxPricePkr != null || query.departureWindow != DepartureWindow.ANY || query.acRequired || query.noSmokingOnly || query.petsAllowedOnly || query.womenOnly,
                    onClick = { showFiltersSheet = true },
                    leadingIcon = { Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Filters", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("search_filters_chip"),
                )

                RideSortOption.entries.forEach { sortOption ->
                    FilterChip(
                        selected = query.sortOption == sortOption,
                        onClick = { onUpdateQuery { it.copy(sortOption = sortOption) } },
                        label = { Text(sortOption.label, style = AppTextStyles.chip, maxLines = 1) },
                        shape = RoundedCornerShape(50),
                    )
                }
            }
            HorizontalDivider(color = AppColors.Border, thickness = 1.dp)

            if (state.searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.screenPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateView(
                        icon = Icons.Rounded.SearchOff,
                        title = "No matching rides found",
                        subtitle = "Try broadening your origin, destination, or date filters to see all available carpools.",
                        actionLabel = "Show all rides",
                        onAction = onClearAllFilters,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.s16),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    item(key = "results_count", contentType = "count_label") {
                        Text(
                            text = "${state.searchResults.size} ride${if (state.searchResults.size == 1) "" else "s"} available",
                            style = AppTextStyles.caption,
                            color = AppColors.TextSecondary,
                        )
                    }
                    items(state.searchResults, key = { it.id }, contentType = { "ride_card" }) { ride ->
                        val clickHandler = remember(ride.id, onRideClick) { { onRideClick(ride.id) } }
                        RideResultCard(
                            ride = ride,
                            driver = state.driversById[ride.driverId],
                            vehicle = state.vehiclesById[ride.vehicleId],
                            onClick = clickHandler,
                            elevated = false,
                        )
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        SearchFilterBottomSheet(
            query = query,
            onApply = { updated ->
                onUpdateQuery { updated }
                showFiltersSheet = false
            },
            onReset = {
                onUpdateQuery {
                    RideSearchQuery(
                        originCity = it.originCity,
                        destinationCity = it.destinationCity,
                        dateDayOffset = it.dateDayOffset,
                        minSeats = it.minSeats,
                    )
                }
                showFiltersSheet = false
            },
            onDismiss = { showFiltersSheet = false },
        )
    }

    if (showEditSearchSheet) {
        EditSearchCriteriaSheet(
            query = query,
            onApply = { updated ->
                onUpdateQuery { updated }
                showEditSearchSheet = false
            },
            onDismiss = { showEditSearchSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterBottomSheet(
    query: RideSearchQuery,
    onApply: (RideSearchQuery) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var maxPrice by remember { mutableFloatStateOf((query.maxPricePkr ?: 2500).toFloat()) }
    var useMaxPrice by remember { mutableStateOf(query.maxPricePkr != null) }
    var verifiedOnly by remember { mutableStateOf(query.verifiedOnly) }
    var window by remember { mutableStateOf(query.departureWindow) }
    var acRequired by remember { mutableStateOf(query.acRequired) }
    var noSmoking by remember { mutableStateOf(query.noSmokingOnly) }
    var petsAllowed by remember { mutableStateOf(query.petsAllowedOnly) }
    var womenOnly by remember { mutableStateOf(query.womenOnly) }

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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Filter rides", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
                TextButton(onClick = onReset) {
                    Text("Reset", style = AppTextStyles.button, color = AppColors.Primary)
                }
            }

            Text("Departure time", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                DepartureWindow.entries.forEach { opt ->
                    FilterChip(
                        selected = window == opt,
                        onClick = { window = opt },
                        label = { Text(opt.label, style = AppTextStyles.chip, maxLines = 1) },
                        shape = RoundedCornerShape(50),
                    )
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Max fare per seat", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
                    Text(
                        text = if (useMaxPrice) Formatters.formatPkr(maxPrice.toInt()) else "Any price",
                        style = AppTextStyles.cardTitle,
                        color = AppColors.Primary,
                    )
                }
                Slider(
                    value = maxPrice,
                    onValueChange = {
                        maxPrice = it
                        useMaxPrice = true
                    },
                    valueRange = 300f..2500f,
                    steps = 21,
                )
            }

            Text("Preferences & trust", style = AppTextStyles.inputLabel, color = AppColors.TextPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s8),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.s8),
            ) {
                FilterChip(
                    selected = verifiedOnly,
                    onClick = { verifiedOnly = !verifiedOnly },
                    label = { Text("Verified drivers", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
                FilterChip(
                    selected = acRequired,
                    onClick = { acRequired = !acRequired },
                    label = { Text("AC available", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
                FilterChip(
                    selected = noSmoking,
                    onClick = { noSmoking = !noSmoking },
                    label = { Text("No smoking", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
                FilterChip(
                    selected = petsAllowed,
                    onClick = { petsAllowed = !petsAllowed },
                    label = { Text("Pets allowed", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
                FilterChip(
                    selected = womenOnly,
                    onClick = { womenOnly = !womenOnly },
                    label = { Text("Female-Only", style = AppTextStyles.chip, maxLines = 1) },
                    shape = RoundedCornerShape(50),
                )
            }

            RaahiPrimaryButton(
                text = "Apply filters",
                onClick = {
                    onApply(
                        query.copy(
                            maxPricePkr = if (useMaxPrice) maxPrice.toInt() else null,
                            verifiedOnly = verifiedOnly,
                            departureWindow = window,
                            acRequired = acRequired,
                            noSmokingOnly = noSmoking,
                            petsAllowedOnly = petsAllowed,
                            womenOnly = womenOnly,
                        )
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSearchCriteriaSheet(
    query: RideSearchQuery,
    onApply: (RideSearchQuery) -> Unit,
    onDismiss: () -> Unit,
) {
    var origin by remember { mutableStateOf(query.originCity) }
    var destination by remember { mutableStateOf(query.destinationCity) }
    var seats by remember { mutableIntStateOf(query.minSeats) }
    var pickingField by remember { mutableStateOf<String?>(null) }

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
        ) {
            Text("Edit search", style = AppTextStyles.sectionTitle, color = AppColors.TextPrimary)
            RaahiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingField = "origin" }
                            .padding(AppSpacing.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.TripOrigin, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column {
                            Text("From", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            Text(origin.ifBlank { "Anywhere" }, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        }
                    }
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingField = "destination" }
                            .padding(AppSpacing.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(AppSpacing.s12))
                        Column {
                            Text("To", style = AppTextStyles.caption, color = AppColors.TextSecondary)
                            Text(destination.ifBlank { "Anywhere" }, style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Seats needed", style = AppTextStyles.cardTitle, color = AppColors.TextPrimary)
                SeatQuantityStepper(seats = seats, onSeatsChange = { seats = it })
            }
            RaahiPrimaryButton(
                text = "Update results",
                onClick = {
                    onApply(query.copy(originCity = origin, destinationCity = destination, minSeats = seats))
                },
            )
        }
    }

    if (pickingField != null) {
        CityPickerSheet(
            title = if (pickingField == "origin") "Select origin" else "Select destination",
            allowAnyOption = true,
            onSelect = { city, _ ->
                if (pickingField == "origin") origin = city else destination = city
                pickingField = null
            },
            onDismiss = { pickingField = null },
        )
    }
}
