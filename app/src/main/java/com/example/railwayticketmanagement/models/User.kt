package com.example.railwayticketmanagement.models

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val mobile: String,
    val password: String = ""
)
