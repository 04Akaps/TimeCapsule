package com.example.common.utils

import com.example.types.LogFormat
import com.example.types.Request
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.uri
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.toMap
import org.slf4j.LoggerFactory
import org.slf4j.MDC

val logger = LoggerFactory.getLogger("route-with-logging")

inline fun <reified T : Any> Route.postWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    post(path) {
        try {
            // 요청 본문을 미리 읽음
            val requestBody = call.receive<T>()

            // 로그 기록 - 요청 본문 포함
            val logFormat = LogFormat<T>(
                requestId = MDC.get("REQUEST_ID"),
                method = call.request.httpMethod.value,
                url = call.request.uri,
                request = Request(
                    headers = call.request.headers.toMap(),
                    request = requestBody // 요청 본문 포함
                ),
            )

            logger.info(logFormat.toString())

            // 사용자 정의 핸들러에 요청 본문 전달
            handler(requestBody)
        } catch (e: Exception) {
            logger.error("Error processing POST request", e)
            throw e
        }
    }
}

inline fun Route.pathWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    get(path) {
        // 요청 로깅
        try {
            val pathParams = call.parameters.entries().associate { it.key to it.value.firstOrNull() }
            val logFormat = LogFormat<Map<String, String?>>(
                requestId = MDC.get("REQUEST_ID"),
                method = call.request.httpMethod.value,
                url = call.request.uri,
                request = Request(
                    headers = call.request.headers.toMap(),
                    request = pathParams
                ),
            )

            logger.info(logFormat.toString())

            handler()
        } catch (e: Exception) {
            logger.error("Error processing PATH request", e)
            throw e
        }
    }
}

inline fun Route.queryWithLogging(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    get(path) {
        val logFormat = LogFormat<Map<String, List<String>>>(
            requestId = MDC.get("REQUEST_ID"),
            method = call.request.httpMethod.value,
            url = call.request.uri,
            request = Request(
                headers = call.request.headers.toMap(),
                request = call.request.queryParameters.toMap()
            ),
        )

        logger.info(logFormat.toString())

        handler()
    }
}