package com.example

import com.example.di.appModule
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    startUpValidation()
    APIObservability()
    initialize()

    contentNegotiation()
    configureDatabase()
    configureRouting()
}
