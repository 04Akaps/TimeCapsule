package com.example.repository

import com.example.security.UlidProvider
import com.example.types.storage.Recipients
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

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


    fun changeHasViewedAndNotificationSent(
        capsuleId : String,
        hasViewed : Boolean,
        notificationSendSuccess : Boolean
    ) {
        Recipients.update({ Recipients.capsuleId eq capsuleId }) {
            it[Recipients.hasViewed] = hasViewed
            it[notificationSent] = notificationSendSuccess
        }
    }

    fun getRecipientsByCapsuleId(capsuleId: String) : String {
        val query = Recipients.slice(Recipients.recipientEmail).select({Recipients.id.eq(capsuleId)})
        val result = query.first().get(Recipients.recipientEmail)
        return result
    }

}