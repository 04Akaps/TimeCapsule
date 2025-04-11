package com.example.routes.capsule.service

import com.example.common.database.DatabaseProvider
import com.example.common.email.EmailService
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorageRepository
import com.example.common.utils.FormatVerify.toLocalDateTime
import com.example.repository.*
import com.example.routes.capsule.types.CapsuleCreateResponse
import com.example.routes.capsule.types.OpenCapsuleResponse
import com.example.security.TimeBaseEncryptionProvider
import com.example.types.response.GlobalResponse
import com.example.types.response.GlobalResponseProvider
import com.example.types.storage.CapsuleStatus
import com.example.types.storage.ContentType
import com.example.types.wire.CapsuleWire
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CapsuleService(
    private val capsuleContentRepository: CapsuleContentRepository,
    private val recipientsRepository: RecipientsRepository,
    private val capsuleRepository: CapsuleRepository,
    private val userRepository: UserRepository,
    private val timeCapsuleEncryptionMapperRepository: TimeCapsuleEncryptionMapperRepository,
    private val emailService: EmailService
) {

    suspend fun openCapsuleContent(capsuleId : String) : GlobalResponse<OpenCapsuleResponse> {

        try {
            val scheduledOpenDate = DatabaseProvider.dbQuery {
                capsuleRepository.getOpenDateByCapsuleId(capsuleId)
            } ?: throw  CustomException(ErrorCode.NOT_FOUND_CAPSULE, capsuleId)

            if (scheduledOpenDate < LocalDateTime.now()) {
                return GlobalResponseProvider.new(-1, "open capsule failed to open", null)
            }

            val recipientsEmail = DatabaseProvider.dbQuery {
                recipientsRepository.getRecipientsByCapsuleId(capsuleId)
            }
            var notiSended  = false

            try {
                emailService.sendEmail(
                    recipientsEmail,
                    "Your Time Capsule is opend!!",
                    """
                    <html>
                    <body>
                        <h1>Your Time Capsule Is Ready!</h1>
                        <p>Great news! The time capsule is now available to open.</p>
                        <p>This capsule was sealed on ${scheduledOpenDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} 
                        and contains memories waiting for you to rediscover.</p>
                        <p>Enjoy your journey back in time!</p>
                        <p>Best regards,<br>The Time Capsule Team</p>
                    </body>
                    </html>
                    """.trimIndent()
                )

                notiSended = true
            } catch (e : Exception) {
                TODO() // log
            }

            DatabaseProvider.dbQuery {
                capsuleRepository.changeCapsuleSealStatus(capsuleId, CapsuleStatus.opened)
                recipientsRepository.changeHasViewedAndNotificationSent(capsuleId, false, notiSended)
            }

            return GlobalResponseProvider.new(0, "", null)
        } catch (e: Exception) {
            return GlobalResponseProvider.failed(-1, e.message.toString(), null)
        }

    }

    suspend fun getCapsuleContentById(capsuleId : String) : GlobalResponse<CapsuleWire?> {
        try {
            val result = DatabaseProvider.dbQuery {
                capsuleRepository.capsuleWithContentsAndRecipient(capsuleId)?.toWire()
            }

            return GlobalResponseProvider.new(0, "", result)
        } catch (e : Exception) {
            throw CustomException(ErrorCode.FAILED_TO_QUERY, e.message)
        }
    }

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
    ) : GlobalResponse<CapsuleCreateResponse> {

        try {
            val encryptedData = TimeBaseEncryptionProvider.encryptWithTimelock(content, openData)
            var capsuleID = ""

            DatabaseProvider.dbQuery {
                timeCapsuleEncryptionMapperRepository.create(encryptedData.encryptedDataKey, encryptedData.timeSalt)

                capsuleID = capsuleRepository.createCapsule(userID, title, description, openData.toLocalDateTime())
                capsuleContentRepository.createCapsuleContent(capsuleID, contentType, encryptedData.encryptedContent)
                recipientsRepository.create(capsuleID, recipientEmail)
            }

            return GlobalResponseProvider.new(0, "success", CapsuleCreateResponse(
                recipientEmail = recipientEmail,
                capsuleId = capsuleID,
                contentType = contentType,
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
    ) : GlobalResponse<CapsuleCreateResponse> {
        try {
            val encryptedData = TimeBaseEncryptionProvider.encryptWithTimelock(content, openData)
            var capsuleID = ""
            var filePath = ""

            DatabaseProvider.dbQuery {
                timeCapsuleEncryptionMapperRepository.create(encryptedData.encryptedDataKey, encryptedData.timeSalt)

                capsuleID = capsuleRepository.createCapsule(userID, title, description, openData.toLocalDateTime())
                capsuleContentRepository.createCapsuleContent(capsuleID, contentType, encryptedData.encryptedContent)
                recipientsRepository.create(capsuleID, recipientEmail)

                // File UPload하는 시간이 길어지면, tranasction connection이 계속 열려있는데.. 괜찮을까?
                filePath = FileStorageRepository.filePathMaker(userID, fileName, title)
                FileStorageRepository.uploadFile(userID, file,fileName, filePath)
            }

            return GlobalResponseProvider.new(0, "success", CapsuleCreateResponse(
                recipientEmail = recipientEmail,
                fileName = fileName,
                fileSize = file.size,
                filePath = filePath,
                capsuleId = capsuleID,
                contentType = contentType
            ))
        } catch (e: Exception) {
            return GlobalResponseProvider.failed(-1, "failed to upload file ${e.message}", null)
        }
    }
}