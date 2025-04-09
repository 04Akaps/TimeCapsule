package com.example.security


import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


/*
    1. 특정 시간이 지나기 전에 복호화 불가능하게 설계
    2.

 */

data class TimelockedData(
    val encryptedContent: String, // 데이터 키로 암호화되는 실제 컨텐츠
    val encryptedDataKey: String, // 마스터 키와, 시간 기반의 키의 조합으로 암호화 시킨 키
    val releaseTime: LocalDateTime, // 가능 시간
    val timeSalt: String // 암호화마다 설정되는 고유한 salt 값
)

object TimeBaseEncryptionProvider {
    private const val GCM_IV_LENGTH = 12 // AES-GCM에 사용되는 표준 길이
    private const val GCM_TAG_LENGTH = 16 // GCM 인증의 태그 길이
    private val secureRandom = SecureRandom() // 난수 생성

    // TODO masterKey 설정 및 init으로 내부 변수 초기화 로직적용 --> 다른 object 코드에 대해서 동일하게 initialize가 아닌 init을 적용

    fun encryptWithTimelock(content: String, masterKey: ByteArray, releaseTime: LocalDateTime): TimelockedData {
        val dataKey = ByteArray(32)
        secureRandom.nextBytes(dataKey)

        val timeSalt = ByteArray(16)
        secureRandom.nextBytes(timeSalt)

        val timeKey = generateTimeKey(releaseTime, timeSalt)

        val combinedKey = combineKeys(masterKey, timeKey)

        val encryptedDataKey = encryptAES(dataKey, combinedKey)

        val encryptedContent = encryptAES(content.toByteArray(Charsets.UTF_8), dataKey)

        return TimelockedData(
            encryptedContent = bytesToBase64(encryptedContent),
            encryptedDataKey = bytesToBase64(encryptedDataKey),
            releaseTime = releaseTime,
            timeSalt = bytesToBase64(timeSalt)
        )
    }

    fun decryptWithTimelock(timelockedData: TimelockedData, masterKey: ByteArray): String {
        try {
            val timeKey = generateTimeKey(timelockedData.releaseTime, base64ToBytes(timelockedData.timeSalt))

            val combinedKey = combineKeys(masterKey, timeKey)

            val encryptedDataKey = base64ToBytes(timelockedData.encryptedDataKey)
            val dataKey = decryptAES(encryptedDataKey, combinedKey)

            val encryptedContent = base64ToBytes(timelockedData.encryptedContent)
            val contentBytes = decryptAES(encryptedContent, dataKey)

            return String(contentBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalStateException("복호화 실패: ${e.message}")
        }
    }

    private fun encryptAES(data: ByteArray, key: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val paramSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec)

        val encryptedData = cipher.doFinal(data)

        val result = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptedData, 0, result, iv.size, encryptedData.size)

        return result
    }

    private fun decryptAES(encryptedData: ByteArray, key: ByteArray): ByteArray {
        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val cipherText = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val paramSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec)

        return cipher.doFinal(cipherText)
    }

    private fun bytesToBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun base64ToBytes(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64)
    }

    private fun generateTimeKey(releaseTime: LocalDateTime, salt: ByteArray): ByteArray {
        val epochSeconds = releaseTime.toEpochSecond(ZoneOffset.UTC)
        val timeBytes = epochSeconds.toString().toByteArray(Charsets.UTF_8)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(timeBytes)
        md.update(salt)

        return md.digest()
    }

    private fun combineKeys(key1: ByteArray, key2: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(key1)
        md.update(key2)
        return md.digest()
    }
}