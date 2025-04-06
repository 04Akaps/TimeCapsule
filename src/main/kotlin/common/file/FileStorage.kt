package com.example.common.file

import com.example.plugins.StorageType
import io.minio.*
import io.minio.http.Method
import io.ktor.http.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID

object FileStorageRepository {

    private lateinit var client: MinioClient

    internal fun initialize(
        endPoint: String, access : String, secret : String,
        region: String, type : StorageType
    ) {
        val builder = MinioClient.builder()
            .endpoint(endPoint)
            .credentials(access, secret)

        if (type == StorageType.S3 && !region.isNullOrBlank()) {
            builder.region(region)
        }


        this.client = builder.build()
    }

    fun uploadFile(bucketName : String, fileBytes: ByteArray, fileName: String, filePath : String): String {
        verifyBucket(bucketName)

        client.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(filePath)
                .stream(ByteArrayInputStream(fileBytes), fileBytes.size.toLong(), -1)
                .contentType(getContentType(fileName))
                .build()
        )

        return fileName
    }

    fun getFileUrl(bucketName : String, filePath: String): String {
        return client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .`object`(filePath)
                .method(Method.GET)
                .build()
        )
    }

    fun deleteFile(bucketName : String, filePath: String): Boolean {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .`object`(filePath)
                .build()
        )
        return true
    }

    fun getFileAsStream(bucketName :String, filePath: String): InputStream {
        return client.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(filePath)
                .build()
        )
    }

    fun getFileAsByteArray(bucketName :String, filePath: String): ByteArray {
        val stream = getFileAsStream(bucketName, filePath)
        return stream.use { it.readBytes() }
    }

    fun getFileMetadata(bucketName :String, filePath: String): StatObjectResponse {
        return client.statObject(
            StatObjectArgs.builder()
                .bucket(bucketName)
                .`object`(filePath)
                .build()
        )
    }

    private fun verifyBucket(bucket : String) {
        val bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
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

    fun filePathMaker(vararg fileNameComponents: String, separator : String = "/"): String {
        return fileNameComponents.filter { it.isNotBlank() }.joinToString(separator)
    }
}