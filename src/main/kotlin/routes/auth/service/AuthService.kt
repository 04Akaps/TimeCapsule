package com.example.routes.auth.service

import com.example.common.database.DatabaseProvider
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.utils.FormatVerify
import com.example.repository.UserRepository
import com.example.repository.UserTokenMapperRepository
import com.example.routes.auth.types.VerifyCreateAccount
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import com.example.security.RegexProvider
import com.example.security.UlidProvider
import com.example.types.response.GlobalResponse
import com.example.types.response.GlobalResponseProvider
import com.example.types.wire.UserWire

class AuthService(
    private val userRepository: UserRepository,
    private val userTokenMapperRepository: UserTokenMapperRepository
) {

    suspend fun login(email : String, password : String) : GlobalResponse<String> {
        FormatVerify.verifyEmailFormat(email)

        var userInfo : UserWire? = null

        DatabaseProvider.dbQuery {
            userInfo = userRepository.findByEmail(email)
        }

        if (userInfo == null) {
            return GlobalResponseProvider.new(-1, "not exists", null)
        }else {
            val verify = PBFDK2Provider.verify(userInfo!!.passwordHash, password)
            if (!verify) {
                return GlobalResponseProvider.new(-1, "password in correct", null)
            }

            val token = PasetoProvider.createToken(userInfo!!.id, email)
            DatabaseProvider.dbQuery {
                userTokenMapperRepository.createPasetoToken(userInfo!!.id, token)
            }

            return GlobalResponse(1, "success", token)
        }
    }

    suspend fun createUser(email : String, password : String) : GlobalResponse<String> {
        FormatVerify.verifyEmailFormat(email)

        val hashedPassword = PBFDK2Provider.encrypt(password)

        return DatabaseProvider.dbQuery {
            val userInfo = userRepository.findByEmail(email)

            if (userInfo != null) {
                return@dbQuery GlobalResponse(1, "failed", "already exists")
            } else {
                val userId = userRepository.create(email, hashedPassword)

                val token = PasetoProvider.createToken(userId, email)
                userTokenMapperRepository.createPasetoToken(userId, token)

                return@dbQuery GlobalResponse(1, "success", token)
            }

        }
    }

    // email을 기반으로 user 정보가 존재하는지 확인 및 password 중복 검증
    suspend fun verifyCreateUserRequest(email : String) : GlobalResponse<VerifyCreateAccount> {
        FormatVerify.verifyEmailFormat(email)

        userRepository.findByEmail(email) ?: return GlobalResponse(1, "", VerifyCreateAccount( "not exist email", email, true))

        return GlobalResponse(1, "", VerifyCreateAccount( "exist email", email, false))
    }




}