package com.example.routes

import com.example.common.utils.*
import com.example.types.*
import com.example.types.request.RequestBodyTest
import com.example.types.request.RequestPathTest
import com.example.types.request.RequestQueryTest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.LocalDateTime

fun Route.sampleRouter() {
    get("/sample") {
        call.respond(Sample("naver.com", LocalDateTime.now()))
    }


    postWithBinding<RequestBodyTest>("/sample-post") { req ->
        println("Name: ${req.email}")
        call.respond(HttpStatusCode.Created, req)
    }

    getWithBinding<RequestPathTest>("/sample-path/{id}/{name}") { req ->
        println(req)
        call.respond(HttpStatusCode.OK, req)
    }

    getWithBinding<RequestQueryTest>("/sample-query") { req ->
        println(req)
        call.respond(HttpStatusCode.OK, req)
    }
}

