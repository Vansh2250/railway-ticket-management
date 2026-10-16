package com.example.railwayticketmanagement.models

import java.io.Serializable

data class Passenger(
    val passengerId: Long = 0,
    val bookingId: Long = 0,
    val passengerName: String,
    val age: Int,
    val gender: String
) : Serializable
