package com.example.routes.capsule.types

import com.example.common.binder.RequestData
import com.example.common.binder.RequestInfo
import com.example.common.binder.RequestSource
import com.example.types.storage.ContentType


@RequestData
data class CreateNewCapsuleRequest(
    @RequestInfo(name = "contentType", source = RequestSource.BODY, required = true)
    val contentType : ContentType,

    @RequestInfo(name = "content", source = RequestSource.BODY) // ContentType == TEXT 인 경우에만 유효 및 검증
    val content : String,

    @RequestInfo(name = "title", source = RequestSource.BODY, required = true)
    val title : String,

    @RequestInfo(name = "description", source = RequestSource.BODY, required = true)
    val description : String,

    @RequestInfo(name = "openDate", source = RequestSource.BODY, required = true)
    val scheduledOpenDate : Long
)