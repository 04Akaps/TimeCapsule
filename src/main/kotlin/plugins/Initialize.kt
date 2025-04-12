package com.example.plugins

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorageRepository
import com.example.di.appModule
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import com.example.security.TimeBaseEncryptionProvider
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.util.*


class PasetoEncryptionConfig {
    var issuer: String = "test-app"
    var privateKey: String = ""
    var publicKey: String = ""
}


class PBFDK2EncryptionConfig {
    var algorithm: String = "PBKDF2WithHmacSHA256"
    var iterations: Int = 210000
    var keyLength: Int = 256
    var saltLength: Int = 16
}

enum class StorageType {
    MINIO, S3;

    companion object {
        fun fromString(value: String): StorageType {
            return when (value.lowercase(Locale.getDefault())) {
                "s3" -> S3
                else -> MINIO
            }
        }
    }
}

fun Application.initialize() {

    pasetoInitialize(environment)
    pbfDK2Initialize(environment)
    fileStorageInitialize(environment)
    timebaseEncryptionInitialize(environment)

    install(Koin) {
        modules(
            module {
                single { environment }
            },
            appModule
        )
    }
}


fun pasetoInitialize(environment : ApplicationEnvironment) {
    try {
        val securityConfig = environment.config.config("security")
        val pasetoConfig = securityConfig.config("paseto")

        val defaultConfig = PasetoEncryptionConfig()

        val issuer = pasetoConfig.propertyOrNull("issuer")?.getString() ?: defaultConfig.issuer
        val privateKey = pasetoConfig.propertyOrNull("privateKey")?.getString() ?: defaultConfig.privateKey
        val publicKey = pasetoConfig.propertyOrNull("publicKey")?.getString() ?: defaultConfig.publicKey

        PasetoProvider.initialize(issuer, privateKey, publicKey)
        environment.log.info("Initialized Paseto Encryption with issuer: $issuer")
    } catch (e : Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_SECURITY, e.message)
    }
}

fun pbfDK2Initialize(environment: ApplicationEnvironment) {
    try {
        // YAML 설정 가져오기
        val securityConfig = environment.config.config("security")
        val pbkdf2Config = securityConfig.config("password.pbkdf2")

        val defaultConfig = PBFDK2EncryptionConfig()

        // 설정에서 값 로드 (또는 기본값 사용)
        val algorithm = pbkdf2Config.propertyOrNull("algorithm")?.getString() ?: defaultConfig.algorithm
        val iterations = pbkdf2Config.propertyOrNull("iterations")?.getString()?.toInt() ?: defaultConfig.iterations
        val keyLength = pbkdf2Config.propertyOrNull("keyLength")?.getString()?.toInt() ?: defaultConfig.keyLength
        val saltLength = pbkdf2Config.propertyOrNull("saltLength")?.getString()?.toInt() ?: defaultConfig.saltLength

        PBFDK2Provider.initialize(
            algorithm = algorithm,
            iterations = iterations,
            keyLength = keyLength,
            saltLength = saltLength
        )

        environment.log.info("Password encryption configured with algorithm=$algorithm, iterations=$iterations")
    } catch (e : Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_SECURITY, e.message)
    }
}

fun fileStorageInitialize(environment: ApplicationEnvironment) {
    try {
        val storageConfig = environment.config.config("storage")
        val storageType = storageConfig.propertyOrNull("type")?.getString() ?: "minio"

        val internalConfig =  when (StorageType.fromString(storageType)) {
            StorageType.MINIO -> storageConfig.config("minio")
            StorageType.S3 -> storageConfig.config("s3")
        }

        val endpoint = internalConfig.propertyOrNull("endpoint")?.getString().toString()
        val accessKey = internalConfig.propertyOrNull("accessKey")?.getString().toString()
        val secretKey = internalConfig.propertyOrNull("secretKey")?.getString().toString()
        val region = internalConfig.propertyOrNull("region")?.getString().toString()

        FileStorageRepository.initialize(endpoint, accessKey, secretKey, region, StorageType.fromString(storageType))
        environment.log.info("Initialized Storage Service with endpoint: ${endpoint}")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_STORAGE, e.message)
    }
}

fun timebaseEncryptionInitialize(environment: ApplicationEnvironment) {
    try {
        val securityConfig = environment.config.config("security")
        val timebaseConfig = securityConfig.config("timebase")


        // 설정에서 마스터키 가져오기
        val masterKeyBase64 = timebaseConfig.propertyOrNull("masterKey")?.getString().toString()

        // Base64 디코딩 및 검증
        if (masterKeyBase64.isBlank()) {
            throw IllegalArgumentException("시간 기반 암호화에 필요한 마스터키가 설정되지 않았습니다.")
        }

        val masterKey = Base64.getDecoder().decode(masterKeyBase64)

        if (masterKey.size != 32) { // 256비트(32바이트) 키 길이 검증
            throw IllegalArgumentException("마스터키는 32바이트(256비트)여야 합니다.")
        }

        TimeBaseEncryptionProvider.initialize(masterKey)

        environment.log.info("Initialized TimeBase Encryption successfully")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_SECURITY, e.message)
    }
}