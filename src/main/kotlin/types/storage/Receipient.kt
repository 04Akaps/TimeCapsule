package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object Recipients : Table() {
    val id = varchar("id", 100)
    val capsuleId = varchar("capsule_id", 100).references(TimeCapsules.id)
    val userId = varchar("user_id", 100).references(Users.id).nullable()
    val hasViewed = bool("has_viewed").default(false)
    val notificationSent = bool("notification_sent").default(false) // TODO -> 어떻게 전송할까..?

    override val primaryKey = PrimaryKey(id)
}

data class RecipientStorage(
    val id: String,
    val capsuleId: String,
    val userId: String? = null,
    val hasViewed: Boolean = false,
    val notificationSent: Boolean = false
)