package com.example.routes.capsule.route

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.utils.getWithBinding
import com.example.common.utils.postWithBinding
import com.example.routes.capsule.service.CapsuleService
import com.example.routes.capsule.types.CreateNewCapsuleRequest
import com.example.security.Intercept
import com.example.security.PasetoProvider
import com.example.types.response.GlobalResponse
import com.example.types.response.GlobalResponseProvider
import com.example.types.storage.ContentType
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import java.io.ByteArrayOutputStream

fun Route.v1CapsuleRoute() {
    val service = get<CapsuleService>()

    route("/capsule") {
        intercept(ApplicationCallPipeline.Call, Intercept.tokenHeaderVerify())

        postWithBinding<CreateNewCapsuleRequest>("/create") { req ->

            val token = call.request.headers["Authorization"].toString() // -> intercept 통해서 검증한 값이기 떄문에 바로 사용 가능
            val userID =  PasetoProvider.getUserId(token)

            if (req.contentType == ContentType.TEXT.name) {
                if (req.content.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new(-1, "content must provided", null))
                }
            } else {
                val multipart = call.receiveMultipart()
                var fileItem: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "file") {
                        fileItem = part
                    } else {
                        part.dispose() // 다른 파트는 즉시 리소스 해제
                    }
                }

                if (fileItem != null) {
                    val fileName = fileItem.originalFileName ?: "unnamed-file"

                    try {
                        val fileDataStream = ByteArrayOutputStream()

                        val chunkSize = 1024 * 1024 // 1MB
                        val inputStream = fileItem.streamProvider()
                        val buffer = ByteArray(chunkSize)

                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            // 청크 데이터 처리
                            fileDataStream.write(buffer, 0, bytesRead)
                        }

                        // 최종 파일 데이터
                        val fileData = fileDataStream.toByteArray()

                        call.respond(HttpStatusCode.Created, GlobalResponseProvider.new(0, "File uploaded successfully",
                            mapOf(
                                "fileName" to fileName,
                                "fileSize" to fileData.size
                            )))
                    }  finally {
                        fileItem.dispose()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new(-1, "No file found with key 'file'", null))
                }
            }


        }
    }
}
