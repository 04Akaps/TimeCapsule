package com.example.types.wire

data class CapsuleWire(
    val id: String,
    val title: String,
    val description: String?,
    val scheduledOpenDate: Int,
    val status: String,

    val contentType: String,
    val content: String?,
    val recipientEmail: String,
    val hasViewed: Boolean
)