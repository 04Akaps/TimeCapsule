package com.example.common.file

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import java.io.ByteArrayOutputStream

object FileHandler {
    private const val chunkSize = 1024 * 1024

    fun handlingIncomingFile(item : PartData.FileItem) : Pair<String, ByteArray> {
        try {
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
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_HANDLE_FILE, e.message)
        } finally {
            item.dispose()
        }
    }

    suspend fun exportFileData(forms : MultiPartData) : PartData.FileItem {
        var fileItem: PartData.FileItem? = null

        forms.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "file") {
                fileItem = part
            } else {
                part.dispose() // 다른 파트는 즉시 리소스 해제
            }
        }

        if (fileItem == null) {
            throw CustomException(ErrorCode.FILE_NOT_FOUND)
        }

        return fileItem
    }
}