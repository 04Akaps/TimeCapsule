package com.example.repository

import com.example.common.utils.FormatVerify.toLocalDateTime
import com.example.security.TimelockedData
import com.example.security.UlidProvider
import com.example.types.storage.CapsuleContents
import com.example.types.storage.Recipients
import com.example.types.storage.TimeCapsuleByCapsuleIdStorage
import com.example.types.storage.TimeCapsules
import com.example.types.storage.TimeCapsules.id
import com.example.types.storage.TimebaseEncryptionMapper
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

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

    fun getTimeLockDataWhenOpen(
        capsuleId: String
    ): Triple<String, String, String>? {  // 반환 타입 추가
        val query = TimebaseEncryptionMapper
            .innerJoin(CapsuleContents, { TimebaseEncryptionMapper.capsuleId }, { CapsuleContents.capsuleId })
            .slice(
                TimebaseEncryptionMapper.encryptedDataKey,
                TimebaseEncryptionMapper.timeSalt,
                CapsuleContents.content,
            )
            .select { TimebaseEncryptionMapper.capsuleId eq capsuleId }

        if (query.empty()) {
            return null
        }

        val capsuleRow = query.first()

        val encryptedDataKey = capsuleRow[TimebaseEncryptionMapper.encryptedDataKey]
        val encryptedData = capsuleRow[CapsuleContents.content].toString()
        val timeSalt = capsuleRow[TimebaseEncryptionMapper.timeSalt]

        return Triple(encryptedDataKey, encryptedData, timeSalt)
    }

}