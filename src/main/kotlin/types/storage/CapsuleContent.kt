package com.example.types.storage

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.LocalDateTime

object CapsuleContents : Table() {
    val id = varchar("id", 100)
    val capsuleId = varchar("capsule_id", 100).references(TimeCapsules.id)
    val contentType = enumerationByName("content_type", 10, ContentType::class)
    val content = text("content").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}

enum class ContentType {
    TEXT, IMAGE, VIDEO, AUDIO
}