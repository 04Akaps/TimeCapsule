package com.example.common.email

import org.slf4j.LoggerFactory

class EmailServiceFactory {
    private val logger = LoggerFactory.getLogger(EmailServiceFactory::class.java)

    fun create(config: EmailConfig): EmailService {
        logger.info("이메일 서비스 생성: 유형=${config.provider}")

        try {
            return when (config.provider) {
                EmailProviderType.JAKARTA_MAIL -> {
                    val jmConfig = config.jakartaMail
                        ?: throw IllegalStateException("Jakarta Mail 설정이 누락되었습니다")

                    JakartaMailService(
                        host = jmConfig.host,
                        port = jmConfig.port,
                        username = jmConfig.username,
                        password = jmConfig.password,
                        fromEmail = config.fromEmail,
                        fromName = config.fromName,
                    )
                }

                EmailProviderType.AWS_SES -> {
                    val sesConfig = config.awsSes
                        ?: throw IllegalStateException("AWS SES 설정이 누락되었습니다")

                    AwsSesService(
                        fromEmail = config.fromEmail,
                        region = sesConfig.region,
                        accessKey = sesConfig.accessKey,
                        accessKeySecret = sesConfig.secretKey
                    )
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("이메일 서비스 생성 중 오류 발생: ${e.message}", e)
        }
    }
}