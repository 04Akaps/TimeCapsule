package com.example.di

import com.example.repository.CapsuleRepository
import com.example.repository.UserRepository
import com.example.routes.auth.service.AuthService
import com.example.routes.capsule.service.CapsuleService
import org.koin.dsl.module

val appModule = module {
    single { CapsuleRepository() }
    single { UserRepository() }
    single { AuthService(get()) }

    single { CapsuleService(
        get(),
        get(),
    ) }
}