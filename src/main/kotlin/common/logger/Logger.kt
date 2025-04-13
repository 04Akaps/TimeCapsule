package com.example.common.logger

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {
    fun <T : Any> getLogger(forClass: Class<T>): Logger = LoggerFactory.getLogger(forClass)

    fun <T> loggingStopWatch(logger: Logger, function: (MutableMap<String, Any>) -> T?): T? {
        val logData = mutableMapOf<String, Any>()
        logData["startAt"] = System.currentTimeMillis() / 1000
        var result : T? = null

        try {
            result = function.invoke(logData)
        } catch (e : CustomException) {
            logData["error"] = e.message ?: ""
        } finally {
            logData["endAt"] = System.currentTimeMillis() / 1000
            logger.info(logData.toString())
        }

        return result
    }
}