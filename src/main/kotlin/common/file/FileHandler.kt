package com.example.common.file

import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import java.io.ByteArrayOutputStream

object FileHandler {
    private const val chunkSize = 1024 * 1024

    fun handlingIncomingFile(item : PartData.FileItem) : Pair<String, ByteArray> {
        val fileDataStream = ByteArrayOutputStream()

        val inputStream = item.streamProvider()
        val buffer = ByteArray(chunkSize)

        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            // 청크 데이터 처리
            fileDataStream.write(buffer, 0, bytesRead)
        }

        val fileData = fileDataStream.toByteArray()
        return Pair(item.originalFileName ?: "unnamed-file", fileData)
    }
}