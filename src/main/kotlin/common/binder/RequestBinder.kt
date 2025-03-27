package com.example.common.binder


import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import io.ktor.server.request.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

// 필드 바인딩 필드
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestParam(
    val name: String = "", // 필드 명
    val source: ParamSource = ParamSource.ANY, // 소스
    val required: Boolean = false // 필수 유무
)

enum class ParamSource {
    PATH, QUERY, BODY, ANY
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestData


object RequestBinder {
    private val objectMapper = jacksonObjectMapper()

    sealed class BindResult<out T> {
        data class Success<T>(val data: T) : BindResult<T>()
        data class Error(val fieldName: String, val message: String) : BindResult<Nothing>()
    }

    internal suspend inline fun <reified T: Any> bind(call: ApplicationCall): BindResult<T> {
        val clazz = T::class

        if (clazz.findAnnotation<RequestData>() == null) {
            return BindResult.Error("class", "Class ${clazz.simpleName} is not annotated with @RequestData")
        }

        // 요청 본문 파싱 (JSON 형식 가정)
        val requestBody: JsonNode? = try {
            call.receiveText().let { text ->
                if (text.isNotBlank()) objectMapper.readTree(text) else null
            }
        } catch (e: Exception) {
            null
        }

        val constructor = clazz.constructors.first()
        val arguments = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val property = clazz.memberProperties.find { it.name == param.name }
            val annotation = property?.findAnnotation<RequestParam>()

            if (annotation == null) {
                arguments[param] = null
                continue
            }

            val paramName = if (annotation.name.isNotEmpty()) annotation.name else param.name!!

            val value = when (annotation.source) {
                ParamSource.PATH -> call.parameters[paramName]
                ParamSource.QUERY -> call.request.queryParameters[paramName]
                ParamSource.BODY -> {
                    requestBody?.get(paramName)?.let {
                        convertJsonNodeToType(it, param.type)
                    }
                }

                ParamSource.ANY -> TODO()
            }

            if (annotation.required && value == null) {
                return BindResult.Error(paramName, "$paramName is required")
            }

            arguments[param] = value
        }

        return try {
            BindResult.Success(constructor.callBy(arguments))
        } catch (e: Exception) {
            BindResult.Error("construction", "Failed to create instance: ${e.message}")
        }
    }

    fun convertJsonNodeToType(node: JsonNode, type: KType): Any? {
        return when {
            node.isNull -> null
            type.isSubtypeOf(String::class.createType()) -> node.asText()
            type.isSubtypeOf(Int::class.createType()) -> if (node.isInt) node.asInt() else null
            type.isSubtypeOf(Long::class.createType()) -> if (node.isLong || node.isInt) node.asLong() else null
            type.isSubtypeOf(Double::class.createType()) -> if (node.isDouble || node.isFloat) node.asDouble() else null
            type.isSubtypeOf(Boolean::class.createType()) -> if (node.isBoolean) node.asBoolean() else null
            else -> null
        }
    }
}
