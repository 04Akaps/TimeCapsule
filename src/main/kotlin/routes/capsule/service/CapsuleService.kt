package com.example.routes.capsule.service

import com.example.repository.CapsuleRepository
import com.example.repository.UserRepository


class CapsuleService(
    private val capsuleRepository: CapsuleRepository,
    private val userRepository: UserRepository
) {
    suspend fun verifyEmail(email : String) : Boolean {
        return userRepository.existsByEmail(email)
    }
}