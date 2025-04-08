package com.example.repository

import com.example.common.database.DatabaseProvider
import com.example.security.UlidProvider
import com.example.types.storage.CapsuleContents
import com.example.types.storage.ContentType
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class CapsuleContentRepository {

    fun createCapsuleContent(
        capsuledId : String,
        contentType: ContentType,
        content: String,
    ) : String {
        val id = UlidProvider.contentId()
        val now = Instant.now()

        CapsuleContents.insert {
            it[CapsuleContents.id] = id
            it[capsuleId] = capsuledId
            it[CapsuleContents.contentType] = contentType
            it[CapsuleContents.content] = content
            it[createdAt] = now
        }

        return id
    }

}