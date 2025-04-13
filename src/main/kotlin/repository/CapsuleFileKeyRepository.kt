package com.example.repository

import com.example.common.email.EmailService
import com.example.security.UlidProvider
import com.example.types.storage.CapsuleFileKeyMapper
import org.jetbrains.exposed.sql.insert

class CapsuleFileKeyRepository(
    private val emailService: EmailService,
) {

    fun create(
        capsuledId : String,
        filePath : String,
        fileName : String,
    ) {
        val storage = emailService.storage()
        val id = UlidProvider.capsuleFilePathMapper()

        CapsuleFileKeyMapper.insert {
            it[CapsuleFileKeyMapper.id] = id
            it[capsuleId] = capsuledId
            it[CapsuleFileKeyMapper.filePath] = filePath
            it[CapsuleFileKeyMapper.storage] = storage
            it[CapsuleFileKeyMapper.fileName]  = fileName
        }
    }
}
