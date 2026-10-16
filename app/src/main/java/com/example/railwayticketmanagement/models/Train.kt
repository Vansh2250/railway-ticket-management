package com.example.railwayticketmanagement.models

import java.io.Serializable

data class Train(
    val trainId: Long = 0,
    val trainNumber: String,
    val trainName: String,
    val source: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val fare: Double,
    val totalSeats: Int,
    val availableSeats: Int
) : Serializable
