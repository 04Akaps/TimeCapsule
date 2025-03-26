package com.example.types

data class LogFormat<T>(
    val requestId: String?,
    val method: String,
    val url: String,
    val request: Request<T>,
)

data class Request<T>(
    val headers: Map<String, List<String>>,
    val request: T?
)