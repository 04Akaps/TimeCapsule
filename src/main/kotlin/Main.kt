package com.example

import com.example.di.appModule
import com.example.plugins.PBFDK2Encryption
import com.example.plugins.configureDatabase
import com.example.plugins.configureRouting
import com.example.plugins.contentNegotiation
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(PBFDK2Encryption)
    install(Koin) {
        modules(appModule)
    }

    contentNegotiation()
    configureDatabase()
    configureRouting()
}
