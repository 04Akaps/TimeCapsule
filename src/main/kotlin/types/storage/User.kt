package com.example.types.storage

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.LocalDateTime

object Users : Table() {
    val id = varchar("id", 100)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val lastLogin = timestamp("last_login").nullable()
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

data class UserStorage(
    val id: String,
    val email: String,
    val passwordHash: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastLogin: LocalDateTime? = null,
    val isActive: Boolean = true
)