package com.example.plugins

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.security.PasetoProvider
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.log

class PasetoEncryptionConfig {
    var issuer: String = "test-app"
}

val PasetoEncryption = createApplicationPlugin(
    name = "PasetoEncryption",
    createConfiguration = ::PasetoEncryptionConfig
) {

    try {
        val securityConfig = application.environment.config.config("security")
        val pasetoConfig = securityConfig.config("paseto")

        val issuer = pasetoConfig.propertyOrNull("issuer")?.getString() ?: pluginConfig.issuer

        PasetoProvider.initialize(issuer)
        application.log.info("Initialized Paseto Encryption with issuer: $issuer")
    } catch (e : Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_SECURITY, e.message)
    }

}
