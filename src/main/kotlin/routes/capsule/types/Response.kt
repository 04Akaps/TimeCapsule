package com.example.routes.capsule.types


data class UploadFileResponse(
    val recipientEmail : String,
    val fileSize : Int,
    val filePath : String,
    val fileName : String
)