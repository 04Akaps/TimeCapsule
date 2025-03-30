package com.example.common.utils

import com.example.common.binder.RequestBinder
import com.example.common.binder.RequestSource
import com.example.common.exception.CustomException
import com.example.types.response.GlobalResponse
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC

val log = LoggerFactory.getLogger("route-with-logging")

data class LogFormat<T>(
    val requestId: String?,
    val method: String,
    val url: String,
    val request: Request<T>,
)

data class Request<T>(
    val request: T?
)

fun <T> logging(call: ApplicationCall, req: T?) {
    val logFormat = LogFormat(
        requestId = MDC.get("REQUEST_ID"),
        method = call.request.httpMethod.value,
        url = call.request.uri,
        request = Request(
            request = req
        ),
    )

    log.info(
        "Request: [{}] {} {} body={}",
        logFormat.requestId,
        logFormat.method,
        logFormat.url,
        logFormat.request.request
    )
}

suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.handleBinding(
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
    requestSource: RequestSource,
) {

    val result = if (requestSource == RequestSource.BODY) {
        RequestBinder.postBindRequest<T>(call)
    } else {
        RequestBinder.bindRequest<T>(call)
    }

    when(result) {
        is RequestBinder.BindResult.Success -> {
            try {
                logging(call, result.data)
                handler(result.data)
            } catch (e: CustomException) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    GlobalResponse<Any>(
                        code = e.getCodeInterface().code,
                        message = e.message ?: "Unknown error",
                        data = null
                    )
                )
            }
        }
        is RequestBinder.BindResult.Error -> {
            call.respond(
                HttpStatusCode.BadRequest,
                GlobalResponse<Any>(
                    code = -1,
                    message = result.message,
                    data = null
                )
            )
        }
    }
}

inline fun <reified T : Any> Route.postWithBinding(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    post(path) {
        handleBinding(handler, RequestSource.BODY)
    }
}

inline fun <reified T : Any> Route.getWithBinding(
    path: String,
    crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    get(path) {
        handleBinding(handler, RequestSource.QUERY)
    }
}

