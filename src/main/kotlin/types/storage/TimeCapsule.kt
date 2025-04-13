package com.example.types.storage

import com.example.types.wire.CapsuleWire
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object TimeCapsules : Table(name = "time_capsules") {
    val id = varchar("id", 100)
    val creator_id = varchar("creator_id", 100).references(Users.id)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val creationDate = integer("creation_date")
    val scheduledOpenDate = integer("scheduled_open_date")
    val status = enumerationByName("status", 10, CapsuleStatus::class)

    override val primaryKey = PrimaryKey(id)
}

enum class CapsuleStatus {
    sealed, opened
}

data class TimeCapsuleByCapsuleIdStorage(
    val id: String,
    val title: String,
    val description: String?,
    val scheduledOpenDate: Int,
    val status: String,

    val contentType: String,
    val content: String?,
    val recipientEmail: String,
    val hasViewed: Boolean,

    val filePath : String?,
    val fileName : String?
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
        contentType = contentType,
        filePath = filePath,
        fileName = fileName
    )
}