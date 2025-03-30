package com.example.routes.auth.route

import com.example.common.utils.postWithBinding
import com.example.routes.auth.types.CreateNewAccountRequest
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.v1AuthRoute() {
    route("/auth") {

        postWithBinding<CreateNewAccountRequest>("/create") { req ->

        }
    }
}
