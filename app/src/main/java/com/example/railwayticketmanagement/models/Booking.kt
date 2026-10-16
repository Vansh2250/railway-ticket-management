package com.example.railwayticketmanagement.models

import java.io.Serializable

data class Booking(
    val bookingId: Long = 0,
    val userId: Long,
    val trainId: Long,
    val journeyDate: String,
    val passengerCount: Int,
    val totalFare: Double,
    val bookingStatus: String,
    val bookingDate: String,
    // Enriched fields for easy display
    var trainNumber: String = "",
    var trainName: String = "",
    var source: String = "",
    var destination: String = "",
    var departureTime: String = "",
    var arrivalTime: String = "",
    var farePerPassenger: Double = 0.0,
    var paymentStatus: String = "SUCCESS",
    var paymentMethod: String = "",
    var passengers: List<Passenger> = emptyList()
) : Serializable
