package com.example.di

import com.example.module
import com.example.repository.UserRepository
import io.ktor.server.application.Application
import org.koin.dsl.module

val appModule = module {
    // 싱글톤으로 관리
    single { UserRepository() }
//    single { AuthService(get()) } // get을 통해서 추가되는 의존성 주입
}