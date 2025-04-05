package com.example.routes.auth.service

import com.example.common.database.DatabaseProvider
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.repository.UserRepository
import com.example.routes.auth.types.VerifyCreateAccount
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import com.example.security.RegexProvider
import com.example.security.UlidProvider
import com.example.types.response.GlobalResponse

class AuthService(
    private val userRepository: UserRepository
) {

    suspend fun createUser(email : String, password : String)  : GlobalResponse<String> {
        verifyEmailFormat(email)

        val hashedPassword = PBFDK2Provider.encrypt(password)

        return DatabaseProvider.dbQuery {
            val userInfo = userRepository.findByEmail(email)

            if (userInfo != null) {
                return@dbQuery GlobalResponse(1, "failed", "already exists")
            } else {
                val userID  = UlidProvider.userId()
                userRepository.create(email, userID,  hashedPassword)

                val token = PasetoProvider.createToken(userID, email)
                userRepository.createPasetoToken(userID, token)

                return@dbQuery GlobalResponse(1, "success", "created")
            }

        }
    }

    // email을 기반으로 user 정보가 존재하는지 확인 및 password 중복 검증
    suspend fun verifyCreateUserRequest(email : String) : GlobalResponse<VerifyCreateAccount> {
       verifyEmailFormat(email)

        userRepository.findByEmail(email) ?: return GlobalResponse(1, "", VerifyCreateAccount( "not exist email", email, true))

        return GlobalResponse(1, "", VerifyCreateAccount( "exist email", email, false))
    }


    private fun verifyEmailFormat(email: String) {
        if (!email.matches(Regex(RegexProvider.emailRegex))) {
            throw CustomException(ErrorCode.NOT_SUPPORTED_EMAIL_FORMAT, email)
        }
    }


}