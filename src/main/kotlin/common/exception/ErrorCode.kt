package com.example.common.exception

enum class ErrorCode(
    override val code : Int,
    override var message : String
) : CodeInterface {
    FAILED_TO_READ_BODY(-1, "failed to request body"),
    NOT_SUPPORTED_REQUEST_TYPE(-2, "not supported request type"),
    NOT_SUPPORTED_EMAIL_FORMAT(-3, "not supported email format"),
    FAILED_TO_INIT_SECURITY(-4, "failed to init security"),
    FILE_NOT_FOUND(-5, "file not found"),
    FAILED_TO_HANDLE_FILE(-6, "failed to handle file"),
}