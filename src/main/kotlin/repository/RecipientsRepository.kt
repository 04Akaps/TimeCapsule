package com.example.repository

import com.example.common.database.DatabaseProvider
import com.example.security.UlidProvider
import com.example.types.storage.Recipients
import org.jetbrains.exposed.sql.insert

class RecipientsRepository {

    fun create(
        capsuleId : String,
        recipientEmail : String,
    ) : String {
        val id = UlidProvider.recipientId()

        Recipients.insert {
            it[Recipients.id] = id
            it[Recipients.capsuleId] = capsuleId
            it[Recipients.recipientEmail] = recipientEmail
            it[hasViewed] = false
            it[notificationSent] = false
        }

        return id
    }

}