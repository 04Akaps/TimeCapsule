package com.example.common.email


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executors
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider

/**
 * AWS SES를 사용한 이메일 서비스 구현체
 */
class AwsSesService(
    private val fromEmail: String,
    private val region: String,
    private val accessKey: String,
    private val accessKeySecret: String,
) : EmailService {

    private val logger = LoggerFactory.getLogger(AwsSesService::class.java)

    private val sesClient: SesClient

    init {
        val credentials = AwsBasicCredentials.create(
            accessKey,
            accessKeySecret
        )

        sesClient = SesClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }

    override fun sendEmail(to: String, subject: String, body: String) {
        sendEmailInternal(to, subject, body)
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }


    @Throws(EmailException::class)
    private fun sendEmailInternal(
        to: String,
        subject: String,
        body: String,
    ) {
        try {
            sendSimpleEmail(to, subject, body)
            logger.info("이메일 전송 성공: $to")
        } catch (e: Exception) {
            logger.error("이메일 전송 중 오류 발생", e)
            throw EmailException("AWS SES 이메일 전송 오류: ${e.message}", e)
        }
    }

    private fun sendSimpleEmail(to: String, subject: String, body: String) {
        val content = Content.builder()
            .data(body)
            .charset("UTF-8")
            .build()

        val bodyContent = Body.builder()
            .apply {
                text(content)
            }
            .build()

        val message = Message.builder()
            .subject(Content.builder().data(subject).charset("UTF-8").build())
            .body(bodyContent)
            .build()

        val destination = Destination.builder()
            .toAddresses(to)
            .build()

        val request = SendEmailRequest.builder()
            .source(fromEmail)
            .destination(destination)
            .message(message)
            .build()

        val response = sesClient.sendEmail(request)
        logger.debug("이메일 전송 ID: ${response.messageId()}")
    }
}