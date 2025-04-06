package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object UsersTokenMapper : Table("user_token_mapper") {
    val id = varchar("user_id", 100).references(Users.id)
    val token = varchar("token",500)

    override val primaryKey = PrimaryKey(id)
}