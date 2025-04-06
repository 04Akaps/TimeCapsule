package com.example.routes.capsule.route

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileHandler
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

            // scheduledOpenDate -> TODO 현재시간보다 커야 한다.

            when(req.contentType) {
                ContentType.TEXT.name -> {
                    if (req.content.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, GlobalResponseProvider.new(-1, "content must provided", null))
                    }
                    val response = service.handlingTextContent(req.title, req.content, req.description, req.scheduledOpenDate)

                    call.respond(HttpStatusCode.OK, response)
                }
                ContentType.AUDIO.name , ContentType.VIDEO.name, ContentType.IMAGE.name  -> {
                    val multipart = call.receiveMultipart()
                    val fileItem = FileHandler.exportFileData(multipart)

                    val (fileName, fileData) = FileHandler.handlingIncomingFile(fileItem)


                    val response = service.handlingFileContent(
                        req.title, req.content, req.description, req.scheduledOpenDate,
                        fileData, fileName
                    )

                    call.respond(HttpStatusCode.Created, GlobalResponseProvider.new(0, "File uploaded successfully", response))
                }


                else -> {

                }
                }
            }






    }
}
