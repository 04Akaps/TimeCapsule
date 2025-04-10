package com.example.common.email

interface EmailService {

    @Throws(EmailException::class)
    fun sendEmail(to: String, subject: String, body: String)

    fun shutdown()
}

class EmailException(message: String, cause: Throwable? = null) :
    Exception(message, cause)


enum class EmailProviderType {
    JAKARTA_MAIL, AWS_SES;

    companion object {
        fun fromString(value: String): EmailProviderType {
            return when (value.lowercase()) {
                "aws-ses", "awsses" -> AWS_SES
                else -> JAKARTA_MAIL
            }
        }
    }
}