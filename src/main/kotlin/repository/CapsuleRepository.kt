package com.example.repository

import com.example.security.UlidProvider
import com.example.types.storage.*
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class CapsuleRepository {

    fun changeCapsuleSealStatus(capsuleId: String,  status: CapsuleStatus) {
        TimeCapsules.update( { TimeCapsules.id eq capsuleId }) {
            it[TimeCapsules.status] = status
        }
    }

    fun getOpenDateByCapsuleId(capsuleId : String) : Int? {
        val query = TimeCapsules.slice(
            TimeCapsules.id,
            TimeCapsules.scheduledOpenDate
        ).select {TimeCapsules.id.eq(capsuleId) }

        if (query.empty()) {
            return null
        }

        val capsuleRow = query.first()
        return capsuleRow[TimeCapsules.scheduledOpenDate]
    }

    fun capsuleWithContentsAndRecipient(capsuleId : String) : TimeCapsuleByCapsuleIdStorage? {
        val query = TimeCapsules
            .innerJoin(CapsuleContents, { id }, { CapsuleContents.capsuleId })
            .innerJoin(Recipients, { TimeCapsules.id }, { Recipients.capsuleId })
            .slice(
                TimeCapsules.id,
                TimeCapsules.title,
                TimeCapsules.description,
                TimeCapsules.scheduledOpenDate,
                TimeCapsules.status,

                CapsuleContents.contentType,
                CapsuleContents.content,

                Recipients.recipientEmail,
                Recipients.hasViewed
            )
            .select { TimeCapsules.id eq capsuleId }

        if (query.empty()) {
            return null
        }

        val capsuleRow = query.first()

        return TimeCapsuleByCapsuleIdStorage(
            id = capsuleRow[TimeCapsules.id],
            title = capsuleRow[TimeCapsules.title],
            description = capsuleRow[TimeCapsules.description],
            scheduledOpenDate = capsuleRow[TimeCapsules.scheduledOpenDate],
            status = capsuleRow[TimeCapsules.status].toString(),
            contentType = capsuleRow[CapsuleContents.contentType].name,
            content = capsuleRow[CapsuleContents.content],
            recipientEmail = capsuleRow[Recipients.recipientEmail],
            hasViewed = capsuleRow[Recipients.hasViewed]
        )
    }

    fun createCapsule(
        creatorId: String,
        title: String,
        description: String = "",
        scheduledOpenDate: Int,
        status: CapsuleStatus = CapsuleStatus.sealed,
    ) : String {
        val id = UlidProvider.capsuleId()
        val now = (System.currentTimeMillis() / 1000).toInt()

        TimeCapsules.insert {
            it[TimeCapsules.id] = id
            it[creator_id] = creatorId
            it[TimeCapsules.title] = title
            it[TimeCapsules.description] = description

            it[creationDate] = now
            it[TimeCapsules.scheduledOpenDate] = scheduledOpenDate

            it[TimeCapsules.status] = status
        }


        return id
    }
}