package com.example.types.response

class GlobalResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)