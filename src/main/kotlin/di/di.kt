package com.example.di

import com.apple.eawt.Application
import com.example.common.email.EmailConfig
import com.example.common.email.EmailService
import com.example.common.email.EmailServiceFactory
import com.example.repository.*
import com.example.routes.auth.service.AuthService
import com.example.routes.capsule.service.CapsuleService
import org.koin.dsl.module

import io.ktor.server.application.*
import kotlin.math.sin


val additionalModule = module {
    single {
        val env: ApplicationEnvironment = get()
        EmailConfig.fromApplicationConfig(env.config)
    }

    // 이메일 서비스 팩토리 생성
    single { EmailServiceFactory() }

    // 이메일 서비스 생성 (팩토리 사용)
    single<EmailService> {
        val factory: EmailServiceFactory = get()
        val config: EmailConfig = get()
        factory.create(config)
    }
}

val repositoryModule = module {
    single { CapsuleRepository() }
    single { UserRepository() }
    single { UserTokenMapperRepository() }
    single { CapsuleContentRepository() }
    single { RecipientsRepository() }
    single { TimeCapsuleEncryptionMapperRepository() }
    single { CapsuleFileKeyRepository(get()) }
}

var serviceModule = module {
    single { AuthService(
        get(), get()
    ) }
    single { CapsuleService(
        get(),
        get(),
        get(),
        get(),
        get(),
        get(),
        get()
    ) }
}

val appModule = module {
    includes(additionalModule)
    includes(repositoryModule)
    includes(serviceModule)
}