package com.example.common.utils

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.security.RegexProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object FormatVerify{

    fun verifyEmailFormat(email: String) {
        if (!email.matches(Regex(RegexProvider.emailRegex))) {
            throw CustomException(ErrorCode.NOT_SUPPORTED_EMAIL_FORMAT, email)
        }
    }

    fun validateFutureDate(scheduledOpenDate: Long): Boolean {
        val currentTimeSeconds = Instant.now().epochSecond
        return scheduledOpenDate > currentTimeSeconds
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneId.systemDefault())
    }

    fun Instant.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(this, ZoneOffset.systemDefault())

}