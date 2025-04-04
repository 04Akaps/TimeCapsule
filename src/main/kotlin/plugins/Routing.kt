package com.example.plugins

import com.example.routes.auth.route.v1AuthRoute
import com.example.routes.capsule.route.v1CapsuleRoute
import com.example.routes.sampleRouter
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/healthCheck") {
            call.respond("Hello!")
        }

        sampleRouter()

        route("/v1") {
            v1CapsuleRoute()
            v1AuthRoute()
        }
    }
}