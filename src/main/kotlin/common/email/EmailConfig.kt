package com.example.common.email

import io.ktor.server.config.*

data class EmailConfig(
    val provider: EmailProviderType,
    val fromEmail: String,
    val fromName: String,
    val debugEnabled: Boolean,
    val jakartaMail: JakartaMailConfig?,
    val awsSes: AwsSesConfig?
) {
    data class JakartaMailConfig(
        val host: String,
        val port: String,
        val username: String,
        val password: String,
        val connectionTimeout: Int,
        val timeout: Int,
    )

    data class AwsSesConfig(
        val region: String,
        val accessKey: String,
        val secretKey: String,
    )

    companion object {
        fun fromApplicationConfig(config: ApplicationConfig): EmailConfig {
            val emailConfig = config.config("email")

            // 기본 설정
            val providerStr = emailConfig.propertyOrNull("provider")?.getString() ?: "jakarta-mail"
            val provider = EmailProviderType.fromString(providerStr)
            val fromEmail = emailConfig.propertyOrNull("fromEmail")?.getString()
                ?: throw IllegalArgumentException("보내는 이메일 주소가 설정되지 않았습니다")
            val fromName = emailConfig.propertyOrNull("fromName")?.getString() ?: ""
            val debugEnabled = emailConfig.propertyOrNull("debugEnabled")?.getString()?.toBoolean() ?: false

            // Jakarta Mail을 사용한다면
            val jakartaMail = if (provider == EmailProviderType.JAKARTA_MAIL) {
                val jmConfig = emailConfig.config("jakartaMail")
                JakartaMailConfig(
                    host = jmConfig.propertyOrNull("host")?.getString() ?: "smtp.gmail.com",
                    port = jmConfig.propertyOrNull("port")?.getString() ?: "587",
                    username = jmConfig.propertyOrNull("username")?.getString()
                        ?: throw IllegalArgumentException("SMTP 사용자 이름이 설정되지 않았습니다"),
                    password = jmConfig.propertyOrNull("password")?.getString()
                        ?: throw IllegalArgumentException("SMTP 비밀번호가 설정되지 않았습니다"),
                    connectionTimeout = jmConfig.propertyOrNull("connectionTimeout")?.getString()?.toInt() ?: 5000,
                    timeout = jmConfig.propertyOrNull("timeout")?.getString()?.toInt() ?: 5000,
                )
            } else null

            // ses를 사용한다면,
            val awsSes = if (provider == EmailProviderType.AWS_SES) {
                val sesConfig = emailConfig.config("awsSes")
                AwsSesConfig(
                    region = sesConfig.propertyOrNull("region")?.getString() ?: "us-east-1",
                    accessKey = sesConfig.propertyOrNull("accessKey")?.getString()
                        ?: throw IllegalArgumentException("AWS 액세스 키가 설정되지 않았습니다"),
                    secretKey = sesConfig.propertyOrNull("secretKey")?.getString()
                        ?: throw IllegalArgumentException("AWS 시크릿 키가 설정되지 않았습니다"),
                )
            } else null

            return EmailConfig(
                provider = provider,
                fromEmail = fromEmail,
                fromName = fromName,
                debugEnabled = debugEnabled,
                jakartaMail = jakartaMail,
                awsSes = awsSes
            )
        }
    }
}