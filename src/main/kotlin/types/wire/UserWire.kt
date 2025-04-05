package com.example.types.wire

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.LocalDateTime

data class UserWire(
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastLogin: LocalDateTime? = null,
    val isActive: Boolean
)