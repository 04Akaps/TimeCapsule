package com.example.repository

import com.example.common.database.DatabaseProvider
import com.example.security.UlidProvider
import com.example.types.storage.CapsuleStatus
import com.example.types.storage.TimeCapsules
import org.jetbrains.exposed.sql.insert
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneOffset

class CapsuleRepository {

    fun createCapsule(
        creatorId: String,
        title: String,
        description: String = "",
        scheduledOpenDate: LocalDateTime,
        locationLat: BigDecimal? = null,
        locationLng: BigDecimal? = null,
        status: CapsuleStatus = CapsuleStatus.SEALED,
    ) : String {
        val id = UlidProvider.capsuleId()
        val nowInstant = Instant.now()

        TimeCapsules.insert {
            it[TimeCapsules.id] = id
            it[creator_id] = creatorId
            it[TimeCapsules.title] = title
            it[TimeCapsules.description] = description

            it[creationDate] = nowInstant
            it[TimeCapsules.scheduledOpenDate] = scheduledOpenDate.toInstant(ZoneOffset.UTC)

            it[TimeCapsules.status] = status

            it[TimeCapsules.locationLat] = locationLat
            it[TimeCapsules.locationLng] = locationLng
        }


        return id
    }
}