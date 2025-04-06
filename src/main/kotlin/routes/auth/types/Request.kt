package com.example.routes.auth.types

import com.example.common.binder.RequestData
import com.example.common.binder.RequestInfo
import com.example.common.binder.RequestSource

@RequestData
data class CreateNewAccountRequest(
    @RequestInfo(name = "email", source = RequestSource.BODY, required = true)
    val email: String,

    @RequestInfo(name = "password", source = RequestSource.BODY, required = true)
    val password: String
)

@RequestData
data class LoginRequest(
    @RequestInfo(name = "email", source = RequestSource.BODY, required = true)
    val email: String,

    @RequestInfo(name = "password", source = RequestSource.BODY, required = true)
    val password: String
)


@RequestData
data class VerifyEmailCreated(
    @RequestInfo(name = "email", source = RequestSource.PATH, required = true)
    val email: String,
)