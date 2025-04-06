package com.example.routes.capsule.service

import com.example.repository.CapsuleRepository
import com.example.repository.UserRepository


class CapsuleService(
    private val capsuleRepository: CapsuleRepository,
    private val userRepository: UserRepository
) {
    suspend fun handlingTextContent(
        title : String,
        content : String,
        description : String,
        openData : Long,
    ) : Boolean {

        return false
    }

    suspend fun handlingFileContent(
        title : String,
        content : String,
        description : String,
        openData : Long,
        file : ByteArray,
        fileName : String
    ) {

    }
}