package com.example.common.exception

enum class ErrorCode(
    override val code : Int,
    override var message : String
) : CodeInterface {
    FailedToLoggerInvoke(-100, "Failed to logger invoke"),
}