package com.example.types.wire

import java.time.LocalDateTime

data class UserWire(
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastLogin: LocalDateTime? = null,
    val isActive: Boolean
)