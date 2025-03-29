package com.example.common.binder


import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import io.ktor.server.application.ApplicationCall
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import io.ktor.server.request.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import sun.jvm.hotspot.HelloWorld.e
import kotlin.text.get

// 필드 바인딩 필드
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestInfo(
    val name: String, // 필드 명
    val source: RequestSource, // 소스
    val required: Boolean = false // 필수 유무
)

enum class RequestSource {
    PATH, QUERY, BODY
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestData


object RequestBinder {
    @PublishedApi
    internal val objectMapper = jacksonObjectMapper()

    sealed class BindResult<out T> {
        data class Success<T>(val data: T) : BindResult<T>()
        data class Error(val fieldName: String, val message: String) : BindResult<Nothing>()
    }

    suspend inline fun <reified T: Any> postBindRequest(call: ApplicationCall) : BindResult<T> {
        val clazz = T::class

        if (clazz.findAnnotation<RequestData>() == null) {
            return BindResult.Error("class", "Class ${clazz.simpleName} is not annotated with @RequestData")
        }

        val requestBody: JsonNode? = try {
            call.receiveText().let { text ->
                if (text.isNotBlank()) objectMapper.readTree(text) else null
            }
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_READ_BODY, e.message)
        }

        val constructor = clazz.constructors.first()
        val arguments = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val property = clazz.memberProperties.find { it.name == param.name }
            val annotation = property?.findAnnotation<RequestInfo>()

            if (annotation == null) {
                continue
            }

            val paramName = if (annotation.name.isNotEmpty()) annotation.name else param.name!!

            val value = when (annotation.source) {
                RequestSource.BODY -> {
                    requestBody?.get(paramName)?.let {
                        convertJsonNodeToType(it, param.type)
                    }
                }

                RequestSource.PATH -> throw CustomException(ErrorCode.FAILED_TO_READ_BODY)
                RequestSource.QUERY -> throw CustomException(ErrorCode.FAILED_TO_READ_BODY)
            }

            if (annotation.required && value == null) {
                return BindResult.Error(paramName, "$paramName is required")
            }

            if (value != null || param.type.isMarkedNullable) {
                arguments[param] = value
            }
        }

        return try {
            BindResult.Success(constructor.callBy(arguments))
        } catch (e: Exception) {
            BindResult.Error("construction",
                "Failed to create instance: ${e.message ?: "Unknown error"} (${e.javaClass.simpleName})")
        }
    }

    suspend inline fun <reified T: Any> bindRequest(call: ApplicationCall): BindResult<T> {
        val clazz = T::class

        if (clazz.findAnnotation<RequestData>() == null) {
            return BindResult.Error("class", "Class ${clazz.simpleName} is not annotated with @RequestData")
        }


        val constructor = clazz.constructors.first()
        val arguments = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val property = clazz.memberProperties.find { it.name == param.name }
            val annotation = property?.findAnnotation<RequestInfo>()

            if (annotation == null) {
                continue
            }

            val paramName = if (annotation.name.isNotEmpty()) annotation.name else param.name!!

            val value = when (annotation.source) {
                RequestSource.PATH -> call.parameters[paramName]
                RequestSource.QUERY -> call.request.queryParameters[paramName]
                RequestSource.BODY -> throw CustomException(ErrorCode.FAILED_TO_READ_BODY)
            }

            if (annotation.required && value == null) {
                return BindResult.Error(paramName, "$paramName is required")
            }

            if (value != null || param.type.isMarkedNullable) {
                arguments[param] = value
            }
        }

        return try {
            BindResult.Success(constructor.callBy(arguments))
        } catch (e: Exception) {
            BindResult.Error("construction",
                "Failed to create instance: ${e.message ?: "Unknown error"} (${e.javaClass.simpleName})")
        }
    }

    fun convertJsonNodeToType(node: JsonNode, type: KType): Any? {
        return when {
            node.isNull -> null

            // 문자열
            type.isSubtypeOf(String::class.createType()) -> when {
                node.isNull -> ""
                else -> node.asText()
            }

            // Long 타입 - 다양한 입력 형식 처리
            type.isSubtypeOf(Long::class.createType()) -> when {
                node.isNumber -> node.asLong()
                node.isTextual -> try { node.asText().toLong() } catch (e: Exception) { null }
                node.isBoolean -> if (node.asBoolean()) 1L else 0L
                else -> 0
            }

            // Int 타입
            type.isSubtypeOf(Int::class.createType()) -> when {
                node.isNumber -> node.asInt()
                node.isTextual -> try { node.asText().toInt() } catch (e: Exception) { null }
                node.isBoolean -> if (node.asBoolean()) 1 else 0
                else -> 0
            }

            // Double 타입
            type.isSubtypeOf(Double::class.createType()) -> when {
                node.isNumber -> node.asDouble()
                node.isTextual -> try { node.asText().toDouble() } catch (e: Exception) { null }
                else -> 0
            }

            // Boolean 타입
            type.isSubtypeOf(Boolean::class.createType()) -> when {
                node.isBoolean -> node.asBoolean()
                node.isTextual -> node.asText().toLowerCase() == "true"
                node.isNumber -> node.asInt() != 0
                else -> false
            }

            else -> null
        }
    }
}
