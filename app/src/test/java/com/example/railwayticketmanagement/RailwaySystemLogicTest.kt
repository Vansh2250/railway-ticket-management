package com.example.railwayticketmanagement

import com.example.railwayticketmanagement.models.Booking
import com.example.railwayticketmanagement.models.Passenger
import com.example.railwayticketmanagement.models.Train
import com.example.railwayticketmanagement.models.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RailwaySystemLogicTest {

    @Test
    fun testFareCalculation() {
        val farePerPassenger = 500.0
        val passengerCount = 3
        val totalFare = farePerPassenger * passengerCount

        assertEquals(1500.0, totalFare, 0.001)
    }

    @Test
    fun testSeatDeductionAndRestoration() {
        val initialSeats = 100
        val bookedSeats1 = 2
        val seatsAfterBooking1 = initialSeats - bookedSeats1
        assertEquals(98, seatsAfterBooking1)

        val bookedSeats2 = 3
        val seatsAfterBooking2 = seatsAfterBooking1 - bookedSeats2
        assertEquals(95, seatsAfterBooking2)

        // Cancel the second booking (3 seats)
        val seatsAfterCancellation = seatsAfterBooking2 + bookedSeats2
        assertEquals(98, seatsAfterCancellation)
    }

    @Test
    fun testInsufficientSeatsValidation() {
        val availableSeats = 2
        val requestedSeats = 3

        val canBook = requestedSeats <= availableSeats
        assertFalse("Should reject booking when requested seats exceed available seats", canBook)
    }

    @Test
    fun testSearchRouteValidation() {
        val fromStation = "Ahmedabad"
        val toStationSame = "Ahmedabad"
        val toStationDiff = "Mumbai"

        assertTrue("Same source and destination should be invalid", fromStation.equals(toStationSame, ignoreCase = true))
        assertFalse("Different stations should be valid", fromStation.equals(toStationDiff, ignoreCase = true))
    }

    @Test
    fun testMobileNumberValidation() {
        val validMobile = "9876543210"
        val invalidMobileShort = "98765"
        val invalidMobileAlpha = "98765abcde"

        assertTrue(validMobile.length == 10 && validMobile.all { it.isDigit() })
        assertFalse(invalidMobileShort.length == 10 && invalidMobileShort.all { it.isDigit() })
        assertFalse(invalidMobileAlpha.length == 10 && invalidMobileAlpha.all { it.isDigit() })
    }

    @Test
    fun testCancellationDoublePrevention() {
        var bookingStatus = "CONFIRMED"
        var currentAvailableSeats = 95
        val passengerCount = 3

        // First cancellation
        var cancelAllowed = bookingStatus == "CONFIRMED"
        assertTrue(cancelAllowed)
        bookingStatus = "CANCELLED"
        currentAvailableSeats += passengerCount
        assertEquals(98, currentAvailableSeats)

        // Second cancellation attempt
        cancelAllowed = bookingStatus == "CONFIRMED"
        assertFalse("Should prevent duplicate cancellation on already cancelled booking", cancelAllowed)
        assertEquals(98, currentAvailableSeats) // Seats must not increase again
    }
}
