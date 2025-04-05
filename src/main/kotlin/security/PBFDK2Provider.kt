package com.example.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2 비밀번호 암호화 서비스 싱글톤
 */
object PBFDK2Provider {
    private lateinit var algorithm: String
    private var iterations: Int = 0
    private var keyLength: Int = 0
    private var saltLength: Int = 0

    internal fun initialize(
        algorithm: String,
        iterations: Int,
        keyLength: Int,
        saltLength: Int
    ) {
        this.algorithm = algorithm
        this.iterations = iterations
        this.keyLength = keyLength
        this.saltLength = saltLength
    }

    fun encrypt(password: String): String {
        val salt = ByteArray(saltLength).apply {
            SecureRandom().nextBytes(this)
        }

        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLength
        )

        val secretKeyFactory = SecretKeyFactory.getInstance(algorithm)
        val hash = secretKeyFactory.generateSecret(keySpec).encoded

        return buildString {
            append(algorithm)
            append(":")
            append(iterations)
            append(":")
            append(Base64.getEncoder().encodeToString(salt))
            append(":")
            append(Base64.getEncoder().encodeToString(hash))
        }
    }

    fun verify(storedHash: String, inputPassword: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 4) return false

        val algorithm = parts[0]
        val iterations = parts[1].toInt()
        val salt = Base64.getDecoder().decode(parts[2])
        val originalHash = Base64.getDecoder().decode(parts[3])

        val keySpec = PBEKeySpec(
            inputPassword.toCharArray(),
            salt,
            iterations,
            originalHash.size * 8
        )

        val secretKeyFactory = SecretKeyFactory.getInstance(algorithm)
        val calculatedHash = secretKeyFactory.generateSecret(keySpec).encoded

        return originalHash.contentEquals(calculatedHash)
    }
}