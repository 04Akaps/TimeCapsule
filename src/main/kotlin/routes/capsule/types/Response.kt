package com.example.routes.capsule.types


data class UploadFileResponse(
    val capsuleId: String,
    val recipientEmail : String,
    val fileSize : Int,
    val filePath : String,
    val fileName : String
)

data class UploadContentResponse(
    val capsuleId: String,
    val recipientEmail : String,
)