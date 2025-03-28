package com.example.types

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.util.toMap
import org.slf4j.MDC

data class LogFormat<T>(
    val requestId: String?,
    val method: String,
    val url: String,
    val request: Request<T>,
)

data class Request<T>(
    val request: T?
)