package com.example.types.storage

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.LocalDateTime
import java.math.BigDecimal

object TimeCapsules : Table() {
    val id = varchar("id", 100)
    val creatorId = varchar("creator_id", 100).references(Users.id)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val isPrivate = bool("is_private").default(true)
    val creationDate = timestamp("creation_date").defaultExpression(CurrentTimestamp())
    val scheduledOpenDate = timestamp("scheduled_open_date")
    val status = enumerationByName("status", 10, CapsuleStatus::class).default(CapsuleStatus.SEALED)
    val locationLat = decimal("location_lat", 10, 8).nullable()
    val locationLng = decimal("location_lng", 11, 8).nullable()

    override val primaryKey = PrimaryKey(id)
}

enum class CapsuleStatus {
    SEALED, OPENED
}

data class TimeCapsuleStorage(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String? = null,
    val isPrivate: Boolean = true,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val scheduledOpenDate: LocalDateTime,
    val status: CapsuleStatus = CapsuleStatus.SEALED,
    val locationLat: BigDecimal? = null,
    val locationLng: BigDecimal? = null
)