package com.example.types.request

import com.example.common.binder.RequestData
import com.example.common.binder.RequestInfo
import com.example.common.binder.RequestSource


@RequestData
data class RequestBodyTest(
    @RequestInfo(name = "email", source = RequestSource.BODY, required = true)
    val email: String,

    @RequestInfo(name = "createdAt", source = RequestSource.BODY)
    val createdAt: Long = System.currentTimeMillis(),

    @RequestInfo(name = "name", source = RequestSource.BODY, required = true)
    val name : String
)

@RequestData
data class RequestPathTest(
    @RequestInfo(name = "id", source = RequestSource.PATH, required = true)
    val id: String,

    @RequestInfo(name = "name", source = RequestSource.PATH, required = true)
    val name : String
)

@RequestData
data class RequestQueryTest(
    @RequestInfo(name = "id", source = RequestSource.QUERY, required = true)
    val t: String,

    @RequestInfo(name = "name", source = RequestSource.QUERY)
    val name : String?
)