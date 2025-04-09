package com.example.plugins

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.utils.log
import io.ktor.server.application.*
import java.io.File

fun Application.startUpValidation() {
    verifyFfmpegInstalled()

    val storageType = environment.config.property("storage.type").getString()
    if (storageType == "minio") {
        verifyAndStartDockerMinIO()
    } else {
        log.info("Storage type is not 'minio', skipping Docker MinIO verification")
    }
}

private fun verifyFfmpegInstalled() {
    try {
        val process = ProcessBuilder("ffmpeg", "-version")
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw CustomException(ErrorCode.FFMPEG_REQUIRED)
        }

        val version = output.lines()
            .firstOrNull { it.contains("ffmpeg version") }
            ?.substringAfter("ffmpeg version")
            ?.trim()
            ?: "unknown"

        log.info("FFmpeg detected: version $version")
    } catch (e: Exception) {
        log.info("FFmpeg not found in system PATH: ${e.message}")
        throw CustomException(ErrorCode.FFMPEG_REQUIRED, e.message)
    }
}

private fun Application.verifyAndStartDockerMinIO() {
    try {
        val dockerVersionProcess = ProcessBuilder("docker", "--version")
            .redirectErrorStream(true)
            .start()

        val dockerVersionOutput = dockerVersionProcess.inputStream.bufferedReader().use { it.readText() }
        val dockerExitCode = dockerVersionProcess.waitFor()

        if (dockerExitCode != 0) {
            log.error("Docker is not installed or accessible. Server cannot start.")
            environment.monitor.raise(ApplicationStopping, this)
            return
        }

        log.info("Docker is available: ${dockerVersionOutput.trim()}")

        val minioCheckProcess = ProcessBuilder("docker", "ps", "-q", "-f", "name=minio")
            .redirectErrorStream(true)
            .start()

        val minioContainerId = minioCheckProcess.inputStream.bufferedReader().use { it.readText() }.trim()

        if (minioContainerId.isEmpty()) {
            log.info("MinIO container is not running. Starting MinIO...")

            val dockerComposeFile = getDockerComposeFile()

            val startProcess = ProcessBuilder("docker-compose", "-f", dockerComposeFile.absolutePath, "up", "-d", "minio")
                .redirectErrorStream(true)
                .start()

            val startOutput = startProcess.inputStream.bufferedReader().use { it.readText() }
            val startExitCode = startProcess.waitFor()

            if (startExitCode == 0) {
                log.info("MinIO container started successfully")

                Thread.sleep(2000)

                log.info("MinIO endpoint is available at: ${environment.config.property("storage.minio.endpoint").getString()}")
            } else {
                log.error("Failed to start MinIO container: ${startOutput.trim()}")
                throw RuntimeException("Failed to start MinIO container")
            }
        } else {
            log.info("MinIO container is already running with ID: $minioContainerId")
        }

    } catch (e: Exception) {
        log.error("Error during Docker/MinIO verification: ${e.message}")
        throw RuntimeException("Failed to verify Docker environment", e)
    }
}

private fun Application.getDockerComposeFile(): File {
    val resourcePath = "src/main/resources/docker-compose.yml"

    val resourceFile = File(resourcePath)
    log.info("Using docker-compose.yml from: $resourcePath")

    return resourceFile
}
