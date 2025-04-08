package com.example.repository

import com.example.common.database.DatabaseProvider
import com.example.types.storage.UsersTokenMapper
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update


class UserTokenMapperRepository {
    suspend fun createPasetoToken(userId : String, token : String) = DatabaseProvider.dbQuery {
        val existingToken = UsersTokenMapper.select { UsersTokenMapper.id eq userId }.singleOrNull()

        if (existingToken == null) {
            UsersTokenMapper.insert {
                it[id] = userId
                it[UsersTokenMapper.token] = token
            }
        } else {
            UsersTokenMapper.update({ UsersTokenMapper.id eq userId }) {
                it[UsersTokenMapper.token] = token
            }
        }
    }

}