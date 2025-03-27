package com.example.common.utils

import com.example.common.exception.CustomException
import com.example.types.LogFormat
import com.example.types.Request
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.toMap
import org.slf4j.LoggerFactory
import org.slf4j.MDC

val log = LoggerFactory.getLogger("route-with-logging")

fun <T> logging(call: ApplicationCall, req: T?) {
    val logFormat = LogFormat(
        requestId = MDC.get("REQUEST_ID"),
        method = call.request.httpMethod.value,
        url = call.request.uri,
        request = Request(
            headers = call.request.headers.toMap(),
            request = req
        ),
    )

    log.info(
        "Request: [{}] {} {} headers={} body={}",
        logFormat.requestId,
        logFormat.method,
        logFormat.url,
        logFormat.request.headers,
        logFormat.request.request
    )
}

inline fun <reified T : Any> Route.postWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    post(path) {
        try {
            val requestBody = call.receive<T>()
            logging(call, requestBody)
            handler(requestBody)
        } catch (e: Exception) {
            log.error("Error processing POST request", e)
            throw e
        }
    }
}

inline fun Route.pathWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    get(path) {
        try {
            val pathParams = call.parameters.entries().associate { it.key to it.value.firstOrNull() }
            logging(call, pathParams)
            handler()
        } catch (e: Exception) {
            log.error("Error processing PATH request", e)
            throw e
        }
    }
}

inline fun Route.queryWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    get(path) {
        try {
            logging(call, call.request.queryParameters.toMap())
            handler()
        } catch (e: Exception) {
            log.error("Error processing PATH request", e)
            throw e
        }
    }
}
