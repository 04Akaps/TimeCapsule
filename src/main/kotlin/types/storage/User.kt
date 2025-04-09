package com.example.types.storage

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table(name = "users") {
    val id = varchar("id", 100)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val lastLogin = timestamp("last_login").nullable()
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}