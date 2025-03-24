package com.example.routes

import com.example.types.LogFormat
import com.example.types.Request
import com.example.types.Response
import com.example.types.Sample
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.call
import io.ktor.server.request.host
import io.ktor.server.request.httpMethod
import io.ktor.server.request.port
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.toMap
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDateTime

val jsonLogger = LoggerFactory.getLogger("JsonLogger")

fun Route.sampleRouter() {
    get("/sample") {
        call.respond(Sample("naver.com", LocalDateTime.now()))
    }

    post("/sample") {
        val sample = call.receive<Sample>()
        call.respond(sample)
    }

    postWithLogging<Sample, Sample>("/logger") {req ->
        req
    }
}

inline fun <reified T : Any, reified R : Any> Route.postWithLogging(
    path: String,
    crossinline block: suspend (T) -> R
) {
    post(path) {
        val request = call.receive<T>()
        val response = block(request)

        val logFormat = LogFormat(
            requestId = MDC.get("REQUEST_ID"),
            host = call.request.host(),
            port = call.request.port(),
            method = call.request.httpMethod.value,
            url = call.request.uri,
            serviceName = "ktor-exposed-sample",
            request = Request(
                headers = call.request.headers.toMap(),
                body = request
            ),
            response = Response(
                status = call.response.status()?.value ?: 500,
                headers = call.response.headers.allValues().toMap(),
                body = response
            ),
            params = call.request.queryParameters.toMap()
        )

        jsonLogger.info(logFormat.toString())
        call.respond(response)
    }
}