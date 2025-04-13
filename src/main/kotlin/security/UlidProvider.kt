package com.example.security

import java.security.SecureRandom
import java.time.Instant

object UlidProvider {
    private val ENCODING_CHARS = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
        'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'
    )

    private fun generate(prefix: String = ""): String {
        val timestamp = Instant.now().toEpochMilli()
        val result = StringBuilder(prefix + encodeTime(timestamp))

        for (i in 0 until 16) {
            val index = SecureRandom().nextInt(32)
            result.append(ENCODING_CHARS[index])
        }

        return result.toString()
    }

    private fun encodeTime(timestamp: Long): String {
        val encodedTime = StringBuilder()
        var remainingTime = timestamp

        for (i in 0 until 10) {
            val mod = (remainingTime % 32).toInt()
            remainingTime /= 32
            encodedTime.insert(0, ENCODING_CHARS[mod])
        }

        return encodedTime.toString()
    }

    fun userId() = generate("")

    fun capsuleId() = generate("")

    fun contentId() = generate("")

    fun recipientId() = generate("")

    fun timeCapsuleEncryptionMapper() = generate("")

    fun capsuleFilePathMapper() = generate("")
}