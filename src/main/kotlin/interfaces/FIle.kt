package com.example.interfaces

interface FileStorage {
    suspend fun uploadFile(bucketName : String, fileBytes: ByteArray, fileName: String): String
    suspend fun getFileUrl(bucketName : String, objectKey: String): String
    suspend fun deleteFile(bucketName : String, objectKey: String): Boolean
}