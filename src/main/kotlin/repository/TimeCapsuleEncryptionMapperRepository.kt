package com.example.repository

import com.example.security.UlidProvider
import com.example.types.storage.TimebaseEncryptionMapper
import org.jetbrains.exposed.sql.insert

class TimeCapsuleEncryptionMapperRepository {

    fun create(
        encryptedDataKey : String,
        timeSalt : String,
    ) : String {
        val id = UlidProvider.timeCapsuleEncryptionMapper()

        TimebaseEncryptionMapper.insert {
            it[TimebaseEncryptionMapper.id] = id
            it[TimebaseEncryptionMapper.encryptedDataKey] = encryptedDataKey
            it[TimebaseEncryptionMapper.timeSalt] = timeSalt
        }

        return id
    }

}