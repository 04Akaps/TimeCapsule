package com.example.routes.capsule.service

import com.example.common.database.DatabaseProvider
import com.example.common.file.FileStorageRepository
import com.example.common.utils.FormatVerify.toLocalDateTime
import com.example.repository.CapsuleContentRepository
import com.example.repository.CapsuleRepository
import com.example.repository.RecipientsRepository
import com.example.repository.UserRepository
import com.example.routes.capsule.types.UploadContentResponse
import com.example.routes.capsule.types.UploadFileResponse
import com.example.types.response.GlobalResponse
import com.example.types.response.GlobalResponseProvider
import com.example.types.storage.ContentType


class CapsuleService(
    private val capsuleContentRepository: CapsuleContentRepository,
    private val recipientsRepository: RecipientsRepository,
    private val capsuleRepository: CapsuleRepository,
    private val userRepository: UserRepository
) {

    suspend fun verifyEmailExist(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    suspend fun handlingTextContent(
        contentType: ContentType,
        recipientEmail: String,
        userID : String,
        title : String,
        content : String,
        description : String,
        openData : Long,
    ) : GlobalResponse<UploadContentResponse> {

        try {
            DatabaseProvider.dbQuery {
                val capsuleID = capsuleRepository.createCapsule(userID, title, description, openData.toLocalDateTime())
                capsuleContentRepository.createCapsuleContent(capsuleID, contentType, content)
                recipientsRepository.create(capsuleID, recipientEmail)
            }

            return GlobalResponseProvider.new(0, "success", UploadContentResponse(
                recipientEmail = recipientEmail,
            ))
        } catch (e :Exception) {
            return GlobalResponseProvider.failed(-1, "failed to content ${e.message}", null)
        }
    }

    suspend fun handlingFileContent(
        contentType: ContentType,
        recipientEmail: String,
        userID: String,
        title: String,
        content: String,
        description: String,
        openData: Long,
        file: ByteArray,
        fileName: String
    ) : GlobalResponse<UploadFileResponse> {
        try {
            DatabaseProvider.dbQuery {
                val capsuleID = capsuleRepository.createCapsule(userID, title, description, openData.toLocalDateTime())
                capsuleContentRepository.createCapsuleContent(capsuleID, contentType, content)
                recipientsRepository.create(capsuleID, recipientEmail)
            }

            val filePath = FileStorageRepository.filePathMaker(userID, fileName, title)
            FileStorageRepository.uploadFile(userID, file,fileName, filePath)

            return GlobalResponseProvider.new(0, "success", UploadFileResponse(
                recipientEmail = recipientEmail,
                fileName = fileName,
                fileSize = file.size,
                filePath = filePath,
            ))
        } catch (e: Exception) {
            return GlobalResponseProvider.failed(-1, "failed to upload file ${e.message}", null)
        }
    }
}