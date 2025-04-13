package com.example.common.file

import com.example.plugins.StorageType
import io.minio.*
import io.minio.http.Method
import io.ktor.http.*
import io.ktor.server.application.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID

import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists

object FileStorageRepository {

    private lateinit var client: MinioClient

    private const val MAX_IMAGE_SIZE = 5 * 1024 * 1024
    private const val MAX_VIDEO_SIZE = 100 * 1024 * 1024
    private const val DEFAULT_IMAGE_QUALITY = 0.8f
    private const val DEFAULT_VIDEO_CRF = "23"
    private const val DEFAULT_VIDEO_PRESET = "medium"

    private val logger = LoggerFactory.getLogger(FileStorageRepository::class.java)

    private var bucket : String  = "test"

    internal fun initialize(
        bucket : String,
        endPoint: String, access : String, secret : String,
        region: String, type : StorageType
    ) {
        this.bucket = bucket
        val builder = MinioClient.builder()
            .endpoint(endPoint)
            .credentials(access, secret)

        if (type == StorageType.S3 && region.isNotBlank()) {
            builder.region(region)
        }

        this.client = builder.build()
        verifyBucket(bucket)
    }

    fun uploadFile(fileBytes: ByteArray, fileName: String, filePath : String): String {

        val contentType = getContentType(fileName)

        println(contentType)

        val optimizedBytes = when {
            contentType.startsWith("image/") -> optimizeImage(fileBytes, fileName)
            contentType.startsWith("video/") -> optimizeVideo(fileBytes, fileName)
            contentType.startsWith("audio/") -> optimizeAudio(fileBytes, fileName)
            else -> fileBytes // 기타 파일 유형은 최적화 없이 그대로 사용
        }

        client.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filePath)
                .stream(ByteArrayInputStream(optimizedBytes), optimizedBytes.size.toLong(), -1)
                .contentType(getContentType(fileName))
                .build()
        )

        logger.info("File uploaded: $filePath")

        return fileName
    }

    fun getFileUrl(filePath: String): String {
        return client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .`object`(filePath)
                .method(Method.GET)
                .build()
        )
    }

    fun deleteFile(filePath: String): Boolean {
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .`object`(filePath)
                .build()
        )
        return true
    }

    fun getFileAsStream(filePath: String): InputStream {
        return client.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(filePath)
                .build()
        )
    }

    fun getFileAsByteArray(filePath: String): ByteArray {
        val stream = getFileAsStream(filePath)
        return stream.use { it.readBytes() }
    }

    fun getFileMetadata(filePath: String): StatObjectResponse {
        return client.statObject(
            StatObjectArgs.builder()
                .bucket(bucket)
                .`object`(filePath)
                .build()
        )
    }

    private fun optimizeImage(fileBytes: ByteArray, fileName: String): ByteArray {
        if (fileBytes.size <= MAX_IMAGE_SIZE) {
            return fileBytes
        }

        try {
            val image = ImageIO.read(ByteArrayInputStream(fileBytes)) ?: return fileBytes

            val targetWidth = if (image.width > 2000) 2000 else image.width
            val targetHeight = (targetWidth.toFloat() / image.width * image.height).toInt()

            val resized = if (image.width > targetWidth) {
                val newImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
                val g = newImage.createGraphics()
                g.drawImage(image.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
                g.dispose()
                newImage
            } else {
                image
            }

            val output = ByteArrayOutputStream()
            val formatName = fileName.substringAfterLast('.', "jpeg")

            if (formatName.equals("jpg", ignoreCase = true) || formatName.equals("jpeg", ignoreCase = true)) {
                val jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next()
                val jpgParams = jpgWriter.defaultWriteParam
                jpgParams.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                jpgParams.compressionQuality = DEFAULT_IMAGE_QUALITY

                val ios = javax.imageio.stream.MemoryCacheImageOutputStream(output)
                jpgWriter.output = ios
                jpgWriter.write(null, javax.imageio.IIOImage(resized, null, null), jpgParams)
                jpgWriter.dispose()
                ios.close()
            } else {
                ImageIO.write(resized, formatName, output)
            }

            logger.info("optimize image success [$fileName]")

            return output.toByteArray()
        } catch (e: Exception) {
            logger.info("optimize image error: ${e.message}")
            return fileBytes
        }
    }

    private fun optimizeVideo(fileBytes: ByteArray, fileName: String): ByteArray {
        if (fileBytes.size <= MAX_VIDEO_SIZE) {
            return fileBytes
        }

        val tempInputFile = Files.createTempFile("input_", ".$fileName")
        val tempOutputFile = Files.createTempFile("output_", ".$fileName")

        try {
            Files.write(tempInputFile, fileBytes)

            val command = arrayOf(
                "ffmpeg", "-i", tempInputFile.toString(),
                "-c:v", "libx264",
                "-crf", DEFAULT_VIDEO_CRF,
                "-preset", DEFAULT_VIDEO_PRESET,
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                "-y",
                tempOutputFile.toString()
            )

            val exitCode = optimizeUsingFfmpeg(command)

            if (exitCode != 0) {
                return fileBytes
            }

            logger.info("optimize video success [$fileName]")

            return Files.readAllBytes(tempOutputFile)
        } catch (e: Exception) {
            return fileBytes
        } finally {
            tempInputFile.deleteIfExists()
            tempOutputFile.deleteIfExists()
        }
    }

    private fun optimizeAudio(fileBytes: ByteArray, fileName: String): ByteArray {
        val tempInputFile = Files.createTempFile("input_", ".$fileName")
        val tempOutputFile = Files.createTempFile("output_", ".$fileName")

        try {
            Files.write(tempInputFile, fileBytes)

            val command = arrayOf(
                "ffmpeg", "-i", tempInputFile.toString(),
                "-c:a", "aac",
                "-b:a", "192k",
                "-y",
                tempOutputFile.toString()
            )

            val exitCode = optimizeUsingFfmpeg(command)

            if (exitCode != 0) {
                return fileBytes
            }

            logger.info("optimize video success [$fileName]")

            return Files.readAllBytes(tempOutputFile)
        } catch (e: Exception) {
            return fileBytes
        } finally {
            tempInputFile.deleteIfExists()
            tempOutputFile.deleteIfExists()
        }
    }

    private fun optimizeUsingFfmpeg(command : Array<String>) : Int {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        logger.info("optimize video output: $output and exitCode: $exitCode")
        return exitCode
    }

    private fun verifyBucket(bucket : String) {
        val bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
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