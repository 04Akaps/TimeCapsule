package com.example.plugins

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorageRepository
import io.ktor.server.application.*
import java.util.*

class StoragePluginConfig {
    var type: StorageType = StorageType.MINIO
    var endpoint: String = "http://localhost:9000"
    var accessKey: String = "minioadmin"
    var secretKey: String = "minioadmin"
    var region: String = ""
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


val StoragePlugin = createApplicationPlugin(
    name = "StoragePlugin",
    createConfiguration = ::StoragePluginConfig
) {
    try {
        val storageConfig = application.environment.config.config("storage")

        val storageType = storageConfig.propertyOrNull("type")?.getString() ?: "minio"

        val internalConfig =  when (StorageType.fromString(storageType)) {
            StorageType.MINIO -> storageConfig.config("minio")
            StorageType.S3 -> storageConfig.config("s3")
        }

        val endpoint = internalConfig.propertyOrNull("endpoint")?.getString() ?: pluginConfig.endpoint
        val accessKey = internalConfig.propertyOrNull("accessKey")?.getString() ?: pluginConfig.accessKey
        val secretKey = internalConfig.propertyOrNull("secretKey")?.getString() ?: pluginConfig.secretKey
        val region = internalConfig.propertyOrNull("region")?.getString() ?: pluginConfig.region

        FileStorageRepository.initialize(endpoint, accessKey, secretKey, region, StorageType.fromString(storageType))
        application.log.info("Initialized Storage Service with type: ${pluginConfig.type}, endpoint: ${pluginConfig.endpoint}")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_INIT_STORAGE, e.message)
    }
}