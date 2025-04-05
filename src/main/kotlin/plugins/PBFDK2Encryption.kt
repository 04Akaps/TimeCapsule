package com.example.plugins

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.security.PBFDK2Provider
import io.ktor.server.application.*

class PBFDK2EncryptionConfig {
    var algorithm: String = "PBKDF2WithHmacSHA256"
    var iterations: Int = 210000
    var keyLength: Int = 256
    var saltLength: Int = 16
}

val PBFDK2Encryption = createApplicationPlugin(
    name = "PasswordEncryption",
    createConfiguration = ::PBFDK2EncryptionConfig
) {

    try {
        // YAML 설정 가져오기
        val securityConfig = application.environment.config.config("security")
        val pbkdf2Config = securityConfig.config("password.pbkdf2")

        // 설정에서 값 로드 (또는 기본값 사용)
        val algorithm = pbkdf2Config.propertyOrNull("algorithm")?.getString() ?: pluginConfig.algorithm
        val iterations = pbkdf2Config.propertyOrNull("iterations")?.getString()?.toInt() ?: pluginConfig.iterations
        val keyLength = pbkdf2Config.propertyOrNull("keyLength")?.getString()?.toInt() ?: pluginConfig.keyLength
        val saltLength = pbkdf2Config.propertyOrNull("saltLength")?.getString()?.toInt() ?: pluginConfig.saltLength

        PBFDK2Provider.initialize(
            algorithm = algorithm,
            iterations = iterations,
            keyLength = keyLength,
            saltLength = saltLength
        )

        // 로그 출력
        application.log.info("Password encryption configured with algorithm=$algorithm, iterations=$iterations")
    } catch (e : Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_SECURITY, e.message)
    }
}