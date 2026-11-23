package com.example.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Commute
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.AppNotification
import com.example.model.AppRole
import com.example.model.BookingStatus
import com.example.ui.screens.*
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.AppTextStyles

object RaahiRoutes {
    const val ROUTE_EXPLORE = "explore"
    const val ROUTE_TRIPS = "trips"
    const val ROUTE_INBOX = "inbox"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_SEARCH_RESULTS = "search_results"
    const val ROUTE_RIDE_DETAILS = "ride_details/{rideId}"
    const val ROUTE_BOOKING_REQUEST = "booking_request/{rideId}/{offerMode}/{pickup}/{dropoff}"
    const val ROUTE_NEGOTIATION = "negotiation/{bookingId}"
    const val ROUTE_BOOKING_DETAILS = "booking_details/{bookingId}"
    const val ROUTE_PUBLISH_RIDE = "publish_ride"
    const val ROUTE_DRIVER_REQUESTS = "driver_requests"
    const val ROUTE_CHAT = "chat/{conversationId}"
    const val ROUTE_SAFETY = "safety"

    // Section B: Verification & Public Profile
    const val ROUTE_VERIFICATION_WIZARD = "verification_wizard"
    const val ROUTE_VERIFICATION_STATUS = "verification_status"
    const val ROUTE_PUBLIC_PROFILE = "public_profile/{driverId}"

    // Section C: Safety & Live Ride
    const val ROUTE_LIVE_RIDE = "live_ride/{bookingId}"
    const val ROUTE_EMERGENCY_CONTACTS = "emergency_contacts"
    const val ROUTE_TRUSTED_CONTACTS = "trusted_contacts"
    const val ROUTE_REPORT_USER = "report_user?target={target}"
    const val ROUTE_BLOCKED_USERS = "blocked_users"
    const val ROUTE_LOST_FOUND = "lost_found"
    const val ROUTE_SUPPORT_DISPUTE = "support_dispute"

    // Section D: Extra Features
    const val ROUTE_PAYMENT = "payment/{bookingId}"
    const val ROUTE_PAYMENT_HISTORY = "payment_history"
    const val ROUTE_RATE_REVIEW = "rate_review/{bookingId}"
    const val ROUTE_EDIT_PROFILE = "edit_profile"
    const val ROUTE_FAVOURITE_ROUTES = "favourite_routes"
    const val ROUTE_FAVOURITE_DRIVERS = "favourite_drivers"
    const val ROUTE_WAITLIST = "waitlist"
    const val ROUTE_NOTIFICATIONS = "notifications"
    const val ROUTE_REFERRAL_PROMO = "referral_promo"
    const val ROUTE_SAVINGS = "savings"
    const val ROUTE_DRIVER_EARNINGS = "driver_earnings"
    const val ROUTE_MY_VEHICLES = "my_vehicles"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_HELP_CENTER = "help_center"

    fun rideDetails(rideId: String) = "ride_details/$rideId"
    fun bookingRequest(rideId: String, offerMode: Boolean, pickup: String, dropoff: String): String {
        return "booking_request/$rideId/$offerMode/${Uri.encode(pickup)}/${Uri.encode(dropoff)}"
    }
    fun negotiation(bookingId: String) = "negotiation/$bookingId"
    fun bookingDetails(bookingId: String) = "booking_details/$bookingId"
    fun chat(conversationId: String) = "chat/$conversationId"
    fun publicProfile(driverId: String) = "public_profile/$driverId"
    fun liveRide(bookingId: String) = "live_ride/$bookingId"
    fun reportUser(target: String = "") = "report_user?target=${Uri.encode(target)}"
    fun payment(bookingId: String) = "payment/$bookingId"
    fun rateReview(bookingId: String) = "rate_review/$bookingId"
}

