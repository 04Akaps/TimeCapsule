package com.example.di

import com.example.repository.UserRepository
import com.example.routes.auth.service.AuthService
import org.koin.dsl.module

val appModule = module {
    single { UserRepository() }
    single { AuthService(get()) }
}