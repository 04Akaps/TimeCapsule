package com.example.types

data class LogFormat(
    val requestId: String?,
    val host: String,
    val port: Int,
    val method: String,
    val url: String,
    val serviceName: String,
    val request: Request,
    val response: Response,
    val params: Map<String, List<String>>
)

data class Request(
    val headers: Map<String, List<String>>,
    val body: Any?
)

data class Response(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: Any?
)