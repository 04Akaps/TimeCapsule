package com.example.types.wire

import java.time.LocalDateTime

data class CapsuleWire(
    val id: String,
    val title: String,
    val description: String?,
    val scheduledOpenDate: LocalDateTime,
    val status: String,

    val contentType: String,
    val content: String?,
    val recipientEmail: String,
    val hasViewed: Boolean
)