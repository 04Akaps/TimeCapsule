package com.example.common.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonHandler {
    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        configure(SerializationFeature.INDENT_OUTPUT, true) // prettyPrint 설정
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // ignoreUnknownKeys 설정
        registerModule(JavaTimeModule()) // Java 8 날짜/시간 지원
    }

    fun <T> encodeToJson(data: T): String {
        return mapper.writeValueAsString(data)
    }

    fun <T> decodeFromJson(jsonString: String, valueType: Class<T>): T {
        return mapper.readValue(jsonString, valueType)
    }

    fun encodeToBytes(value: Any): ByteArray {
        return mapper.writeValueAsBytes(value)
    }
}