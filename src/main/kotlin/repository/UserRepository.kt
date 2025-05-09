package com.example.repository

import com.example.common.database.DatabaseProvider
import com.example.routes.auth.types.CreateNewAccountRequest
import com.example.security.UlidProvider
import com.example.types.storage.Users
import com.example.types.storage.Users.email
import com.example.types.storage.UsersTokenMapper
import com.example.types.wire.UserWire
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserRepository {

    fun create(mail: String, hashedPassword: String) : String {
        val id = UlidProvider.userId()

        Users.insert {
            it[Users.id] = id
            it[email] = mail
            it[passwordHash] = hashedPassword
        }

        return id
    }

    suspend fun findById(id: String): UserWire? = DatabaseProvider.dbQuery {
        Users.select { Users.id eq id }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findByEmail(email: String): UserWire? = DatabaseProvider.dbQuery  {
        Users.select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun existsByEmail(email: String): Boolean = DatabaseProvider.dbQuery {
        Users.select { Users.email eq email }
            .limit(1)
            .count() > 0
    }

    suspend fun delete(id: String): Boolean =  DatabaseProvider.dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    private fun rowToUser(row: ResultRow): UserWire? =
        UserWire(
            id = row[Users.id],
            email = row[Users.email],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt],
            isActive = row[Users.isActive],
            passwordHash = row[Users.passwordHash]
        )
}