package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object Recipients : Table() {
    val id = varchar("id", 100)
    val capsuleId = varchar("capsule_id", 100).references(TimeCapsules.id)
    val recipientEmail = varchar("recipient_email", 100)
    val hasViewed = bool("has_viewed").default(false)
    val notificationSent = bool("notification_sent").default(false) // TODO -> 어떻게 전송할까..?

    override val primaryKey = PrimaryKey(id)
}