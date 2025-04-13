package com.example.types.wire


data class UserWire(
    val id : String,
    val email: String,
    val createdAt: Int =0,
    val updatedAt: Int = 0,
    val passwordHash : String,
    val isActive: Boolean
)