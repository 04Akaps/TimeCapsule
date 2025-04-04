package com.example.types.response

class GlobalResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)


object GlobalResponseProvider {
    fun <T> new(code : Int, message : String, v : T?) : GlobalResponse<T> = GlobalResponse(code, message, v)

    fun authFailed(code : Int, message: String, v : String) : GlobalResponse<String> = GlobalResponse<String>(code, message, v)
}