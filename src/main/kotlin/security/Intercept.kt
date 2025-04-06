package com.example.security

import com.example.routes.capsule.service.CapsuleService
import com.example.types.response.GlobalResponseProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineInterceptor


object Intercept {
    fun tokenHeaderVerify(): PipelineInterceptor<Unit, ApplicationCall> {
        return PipelineInterceptor@{
            val token = call.request.headers["Authorization"].toString()

            if (token.isBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    GlobalResponseProvider.authFailed(HttpStatusCode.Unauthorized.value, "empty token", token)
                )
                return@PipelineInterceptor finish()
            }

            try {
                PasetoProvider.verifyToken(token)
            } catch (e : Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GlobalResponseProvider.authFailed(HttpStatusCode.BadRequest.value, "invalid token", e.message.toString())
                )
                return@PipelineInterceptor finish()
            }

            proceed()
        }
    }
}