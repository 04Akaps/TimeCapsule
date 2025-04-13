package com.example.routes.capsule.types

import com.example.common.binder.RequestData
import com.example.common.binder.RequestInfo
import com.example.common.binder.RequestSource


@RequestData
data class CreateNewCapsuleRequest(
    @RequestInfo(name = "contentType", source = RequestSource.BODY, required = true)
    val contentType : String,

    @RequestInfo(name = "content", source = RequestSource.BODY) // ContentType == TEXT 인 경우에만 유효 및 검증
    val content : String,

    @RequestInfo(name = "title", source = RequestSource.BODY, required = true)
    val title : String,

    @RequestInfo(name = "description", source = RequestSource.BODY, required = true)
    val description : String,

    @RequestInfo(name = "recipients", source = RequestSource.BODY, required = true)
    val recipients : String,

    @RequestInfo(name = "openDate", source = RequestSource.BODY, required = true)
    val scheduledOpenDate : Int
)


@RequestData
data class CapsuleDetailRequest(
    @RequestInfo(name = "capsuleId", source = RequestSource.PATH, required = true)
    val capsuleId : String,
)


@RequestData
data class OpenCapsuleRequest(
    @RequestInfo(name = "capsuleId", source = RequestSource.BODY, required = true)
    val capsuleId : String,
)