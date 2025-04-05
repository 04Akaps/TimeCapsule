package com.example.routes.capsule.route

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
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Route.v1CapsuleRoute() {
    val service = get<CapsuleService>()


    intercept(ApplicationCallPipeline.Call, Intercept.emailHeaderVerify(service::verifyEmail))

    route("/capsule") {
        postWithBinding<CreateNewCapsuleRequest>("/create") { req ->
            if (req.contentType == ContentType.TEXT && req.content.isEmpty()) {
                TODO() // throw exception or return Response
            }

            val token = call.request.headers["Authorization"].toString() // -> intercept 통해서 검증한 값이기 떄문에 바로 사용 가능
            val userID =  PasetoProvider.getUserId(token)


            TODO() // service 연결하여 해당 내부에서 처리 -> ContentType에 따라
        }
    }
}
