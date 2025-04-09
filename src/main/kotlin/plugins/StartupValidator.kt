package com.example.plugins

import com.example.common.utils.log
import io.ktor.server.application.*

fun Application.StartUpValidation() {
    try {
        verifyFfmpegInstalled()
    } catch (e: Exception) {
        log.error("FFmpeg 설치 확인 실패: ${e.message}")
        System.exit(1)
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
            TODO("exception 처리")
        }

        val version = output.lines()
            .firstOrNull { it.contains("ffmpeg version") }
            ?.substringAfter("ffmpeg version")
            ?.trim()
            ?: "unknown"


        log.info("FFmpeg detected: version $version")
    } catch (e: Exception) {
        log.info("FFmpeg not found in system PATH: ${e.message}")
        TODO("exception 처리")
    }
}