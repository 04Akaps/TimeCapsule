package com.example.types.storage

import com.example.types.wire.CapsuleWire
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.LocalDateTime

object TimeCapsules : Table(name = "time_capsules") {
    val id = varchar("id", 100)
    val creator_id = varchar("creator_id", 100).references(Users.id)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val creationDate = timestamp("creation_date")
    val scheduledOpenDate = timestamp("scheduled_open_date")
    val status = enumerationByName("status", 10, CapsuleStatus::class)
    val locationLat = decimal("location_lat", 10, 8).nullable()
    val locationLng = decimal("location_lng", 11, 8).nullable()

    override val primaryKey = PrimaryKey(id)
}

enum class CapsuleStatus {
    sealed, opened
}

data class TimeCapsuleByCapsuleIdStorage(
    val id: String,
    val title: String,
    val description: String?,
    val scheduledOpenDate: LocalDateTime,
    val status: String,

    val contentType: String,
    val content: String?,
    val recipientEmail: String,
    val hasViewed: Boolean
) {
    fun toWire() = CapsuleWire(
        id = id,
        title = title,
        description = description,
        scheduledOpenDate = scheduledOpenDate,
        status = status,
        content = content,
        recipientEmail = recipientEmail,
        hasViewed = false,
        contentType = contentType
    )
}