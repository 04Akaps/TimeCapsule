package com.example.security

import com.example.routes.capsule.service.CapsuleService
import com.example.types.response.GlobalResponseProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineInterceptor


object Intercept {
    fun emailHeaderVerify(verifyEmail: suspend (String) -> Boolean): PipelineInterceptor<Unit, ApplicationCall> {
        return PipelineInterceptor@{
            val email = call.request.headers["EmailInfo"].toString()

            if (email.isBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    GlobalResponseProvider.authFailed(HttpStatusCode.Unauthorized.value, "empty email address", email)
                )
                return@PipelineInterceptor finish()
            }

            if (verifyEmail(email)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GlobalResponseProvider.authFailed(HttpStatusCode.BadRequest.value, "invalid email address", email)
                )
                return@PipelineInterceptor finish()
            }

            proceed()
        }
    }
}