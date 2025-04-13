package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object CapsuleFileKeyMapper : Table(name = "capsule_file_key_mapper") {
    val id = varchar("id", 100)
    val capsuleId = varchar("capsule_id", 100)
    val filePath = varchar("file_path", 500)
    val fileName = varchar("file_name", 500)
    val storage = varchar("storage", 500)

    // content는 TimeCapsule 정보에 담기게 된다.

    override val primaryKey = PrimaryKey(id)
}
