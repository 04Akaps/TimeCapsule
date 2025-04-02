package com.example.routes.auth.route

import com.example.common.utils.getWithBinding
import com.example.common.utils.postWithBinding
import com.example.routes.auth.service.AuthService
import com.example.routes.auth.types.CreateNewAccountRequest
import com.example.routes.auth.types.VerifyCreateAccount
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Route.v1AuthRoute() {
    val authService = get<AuthService>()

    route("/auth") {

        postWithBinding<CreateNewAccountRequest>("/create") { req ->
            val response = authService.createUser(req.email, req.password)
            call.respond(HttpStatusCode.OK, response)
        }

        getWithBinding<VerifyCreateAccount>("/verify/{email}") { req ->
            val response = authService.verifyCreateUserRequest(req.email)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
