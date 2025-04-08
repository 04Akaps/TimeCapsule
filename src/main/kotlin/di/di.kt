package com.example.di

import com.example.repository.*
import com.example.routes.auth.service.AuthService
import com.example.routes.capsule.service.CapsuleService
import org.koin.dsl.module


val repositoryModule = module {
    single { CapsuleRepository() }
    single { UserRepository() }
    single { UserTokenMapperRepository() }
    single { CapsuleContentRepository() }
    single { RecipientsRepository() }
}

val appModule = module {
    repositoryModule

    single { AuthService(get(), get()) }
    single { CapsuleService(
        get(),
        get(),
        get(),
        get(),
    ) }
}