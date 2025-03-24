package com.example

import com.example.plugins.contentNegotiation
import com.example.routes.sampleRouter
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    contentNegotiation()
    routing {
        get("/") {
            call.respond("Hello, Ktor!")
        }

        sampleRouter()
    }
}
