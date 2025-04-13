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

    suspend fun exportFileData(forms : MultiPartData) :  Pair<PartData.FileItem, String>{
        var fileItem: PartData.FileItem? = null
        var jsonData  = ""

        forms.forEachPart { part ->
            when {
                // 파일이고 이름이 'file'인 경우
                part is PartData.FileItem && part.name == "file" -> {
                    fileItem = part
                }
                // JSON 데이터를 포함하는 폼 항목
                part is PartData.FormItem && part.name == "jsonData" -> {
                    jsonData = part.value
                    part.dispose()
                }
                else -> {
                    part.dispose()
                }
            }
        }

        if (fileItem == null) {
            throw CustomException(ErrorCode.FILE_NOT_FOUND)
        }

        if (jsonData.isBlank()) {
            throw CustomException(ErrorCode.INVALID_REQUEST_DATA, "JSON 데이터가 필요합니다")
        }

        return Pair(fileItem, jsonData)
    }
}