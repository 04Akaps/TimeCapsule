package com.example.routes.capsule.service

import com.example.common.database.DatabaseProvider
import com.example.common.email.EmailService
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorageRepository
import com.example.common.utils.FormatVerify
import com.example.repository.*
import com.example.routes.capsule.types.CapsuleCreateResponse
import com.example.routes.capsule.types.OpenCapsuleResponse
import com.example.security.TimeBaseEncryptionProvider
import com.example.security.TimelockedData
import com.example.types.response.GlobalResponse
import com.example.types.response.GlobalResponseProvider
import com.example.types.storage.CapsuleStatus
import com.example.types.storage.ContentType
import com.example.types.wire.CapsuleWire


class CapsuleService(
    private val capsuleContentRepository: CapsuleContentRepository,
    private val recipientsRepository: RecipientsRepository,
    private val capsuleRepository: CapsuleRepository,
    private val userRepository: UserRepository,
    private val timeCapsuleEncryptionMapperRepository: TimeCapsuleEncryptionMapperRepository,
    private val capsuleFileKeyRepository: CapsuleFileKeyRepository,
    private val emailService: EmailService
) {

    suspend fun openCapsuleContent(capsuleId : String) : GlobalResponse<OpenCapsuleResponse> {

        try {
            val scheduledOpenDate = DatabaseProvider.dbQuery {
                capsuleRepository.getOpenDateByCapsuleId(capsuleId)
            } ?: throw CustomException(ErrorCode.NOT_FOUND_CAPSULE, capsuleId)

            if (!FormatVerify.validateFutureDate(scheduledOpenDate)) {
                return GlobalResponseProvider.new(-1, "not reached schedule open data", null)
            }

            val recipientsEmail = DatabaseProvider.dbQuery {
                recipientsRepository.getRecipientsByCapsuleId(capsuleId)
            }

            var notiSended  = false

            // TODO -> 이렇게 처리 할꺼면 사실 코루틴으로 처리하고 update 처리해도 무방하지 않을까 생각
            try {
                emailService.sendEmail(
                    recipientsEmail,
                    "Your Time Capsule is opend!!",
                    """
                    <html>
                    <body>
                        <h1>Your Time Capsule Is Ready!</h1>
                        <p>Great news! The time capsule is now available to open.</p>
                        <p>This capsule was sealed on ${scheduledOpenDate} 
                        and contains memories waiting for you to rediscover.</p>
                        <p>Enjoy your journey back in time!</p>
                        <p>Best regards,<br>The Time Capsule Team</p>
                    </body>
                    </html>
                    """.trimIndent()
                )

                notiSended = true
            } catch (e : Exception) {
                println("Failed To Send Message : ${e.message}")
            }

            val (key, data, timeSalt) = DatabaseProvider.dbQuery {
                timeCapsuleEncryptionMapperRepository.getTimeLockDataWhenOpen(capsuleId)
            } ?: throw CustomException(ErrorCode.NOT_FOUND_ENCRYPTION, capsuleId)
            
            
            val timeLockData = TimelockedData(
                encryptedContent = data,
                encryptedDataKey = key,
                releaseTime = scheduledOpenDate,
                timeSalt = timeSalt
            )

            println("data : ${data}, key : $key, timeSalt : $timeSalt")

            val decryptedContent = TimeBaseEncryptionProvider.decryptWithTimelock(timeLockData)

            DatabaseProvider.dbQuery {
                capsuleContentRepository.changeCapsuleContent(capsuleId, decryptedContent)
                capsuleRepository.changeCapsuleSealStatus(capsuleId, CapsuleStatus.opened)
                recipientsRepository.changeHasViewedAndNotificationSent(capsuleId, true, notiSended)
            }

            return GlobalResponseProvider.new(0, "", null)
        } catch (e: Exception) {
            return GlobalResponseProvider.failed(-1, "Failed to open Capsule Content : ${e.message}", null)
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
        openData : Int,
    ) : GlobalResponse<CapsuleCreateResponse> {

        try {
            val encryptedData = TimeBaseEncryptionProvider.encryptWithTimelock(content, openData)
            var capsuleID = ""

            DatabaseProvider.dbQuery {
                capsuleID = capsuleRepository.createCapsule(userID, title, description, openData)
                timeCapsuleEncryptionMapperRepository.create(capsuleID, encryptedData.encryptedDataKey, encryptedData.timeSalt)
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
        openData: Int,
        file: ByteArray,
        fileName: String
    ) : GlobalResponse<CapsuleCreateResponse> {
        try {
            val encryptedData = TimeBaseEncryptionProvider.encryptWithTimelock(content, openData)
            var capsuleID = ""
            var filePath = ""

            DatabaseProvider.dbQuery {
                capsuleID = capsuleRepository.createCapsule(userID, title, description, openData)

                timeCapsuleEncryptionMapperRepository.create(capsuleID, encryptedData.encryptedDataKey, encryptedData.timeSalt)
                capsuleContentRepository.createCapsuleContent(capsuleID, contentType, encryptedData.encryptedContent)
                recipientsRepository.create(capsuleID, recipientEmail)

                // File UPload하는 시간이 길어지면, tranasction connection이 계속 열려있는데.. 영속성을 위해서라지만 이게 맞을까? 대용량 트래픽 처리 고민
                filePath = FileStorageRepository.filePathMaker(userID, title, fileName)

                capsuleFileKeyRepository.create(capsuleID, filePath, fileName)
                FileStorageRepository.uploadFile(file, fileName, filePath)
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