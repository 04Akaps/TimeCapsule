package com.example

import com.example.plugins.configureDatabase
import com.example.plugins.configureRouting
import com.example.plugins.contentNegotiation
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    contentNegotiation()
    configureDatabase()
    configureRouting()
}
