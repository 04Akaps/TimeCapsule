package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val dbConfig = environment.config.config("database")

    val driverClassName = dbConfig.property("driverClassName").getString()
    val jdbcURL = dbConfig.property("jdbcURL").getString()
    val username = dbConfig.property("username").getString()
    val password = dbConfig.property("password").getString()
    val maximumPoolSize = dbConfig.property("maximumPoolSize").getString().toInt()

    val config = HikariConfig().apply {
        this.driverClassName = driverClassName
        this.jdbcUrl = jdbcURL
        this.username = username
        this.password = password
        this.maximumPoolSize = maximumPoolSize

        // 커넥션 풀 최적화 설정
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
}