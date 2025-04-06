package com.example.repository

import com.example.interfaces.FileStorage
import io.minio.*
import io.minio.http.Method
import io.ktor.http.*
import java.io.ByteArrayInputStream
import java.util.UUID

class FileStorageRepository(
    private val client: MinioClient
) : FileStorage {

    private fun verifyBucket(bucket : String) {
        val bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
    }

    override suspend fun uploadFile(bucketName : String, fileBytes: ByteArray, fileName: String): String {
        verifyBucket(bucketName)
        val uniqueFileName = generateUniqueFileName(fileName)

        client.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(uniqueFileName)
                .stream(ByteArrayInputStream(fileBytes), fileBytes.size.toLong(), -1)
                .contentType(getContentType(fileName))
                .build()
        )

        return uniqueFileName
    }

    override suspend fun getFileUrl(bucketName : String, objectKey: String): String {
        return client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .`object`(objectKey)
                .method(Method.GET)
                .build()
        )
    }

    override suspend fun deleteFile(bucketName : String, objectKey: String): Boolean {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectKey)
                .build()
        )
        return true
    }

    private fun generateUniqueFileName(originalFileName: String): String {
        return "${UUID.randomUUID()}-$originalFileName"
    }

    private fun getContentType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> ContentType.Image.JPEG.toString()
            fileName.endsWith(".png", true) -> ContentType.Image.PNG.toString()
            fileName.endsWith(".pdf", true) -> ContentType.Application.Pdf.toString()
            fileName.endsWith(".txt", true) -> ContentType.Text.Plain.toString()
            fileName.endsWith(".mp4", true) -> ContentType.Video.MP4.toString()
            fileName.endsWith(".mp3", true) -> ContentType.Audio.MPEG.toString()
            else -> ContentType.Application.OctetStream.toString()
        }
    }
}