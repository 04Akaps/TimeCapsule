package com.example.routes.auth.types

data class VerifyCreateAccount(
    val message : String,
    val email : String,
    val valid : Boolean
)