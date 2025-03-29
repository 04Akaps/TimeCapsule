package com.example.types

class GlobalResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)