package com.example.routes.capsule.types

import com.example.types.storage.ContentType


data class CapsuleCreateResponse(
    val capsuleId: String,
    val recipientEmail : String,
    val contentType: ContentType,
    val fileSize : Int? = null,
    val filePath : String? = null,
    val fileName : String? = null
)