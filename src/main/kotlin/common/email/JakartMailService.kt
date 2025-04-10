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
    private val connectionTimeout: Int,
    private val timeout: Int,
) : EmailService {

    private val logger = LoggerFactory.getLogger(JakartaMailService::class.java)
    private val emailExecutor = Executors.newFixedThreadPool(5)

    override fun sendEmail(to: String, subject: String, body: String) {
        sendEmailInternal(to, subject, body)
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
            put("mail.smtp.connectiontimeout", connectionTimeout.toString())
            put("mail.smtp.timeout", timeout.toString())
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
            message.setText(body, "utf-8")

            Transport.send(message)

            logger.info("Email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email: ${e.message}", e)
            throw EmailException("Failed to send email: ${e.message}", e)
        }
    }


    override fun shutdown() {
        emailExecutor.shutdown()
        try {
            if (!emailExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                emailExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            emailExecutor.shutdownNow()
        }
    }

}

