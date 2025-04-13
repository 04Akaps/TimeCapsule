package com.example.common.email


import jakarta.mail.*
import jakarta.mail.internet.*
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

class JakartaMailService(
    private val host: String,
    private val port: String,
    private val username: String,
    private val password: String,
    private val fromEmail: String,
    private val fromName: String,
) : EmailService {

    private val logger = LoggerFactory.getLogger(JakartaMailService::class.java)
    private val emailExecutor = Executors.newFixedThreadPool(5)

    override fun sendEmail(to: String, subject: String, body: String) {
        sendEmailInternal(to, subject, body)
    }

    override fun shutdown() {
        try {
            logger.info("Jakarta Mail 서비스 종료 중...")
            emailExecutor.shutdown()
            if (!emailExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow()
            }
            logger.info("Jakarta Mail 서비스가 정상적으로 종료되었습니다.")
        } catch (e: Exception) {
            logger.error("Jakarta Mail 서비스 종료 중 오류 발생: ${e.message}", e)
            emailExecutor.shutdownNow()
        }
    }

    @Throws(EmailException::class)
    private fun sendEmailInternal(
        to: String,
        subject: String,
        body: String,
    ) {
        val properties = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
            put("mail.smtp.auth", "true")
            
            // TLS 설정 추가
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            
            // 디버깅 활성화 (필요시 로그 확인용)
            put("mail.debug", "true")
            
            // 추가 보안 설정
            put("mail.smtp.ssl.protocols", "TLSv1.2")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(fromEmail, fromName))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.subject = subject
            message.sentDate = Date()

            message.setContent(body, "text/html; charset=utf-8")

            Transport.send(message)

            logger.info("Email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email: ${e.message}", e)
            throw EmailException("Failed to send email: ${e.message}", e)
        }
    }

}

