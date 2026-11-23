package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.mock.MockCarPoolStore
import com.example.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Before
    fun setUp() {
        MockCarPoolStore.resetDemoData()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Raahi", appName)
    }

    @Test
    fun `counter offer negotiation preserves history and updates seats on accept`() = runTest {
        val initialBooking = MockCarPoolStore.getBookingById("B-1001")
        assertNotNull(initialBooking)
        assertEquals(3, initialBooking!!.offers.size)
        assertEquals(BookingStatus.NEGOTIATING, initialBooking.status)

        val initialRide = MockCarPoolStore.getRideById("R-001")!!
        assertEquals(3, initialRide.availableSeats)

        val counterResult = MockCarPoolStore.submitCounterOffer(
            bookingId = "B-1001",
            actor = OfferActor.PASSENGER,
            amountPerSeatPkr = 850,
            note = "Final offer",
        )
        assertTrue(counterResult.isSuccess)
        val afterCounter = counterResult.getOrThrow()
        assertEquals(4, afterCounter.offers.size)
        assertEquals(CounterOfferStatus.COUNTERED, afterCounter.offers[2].status)
        assertEquals(CounterOfferStatus.PENDING, afterCounter.offers[3].status)

        val acceptResult = MockCarPoolStore.respondToPendingOffer("B-1001", OfferAction.ACCEPT)
        assertTrue(acceptResult.isSuccess)
        val confirmed = acceptResult.getOrThrow()
        assertEquals(BookingStatus.CONFIRMED, confirmed.status)
        assertEquals(850, confirmed.agreedFarePerSeatPkr)
        assertEquals(4, confirmed.offers.size)

        val updatedRide = MockCarPoolStore.getRideById("R-001")!!
        assertEquals(1, updatedRide.availableSeats)
    }

    @Test
    fun `publish ride validates capacity and female only locks audience to verified only`() = runTest {
        val badResult = MockCarPoolStore.publishRide(
            CreateRideRequest(
                originCity = "Karachi",
                originArea = "Clifton",
                destinationCity = "Karachi",
                destinationArea = "Clifton",
                intermediateStops = emptyList(),
                dateDayOffset = 1,
                departureDateLabel = "Tomorrow",
                departureTimeLabel = "09:00 AM",
                departureMinutesOfDay = 540,
                durationMinutes = 60,
                vehicleId = "V-ME-1",
                availableSeats = 2,
                pricePerSeatPkr = 500,
                preferences = RidePreferences(),
                audience = RideAudience.ALL_USERS,
                notes = "",
            )
        )
        assertTrue(badResult.isFailure)

        val femaleOnlyResult = MockCarPoolStore.publishRide(
            CreateRideRequest(
                originCity = "Lahore",
                originArea = "Gulberg",
                destinationCity = "Islamabad",
                destinationArea = "F-10",
                intermediateStops = emptyList(),
                dateDayOffset = 2,
                departureDateLabel = "In 2 days",
                departureTimeLabel = "08:00 AM",
                departureMinutesOfDay = 480,
                durationMinutes = 270,
                vehicleId = "V-ME-1",
                availableSeats = 3,
                pricePerSeatPkr = 2100,
                preferences = RidePreferences(womenOnly = true),
                audience = RideAudience.ALL_USERS, // Should be overridden to VERIFIED_ONLY
                notes = "Motorway trip",
            )
        )
        assertTrue(femaleOnlyResult.isSuccess)
        val published = femaleOnlyResult.getOrThrow()
        assertTrue(published.isDriverOwned)
        assertEquals(RideAudience.VERIFIED_ONLY, published.audience)
        assertNotNull(MockCarPoolStore.getRideById(published.id))
    }

    @Test
    fun `search rides puts verified drivers first and filters verified only`() {
        val allResults = MockCarPoolStore.searchRides(
            RideSearchQuery(
                originCity = "Hyderabad",
                destinationCity = "Karachi",
                maxPricePkr = 1000,
            )
        )
        assertTrue(allResults.isNotEmpty())
        val drivers = MockCarPoolStore.driversFlow.value.associateBy { it.id }
        val firstUnverifiedIdx = allResults.indexOfFirst { drivers[it.driverId]?.isVerified == false }
        val lastVerifiedIdx = allResults.indexOfLast { drivers[it.driverId]?.isVerified == true }
        if (firstUnverifiedIdx != -1 && lastVerifiedIdx != -1) {
            assertTrue(lastVerifiedIdx < firstUnverifiedIdx)
        }

        val verifiedOnly = MockCarPoolStore.searchRides(
            RideSearchQuery(
                originCity = "Hyderabad",
                destinationCity = "Karachi",
                verifiedOnly = true,
            )
        )
        assertTrue(verifiedOnly.isNotEmpty())
        assertTrue(verifiedOnly.all { drivers[it.driverId]?.isVerified == true })
    }

    @Test
    fun `basic mode blocks verified only booking while verified mode allows it`() = runTest {
        MockCarPoolStore.updateUser { it.copy(previewAsBasic = true) }

        val blockedResult = MockCarPoolStore.createBooking(
            CreateBookingRequest(
                rideId = "R-001", // VERIFIED_ONLY
                seatsRequested = 1,
                proposedFarePerSeatPkr = null,
                pickupPoint = "Latifabad Unit 7",
                dropoffPoint = "Clifton Block 4",
                note = "",
            )
        )
        assertTrue(blockedResult.isFailure)

        val basicAllowedResult = MockCarPoolStore.createBooking(
            CreateBookingRequest(
                rideId = "R-002", // ALL_USERS
                seatsRequested = 1,
                proposedFarePerSeatPkr = null,
                pickupPoint = "DHA Phase 6",
                dropoffPoint = "Autobhan Road",
                note = "",
            )
        )
        assertTrue(basicAllowedResult.isSuccess)

        MockCarPoolStore.updateUser { it.copy(previewAsBasic = false) }
        val verifiedAllowedResult = MockCarPoolStore.createBooking(
            CreateBookingRequest(
                rideId = "R-001",
                seatsRequested = 1,
                proposedFarePerSeatPkr = null,
                pickupPoint = "Latifabad Unit 7",
                dropoffPoint = "Clifton Block 4",
                note = "",
            )
        )
        assertTrue(verifiedAllowedResult.isSuccess)
    }

    @Test
    fun `ride start otp transitions booking to ongoing and completed`() {
        val badOtp = MockCarPoolStore.verifyRideStartOtp("B-1002", "9999")
        assertTrue(badOtp.isFailure)

        val started = MockCarPoolStore.verifyRideStartOtp("B-1002", "1234")
        assertTrue(started.isSuccess)
        assertEquals(BookingStatus.ONGOING, started.getOrThrow().status)

        val completed = MockCarPoolStore.completeOngoingRide("B-1002")
        assertTrue(completed.isSuccess)
        assertEquals(BookingStatus.COMPLETED, completed.getOrThrow().status)
    }

    @Test
    fun `payment promo FIRST50 applies discount and marks booking paid`() {
        val txResult = MockCarPoolStore.recordPayment("B-1002", "JazzCash", "FIRST50")
        assertTrue(txResult.isSuccess)
        val tx = txResult.getOrThrow()
        assertEquals(250, tx.discountPkr)
        assertEquals(2200 - 250, tx.amountPkr)
        assertEquals("FIRST50", tx.promoCode)
        assertTrue(MockCarPoolStore.getBookingById("B-1002")!!.isPaid)
    }

    @Test
    fun `trusted contacts limit of 3 and waitlist join leave work`() {
        // Initial data starts with 2 trusted contacts
        val third = MockCarPoolStore.upsertTrustedContact(null, "Bilal", "+92 300 1112233", "Friend")
        assertTrue(third.isSuccess)
        val fourth = MockCarPoolStore.upsertTrustedContact(null, "Extra", "+92 300 4445566", "Friend")
        assertTrue(fourth.isFailure)

        MockCarPoolStore.joinWaitlist("R-007")
        assertTrue(MockCarPoolStore.waitlist.value.any { it.rideId == "R-007" })
        MockCarPoolStore.leaveWaitlist("R-007")
        assertFalse(MockCarPoolStore.waitlist.value.any { it.rideId == "R-007" })
    }
}
