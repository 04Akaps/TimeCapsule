package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object Users : Table(name = "users") {
    val id = varchar("id", 100)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = integer("created_at")
    val updatedAt = integer("updated_at")
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}