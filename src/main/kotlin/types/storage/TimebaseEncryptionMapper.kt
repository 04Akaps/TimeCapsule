package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object TimebaseEncryptionMapper : Table(name = "time_capsule_encryption_mapper") {
    val id = varchar("id", 100)
    val encryptedDataKey = varchar("encrypted_data_key", 500)
    val timeSalt = varchar("time_salt", 500)

    // content는 TimeCapsule 정보에 담기게 된다.

    override val primaryKey = PrimaryKey(id)
}