@Composable
fun RaahiApp(
    viewModel: CarPoolViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingNavigateToWizard by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(state.entryStage, pendingNavigateToWizard) {
        if (state.entryStage == EntryStage.APP_READY && pendingNavigateToWizard) {
            pendingNavigateToWizard = false
            navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) {
                launchSingleTop = true
            }
        }
    }

    when (state.entryStage) {
        EntryStage.SPLASH -> {
            SplashScreen(onFinished = viewModel::finishSplash)
            return
        }
        EntryStage.ONBOARDING -> {
            OnboardingScreen(
                onFinishSlides = viewModel::finishOnboardingSlides,
                onExploreDemoDirectly = viewModel::skipToDemoApp,
            )
            return
        }
        EntryStage.OTP_LOGIN -> {
            OtpLoginScreen(
                initialPhone = state.user.phone,
                onOtpVerified = viewModel::completeOtpLogin,
                onSkipToDemo = viewModel::skipToDemoApp,
            )
            return
        }
        EntryStage.PROFILE_SETUP -> {
            ProfileSetupScreen(
                initialName = state.user.name,
                initialDob = state.user.dob,
                initialGender = state.user.gender,
                initialEmergencyName = state.user.emergencyContactName,
                initialEmergencyPhone = state.user.emergencyContactPhone,
                onComplete = viewModel::completeProfileSetup,
            )
            return
        }
        EntryStage.CHOOSE_MODE -> {
            ChooseVerificationModeScreen(
                onContinueAsBasic = viewModel::continueAsBasic,
                onGetVerified = {
                    pendingNavigateToWizard = true
                    viewModel.startVerificationFromChooser()
                },
            )
            return
        }
        EntryStage.APP_READY -> Unit
    }

    fun handleNotificationNavigation(notif: AppNotification) {
        viewModel.markNotificationRead(notif.id)
        when {
            notif.targetBookingId != null -> {
                val booking = state.bookings.find { it.id == notif.targetBookingId }
                if (booking?.isIncomingDriverRequest == true) {
                    viewModel.switchRole(AppRole.DRIVER)
                }
                when (booking?.status) {
                    BookingStatus.NEGOTIATING -> navController.navigate(RaahiRoutes.negotiation(notif.targetBookingId))
                    BookingStatus.ONGOING -> navController.navigate(RaahiRoutes.liveRide(notif.targetBookingId))
                    else -> navController.navigate(RaahiRoutes.bookingDetails(notif.targetBookingId))
                }
            }
            notif.targetRideId != null -> {
                navController.navigate(RaahiRoutes.rideDetails(notif.targetRideId))
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        RaahiRoutes.ROUTE_EXPLORE,
        RaahiRoutes.ROUTE_TRIPS,
        RaahiRoutes.ROUTE_INBOX,
        RaahiRoutes.ROUTE_PROFILE,
    )
    val showBottomBar = currentRoute in topLevelRoutes
    val isDriverMode = state.user.activeRole == AppRole.DRIVER

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp && showBottomBar

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppColors.Background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = if (showBottomBar && !useNavRail) {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            } else {
                WindowInsets(0, 0, 0, 0)
            },
            bottomBar = {
                if (showBottomBar && !useNavRail) {
                    Column(modifier = Modifier.background(AppColors.Surface)) {
                        HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                        NavigationBar(
                            containerColor = AppColors.Surface,
                            tonalElevation = 0.dp,
                        ) {
                            TopLevelTabs(
                                currentRoute = currentRoute,
                                isDriverMode = isDriverMode,
                                unreadInboxCount = state.unreadInboxCount,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = AppColors.Surface,
                    ) {
                        Spacer(Modifier.height(AppSpacing.s12))
                        val tabs = buildTabSpecs(isDriverMode, state.unreadInboxCount)
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { TabBadgeIcon(tab, selected) },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = AppTextStyles.chip,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = AppColors.Primary,
                                    selectedTextColor = AppColors.Primary,
                                    indicatorColor = AppColors.PrimarySoft,
                                    unselectedIconColor = AppColors.TextSecondary,
                                    unselectedTextColor = AppColors.TextSecondary,
                                ),
                            )
                        }
                    }
                    VerticalDivider(color = AppColors.Border, thickness = 1.dp)
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = RaahiRoutes.ROUTE_EXPLORE,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                    ) {
                        composable(RaahiRoutes.ROUTE_EXPLORE) {
                            if (isDriverMode) {
                                DriverDashboardScreen(
                                    state = state,
                                    onSwitchRole = viewModel::switchRole,
                                    onPublishRideClick = { navController.navigate(RaahiRoutes.ROUTE_PUBLISH_RIDE) },
                                    onOpenRequests = { navController.navigate(RaahiRoutes.ROUTE_DRIVER_REQUESTS) },
                                    onAcceptRequest = viewModel::acceptDriverRequest,
                                    onRejectRequest = viewModel::rejectDriverRequest,
                                    onOpenNegotiation = { bookingId ->
                                        navController.navigate(RaahiRoutes.negotiation(bookingId))
                                    },
                                    onRideClick = { rideId ->
                                        navController.navigate(RaahiRoutes.rideDetails(rideId))
                                    },
                                    onOpenVerification = { navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_STATUS) },
                                    onOpenEarnings = { navController.navigate(RaahiRoutes.ROUTE_DRIVER_EARNINGS) },
                                )
                            } else {
                                ExploreScreen(
                                    state = state,
                                    onSwitchRole = viewModel::switchRole,
                                    onUpdateQuery = viewModel::updateSearchQuery,
                                    onSwapCities = viewModel::swapSearchCities,
                                    onSearchClicked = { navController.navigate(RaahiRoutes.ROUTE_SEARCH_RESULTS) },
                                    onQuickSearch = { from, to ->
                                        viewModel.applyQuickSearch(from, to)
                                        navController.navigate(RaahiRoutes.ROUTE_SEARCH_RESULTS)
                                    },
                                    onRideClick = { rideId ->
                                        navController.navigate(RaahiRoutes.rideDetails(rideId))
                                    },
                                    onOpenSafety = { navController.navigate(RaahiRoutes.ROUTE_SAFETY) },
                                    onOpenOfferRide = {
                                        viewModel.switchRole(AppRole.DRIVER)
                                        navController.navigate(RaahiRoutes.ROUTE_PUBLISH_RIDE)
                                    },
                                    onOpenNegotiation = { bookingId ->
                                        navController.navigate(RaahiRoutes.negotiation(bookingId))
                                    },
                                    onOpenVerification = { navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) },
                                    onOpenNotifications = { navController.navigate(RaahiRoutes.ROUTE_NOTIFICATIONS) },
                                    onOpenLiveRide = { bookingId ->
                                        navController.navigate(RaahiRoutes.liveRide(bookingId))
                                    },
                                    onOpenFavRoutes = { navController.navigate(RaahiRoutes.ROUTE_FAVOURITE_ROUTES) },
                                )
                            }
                        }

                        composable(RaahiRoutes.ROUTE_TRIPS) {
                            MyTripsScreen(
                                state = state,
                                onOpenBooking = { bookingId ->
                                    navController.navigate(RaahiRoutes.bookingDetails(bookingId))
                                },
                                onOpenNegotiation = { bookingId ->
                                    navController.navigate(RaahiRoutes.negotiation(bookingId))
                                },
                                onOpenLiveRide = { bookingId ->
                                    navController.navigate(RaahiRoutes.liveRide(bookingId))
                                },
                                onOpenRideDetail = { rideId ->
                                    navController.navigate(RaahiRoutes.rideDetails(rideId))
                                },
                                onLeaveWaitlist = viewModel::leaveWaitlist,
                                onOpenRateReview = { bookingId ->
                                    navController.navigate(RaahiRoutes.rateReview(bookingId))
                                },
                                onExploreRides = {
                                    viewModel.switchRole(AppRole.PASSENGER)
                                    navController.navigate(RaahiRoutes.ROUTE_EXPLORE) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_INBOX) {
                            InboxScreen(
                                state = state,
                                onOpenConversation = { convoId ->
                                    navController.navigate(RaahiRoutes.chat(convoId))
                                },
                                onNotificationClick = ::handleNotificationNavigation,
                                onMarkAllNotificationsRead = viewModel::markAllNotificationsRead,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_PROFILE) {
                            ProfileScreen(
                                state = state,
                                onSwitchRole = viewModel::switchRole,
                                onTogglePreviewMode = viewModel::togglePreviewMode,
                                onUpdatePreferences = viewModel::updatePreferences,
                                onAddSavedPlace = viewModel::addSavedPlace,
                                onOpenVerificationWizard = { navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) },
                                onOpenVerificationStatus = { navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_STATUS) },
                                onOpenPublicProfile = { navController.navigate(RaahiRoutes.publicProfile("D-ME")) },
                                onOpenEditProfile = { navController.navigate(RaahiRoutes.ROUTE_EDIT_PROFILE) },
                                onOpenMyVehicles = { navController.navigate(RaahiRoutes.ROUTE_MY_VEHICLES) },
                                onOpenSafety = { navController.navigate(RaahiRoutes.ROUTE_SAFETY) },
                                onOpenEmergencyContacts = { navController.navigate(RaahiRoutes.ROUTE_EMERGENCY_CONTACTS) },
                                onOpenTrustedContacts = { navController.navigate(RaahiRoutes.ROUTE_TRUSTED_CONTACTS) },
                                onOpenFavouriteRoutes = { navController.navigate(RaahiRoutes.ROUTE_FAVOURITE_ROUTES) },
                                onOpenFavouriteDrivers = { navController.navigate(RaahiRoutes.ROUTE_FAVOURITE_DRIVERS) },
                                onOpenWaitlist = { navController.navigate(RaahiRoutes.ROUTE_WAITLIST) },
                                onOpenSavings = { navController.navigate(RaahiRoutes.ROUTE_SAVINGS) },
                                onOpenDriverEarnings = { navController.navigate(RaahiRoutes.ROUTE_DRIVER_EARNINGS) },
                                onOpenPaymentHistory = { navController.navigate(RaahiRoutes.ROUTE_PAYMENT_HISTORY) },
                                onOpenReferralPromo = { navController.navigate(RaahiRoutes.ROUTE_REFERRAL_PROMO) },
                                onOpenSupport = { navController.navigate(RaahiRoutes.ROUTE_SUPPORT_DISPUTE) },
                                onOpenHelpCenter = { navController.navigate(RaahiRoutes.ROUTE_HELP_CENTER) },
                                onOpenSettings = { navController.navigate(RaahiRoutes.ROUTE_SETTINGS) },
                                onResetDemoData = viewModel::resetDemoData,
                                onSignOut = viewModel::logoutToOtp,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_SEARCH_RESULTS) {
                            SearchResultsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onUpdateQuery = viewModel::updateSearchQuery,
                                onClearAllFilters = viewModel::clearAllSearchCriteria,
                                onRideClick = { rideId ->
                                    navController.navigate(RaahiRoutes.rideDetails(rideId))
                                },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_RIDE_DETAILS,
                            arguments = listOf(navArgument("rideId") { type = NavType.StringType }),
                        ) { entry ->
                            val rideId = entry.arguments?.getString("rideId") ?: ""
                            RideDetailsScreen(
                                rideId = rideId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onRequestJoin = { id, offerMode, pickup, dropoff ->
                                    navController.navigate(
                                        RaahiRoutes.bookingRequest(id, offerMode, pickup, dropoff)
                                    )
                                },
                                onOpenSafety = { navController.navigate(RaahiRoutes.ROUTE_SAFETY) },
                                onOpenPublicProfile = { driverId ->
                                    navController.navigate(RaahiRoutes.publicProfile(driverId))
                                },
                                onToggleFollowDriver = viewModel::toggleFollowDriver,
                                onOpenVerification = { navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) },
                                onJoinWaitlist = viewModel::joinWaitlist,
                                onLeaveWaitlist = viewModel::leaveWaitlist,
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_BOOKING_REQUEST,
                            arguments = listOf(
                                navArgument("rideId") { type = NavType.StringType },
                                navArgument("offerMode") { type = NavType.BoolType },
                                navArgument("pickup") { type = NavType.StringType },
                                navArgument("dropoff") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            val rideId = entry.arguments?.getString("rideId") ?: ""
                            val offerMode = entry.arguments?.getBoolean("offerMode") ?: false
                            val pickup = Uri.decode(entry.arguments?.getString("pickup") ?: "")
                            val dropoff = Uri.decode(entry.arguments?.getString("dropoff") ?: "")
                            BookingRequestScreen(
                                rideId = rideId,
                                initialOfferMode = offerMode,
                                pickupPoint = pickup,
                                dropoffPoint = dropoff,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSubmitBooking = { req ->
                                    viewModel.createBooking(req) { created ->
                                        navController.navigate(RaahiRoutes.negotiation(created.id)) {
                                            popUpTo(RaahiRoutes.ROUTE_EXPLORE)
                                        }
                                    }
                                },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_NEGOTIATION,
                            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
                        ) { entry ->
                            val bookingId = entry.arguments?.getString("bookingId") ?: ""
                            CounterOfferNegotiationScreen(
                                bookingId = bookingId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onRespondToOffer = viewModel::respondToPendingOffer,
                                onSubmitCounterOffer = { bId, actor, amount, note ->
                                    viewModel.submitCounterOffer(bId, actor, amount, note)
                                },
                                onSwitchRole = viewModel::switchRole,
                                onOpenBookingDetails = { bId ->
                                    navController.navigate(RaahiRoutes.bookingDetails(bId))
                                },
                                onBrowseOtherRides = {
                                    viewModel.switchRole(AppRole.PASSENGER)
                                    navController.navigate(RaahiRoutes.ROUTE_SEARCH_RESULTS)
                                },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_BOOKING_DETAILS,
                            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
                        ) { entry ->
                            val bookingId = entry.arguments?.getString("bookingId") ?: ""
                            BookingDetailsScreen(
                                bookingId = bookingId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onOpenNegotiation = { bId ->
                                    navController.navigate(RaahiRoutes.negotiation(bId))
                                },
                                onOpenChat = { bId ->
                                    val convoId = viewModel.openConversationForBooking(bId)
                                    if (convoId != null) {
                                        navController.navigate(RaahiRoutes.chat(convoId))
                                    }
                                },
                                onCancelBooking = viewModel::cancelBooking,
                                onOpenSafety = { navController.navigate(RaahiRoutes.ROUTE_SAFETY) },
                                onVerifyStartOtp = viewModel::verifyRideStartOtp,
                                onOpenLiveRide = { bId ->
                                    navController.navigate(RaahiRoutes.liveRide(bId))
                                },
                                onOpenPayment = { bId ->
                                    navController.navigate(RaahiRoutes.payment(bId))
                                },
                                onOpenRateReview = { bId ->
                                    navController.navigate(RaahiRoutes.rateReview(bId))
                                },
                                onOpenLostFound = { navController.navigate(RaahiRoutes.ROUTE_LOST_FOUND) },
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_PUBLISH_RIDE) {
                            PublishRideScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onAddVehicle = viewModel::addVehicle,
                                onPublishRide = viewModel::publishRide,
                                onDoneNavigateDashboard = {
                                    viewModel.switchRole(AppRole.DRIVER)
                                    navController.navigate(RaahiRoutes.ROUTE_EXPLORE) {
                                        popUpTo(RaahiRoutes.ROUTE_EXPLORE) { inclusive = true }
                                    }
                                },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_DRIVER_REQUESTS) {
                            DriverBookingRequestsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onAcceptRequest = viewModel::acceptDriverRequest,
                                onRejectRequest = viewModel::rejectDriverRequest,
                                onOpenNegotiation = { bId ->
                                    navController.navigate(RaahiRoutes.negotiation(bId))
                                },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_CHAT,
                            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
                        ) { entry ->
                            val convoId = entry.arguments?.getString("conversationId") ?: ""
                            ChatScreen(
                                conversationId = convoId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSendMessage = viewModel::sendChatMessage,
                                onMarkRead = viewModel::markConversationRead,
                                onOpenBooking = { bId ->
                                    navController.navigate(RaahiRoutes.bookingDetails(bId))
                                },
                                onOpenPublicProfile = { driverId ->
                                    navController.navigate(RaahiRoutes.publicProfile(driverId))
                                },
                                onReportUser = { name ->
                                    navController.navigate(RaahiRoutes.reportUser(name))
                                },
                                onBlockUser = { userId, name, initials ->
                                    viewModel.blockUser(userId, name, initials, "Blocked from chat")
                                },
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_SAFETY) {
                            SafetySupportScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onOpenEmergencyContacts = { navController.navigate(RaahiRoutes.ROUTE_EMERGENCY_CONTACTS) },
                                onOpenTrustedContacts = { navController.navigate(RaahiRoutes.ROUTE_TRUSTED_CONTACTS) },
                                onOpenReportUser = { navController.navigate(RaahiRoutes.reportUser()) },
                                onOpenBlockedUsers = { navController.navigate(RaahiRoutes.ROUTE_BLOCKED_USERS) },
                                onOpenLostFound = { navController.navigate(RaahiRoutes.ROUTE_LOST_FOUND) },
                                onOpenSupport = { navController.navigate(RaahiRoutes.ROUTE_SUPPORT_DISPUTE) },
                                onOpenHelpCenter = { navController.navigate(RaahiRoutes.ROUTE_HELP_CENTER) },
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        // Section B: Verification & Public Profile
                        composable(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) {
                            VerificationWizardScreen(
                                initialDriverMode = state.user.activeRole == AppRole.DRIVER,
                                onBack = { navController.popBackStack() },
                                onSubmitted = { isDriver ->
                                    viewModel.submitVerificationWizard(isDriver)
                                    navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_STATUS) {
                                        popUpTo(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) { inclusive = true }
                                    }
                                },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_VERIFICATION_STATUS) {
                            VerificationStatusScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSimulateOutcome = viewModel::simulateVerificationOutcome,
                                onReuploadClick = {
                                    navController.navigate(RaahiRoutes.ROUTE_VERIFICATION_WIZARD) {
                                        popUpTo(RaahiRoutes.ROUTE_VERIFICATION_STATUS) { inclusive = true }
                                    }
                                },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_PUBLIC_PROFILE,
                            arguments = listOf(navArgument("driverId") { type = NavType.StringType }),
                        ) { entry ->
                            val driverId = entry.arguments?.getString("driverId") ?: ""
                            PublicProfileScreen(
                                driverId = driverId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onToggleFollow = viewModel::toggleFollowDriver,
                                onReportUser = { _, name ->
                                    navController.navigate(RaahiRoutes.reportUser(name))
                                },
                                onBlockUser = { id, name, initials ->
                                    viewModel.blockUser(id, name, initials, "Blocked from Public Profile")
                                },
                            )
                        }

                        // Section C: Safety & Live Ride
                        composable(
                            route = RaahiRoutes.ROUTE_LIVE_RIDE,
                            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
                        ) { entry ->
                            val bookingId = entry.arguments?.getString("bookingId") ?: ""
                            LiveRideScreen(
                                bookingId = bookingId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onOpenChat = { bId ->
                                    val convoId = viewModel.openConversationForBooking(bId)
                                    if (convoId != null) {
                                        navController.navigate(RaahiRoutes.chat(convoId))
                                    }
                                },
                                onOpenSafetyCenter = { navController.navigate(RaahiRoutes.ROUTE_SAFETY) },
                                onCompleteTrip = viewModel::completeOngoingRide,
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_EMERGENCY_CONTACTS) {
                            EmergencyContactsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onUpsert = viewModel::upsertEmergencyContact,
                                onDelete = viewModel::deleteEmergencyContact,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_TRUSTED_CONTACTS) {
                            TrustedContactsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onToggleAutoShare = { enabled ->
                                    viewModel.updateUserProfile { it.copy(autoShareLiveRide = enabled) }
                                },
                                onUpsert = viewModel::upsertTrustedContact,
                                onDelete = viewModel::deleteTrustedContact,
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_REPORT_USER,
                            arguments = listOf(
                                navArgument("target") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            val target = Uri.decode(entry.arguments?.getString("target") ?: "")
                            ReportUserScreen(
                                initialTargetName = target,
                                onBack = { navController.popBackStack() },
                                onSubmitReport = viewModel::submitComplaint,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_BLOCKED_USERS) {
                            BlockedUsersScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onUnblock = viewModel::unblockUser,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_LOST_FOUND) {
                            LostFoundScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onAddItem = viewModel::addLostFoundItem,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_SUPPORT_DISPUTE) {
                            SupportDisputeScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSubmitComplaint = viewModel::submitComplaint,
                            )
                        }

                        // Section D: Extra Features
                        composable(
                            route = RaahiRoutes.ROUTE_PAYMENT,
                            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
                        ) { entry ->
                            val bookingId = entry.arguments?.getString("bookingId") ?: ""
                            PaymentScreen(
                                bookingId = bookingId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onPay = viewModel::recordPayment,
                                onViewHistory = {
                                    navController.navigate(RaahiRoutes.ROUTE_PAYMENT_HISTORY) {
                                        popUpTo(RaahiRoutes.ROUTE_PAYMENT) { inclusive = true }
                                    }
                                },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_PAYMENT_HISTORY) {
                            PaymentHistoryScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(
                            route = RaahiRoutes.ROUTE_RATE_REVIEW,
                            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
                        ) { entry ->
                            val bookingId = entry.arguments?.getString("bookingId") ?: ""
                            RateReviewScreen(
                                bookingId = bookingId,
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSubmitReview = viewModel::submitReview,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_EDIT_PROFILE) {
                            EditProfileScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSave = viewModel::updateUserProfile,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_FAVOURITE_ROUTES) {
                            FavouriteRoutesScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onSelectRoute = { origin, dest ->
                                    viewModel.applyQuickSearch(origin, dest)
                                    navController.navigate(RaahiRoutes.ROUTE_SEARCH_RESULTS)
                                },
                                onAddRoute = viewModel::addFavouriteRoute,
                                onDeleteRoute = viewModel::deleteFavouriteRoute,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_FAVOURITE_DRIVERS) {
                            FavouriteDriversScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onOpenDriverProfile = { driverId ->
                                    navController.navigate(RaahiRoutes.publicProfile(driverId))
                                },
                                onToggleFollow = viewModel::toggleFollowDriver,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_WAITLIST) {
                            WaitlistScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onOpenRideDetail = { rideId ->
                                    navController.navigate(RaahiRoutes.rideDetails(rideId))
                                },
                                onLeaveWaitlist = viewModel::leaveWaitlist,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_NOTIFICATIONS) {
                            NotificationsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onMarkAllRead = viewModel::markAllNotificationsRead,
                                onNotificationClick = ::handleNotificationNavigation,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_REFERRAL_PROMO) {
                            ReferralPromoScreen(
                                onBack = { navController.popBackStack() },
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_SAVINGS) {
                            SavingsDashboardScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_DRIVER_EARNINGS) {
                            DriverEarningsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                            )
                        }

                        composable(RaahiRoutes.ROUTE_MY_VEHICLES) {
                            MyVehiclesScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onAddVehicle = viewModel::addVehicle,
                                onUpdateVehicle = viewModel::updateVehicle,
                                onSetActive = viewModel::setActiveVehicle,
                                onDeleteVehicle = viewModel::deleteVehicle,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_SETTINGS) {
                            SettingsScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onToggleLanguage = { lang ->
                                    viewModel.updateUserProfile { it.copy(language = lang) }
                                },
                                onToggleNotifications = { enabled ->
                                    viewModel.updatePreferences { it.copy(notificationsEnabled = enabled) }
                                },
                                onOpenBlockedUsers = { navController.navigate(RaahiRoutes.ROUTE_BLOCKED_USERS) },
                                onResetDemoData = viewModel::resetDemoData,
                                onLogout = viewModel::logoutToOtp,
                                onShowMessage = viewModel::showMessage,
                            )
                        }

                        composable(RaahiRoutes.ROUTE_HELP_CENTER) {
                            HelpCenterScreen(
                                onBack = { navController.popBackStack() },
                                onOpenSupport = { navController.navigate(RaahiRoutes.ROUTE_SUPPORT_DISPUTE) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TabSpec(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int = 0,
    val testTag: String,
)

private fun buildTabSpecs(isDriverMode: Boolean, unreadInboxCount: Int): List<TabSpec> = listOf(
    TabSpec(
        route = RaahiRoutes.ROUTE_EXPLORE,
        label = if (isDriverMode) "Drive" else "Explore",
        selectedIcon = if (isDriverMode) Icons.Rounded.DirectionsCar else Icons.Rounded.Explore,
        unselectedIcon = if (isDriverMode) Icons.Outlined.DirectionsCar else Icons.Outlined.Explore,
        testTag = "nav_tab_explore",
    ),
    TabSpec(
        route = RaahiRoutes.ROUTE_TRIPS,
        label = "My Rides",
        selectedIcon = Icons.Rounded.Commute,
        unselectedIcon = Icons.Outlined.Commute,
        testTag = "nav_tab_trips",
    ),
    TabSpec(
        route = RaahiRoutes.ROUTE_INBOX,
        label = "Inbox",
        selectedIcon = Icons.Rounded.Inbox,
        unselectedIcon = Icons.Outlined.Inbox,
        badgeCount = unreadInboxCount,
        testTag = "nav_tab_inbox",
    ),
    TabSpec(
        route = RaahiRoutes.ROUTE_PROFILE,
        label = "Profile",
        selectedIcon = Icons.Rounded.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        testTag = "nav_tab_profile",
    ),
)

@Composable
private fun RowScope.TopLevelTabs(
    currentRoute: String?,
    isDriverMode: Boolean,
    unreadInboxCount: Int,
    onNavigate: (String) -> Unit,
) {
    val tabs = buildTabSpecs(isDriverMode, unreadInboxCount)
    tabs.forEach { tab ->
        val selected = currentRoute == tab.route
        NavigationBarItem(
            selected = selected,
            onClick = { onNavigate(tab.route) },
            icon = { TabBadgeIcon(tab, selected) },
            label = {
                Text(
                    text = tab.label,
                    style = AppTextStyles.chip,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.Primary,
                selectedTextColor = AppColors.Primary,
                indicatorColor = AppColors.PrimarySoft,
                unselectedIconColor = AppColors.TextSecondary,
                unselectedTextColor = AppColors.TextSecondary,
            ),
            modifier = Modifier.testTag(tab.testTag),
        )
    }
}

@Composable
private fun TabBadgeIcon(tab: TabSpec, selected: Boolean) {
    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
    if (tab.badgeCount > 0) {
        BadgedBox(badge = { Badge(containerColor = AppColors.Error) { Text("${tab.badgeCount}") } }) {
            Icon(icon, contentDescription = tab.label, modifier = Modifier.size(24.dp))
        }
    } else {
        Icon(icon, contentDescription = tab.label, modifier = Modifier.size(24.dp))
    }
}
