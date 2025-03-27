package com.example.routes

import com.example.common.utils.pathWithLogging
import com.example.common.utils.postWithLogging
import com.example.common.utils.queryWithLogging
import com.example.types.Sample
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.time.LocalDateTime



fun Route.sampleRouter() {
    get("/sample") {
        call.respond(Sample("naver.com", LocalDateTime.now()))
    }

    post("/sample") {
        val sample = call.receive<Sample>()
        call.respond(sample)
    }


    postWithLogging<Sample>("/sample-post") { req ->
        println("Name: ${req.email}")
        call.respond(HttpStatusCode.Created, "test success")
    }

    queryWithLogging("/sample-query") {
        val name = call.request.queryParameters["name"]
        println("Name parameter: $name") // name 파라미터를 콘솔에 출력
        call.respond(HttpStatusCode.OK, "Query processed")
    }

    pathWithLogging("/sample-path/{id}") {
        val id = call.parameters["id"]
        println("User ID: $id")
        call.respond(HttpStatusCode.OK, "sucess")
    }
}

