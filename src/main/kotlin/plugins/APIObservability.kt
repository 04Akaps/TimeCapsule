package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun Application.APIObservability() {
     install(CallLogging) {
         level = Level.INFO
         filter { it.request.path().startsWith("/api") }
     }

    install(CallMonitoringPlugin)
}

val CallMonitoringPlugin = createApplicationPlugin(name = "CallMonitoringPlugin") {
    val log = LoggerFactory.getLogger("APICallMonitor")

    onCall { call ->
        val startTime = System.currentTimeMillis() / 1000
        call.attributes.put(AttributeKey("start-time"), startTime)
    }

    onCallRespond { call, body ->
        val startTime = call.attributes[AttributeKey("start-time")] as Long
        val duration = System.currentTimeMillis() / 1000 - startTime
        val path = call.request.path()

        log.info("Status: [${call.response.status()}] path: [${path}] body:[${body}] Duration: [${duration}ms]")
    }
}
