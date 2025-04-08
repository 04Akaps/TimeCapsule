package com.example.types.response

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

class GlobalResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)


object GlobalResponseProvider {
    fun <T> new(code : Int, message : String, v : T?) : GlobalResponse<T> = GlobalResponse(code, message, v)

    fun <T> failed(code : Int, message : String, v : T?) : GlobalResponse<T> = GlobalResponse(code, message, v)

    fun authFailed(code : Int, message: String, v : String) : GlobalResponse<String> = GlobalResponse(code, message, v)
}