package com.example.railwayticketmanagement.models

import java.io.Serializable

data class Payment(
    val paymentId: Long = 0,
    val bookingId: Long = 0,
    val amount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val paymentDate: String
) : Serializable
