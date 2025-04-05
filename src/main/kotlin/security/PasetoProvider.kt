package com.example.security

import dev.paseto.jpaseto.Paseto
import dev.paseto.jpaseto.Pasetos
import dev.paseto.jpaseto.PasetoParser
import dev.paseto.jpaseto.Version
import dev.paseto.jpaseto.lang.Keys
import io.ktor.server.application.*
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


object PasetoProvider {
    private lateinit var keyPair: KeyPair
    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey
    private lateinit var issuer: String

    internal fun initialize(issuer: String) {
        this.issuer = issuer

        keyPair = Keys.keyPairFor(Version.V2)
        publicKey = keyPair.public
        privateKey = keyPair.private
    }

    fun createToken(userId: String, email: String, roles: List<String> = emptyList()): String {

        val now = Instant.now()

        return Pasetos.V2.PUBLIC.builder()
            .setPrivateKey(privateKey)
            .setIssuedAt(now)
            .setIssuer(issuer)
            .setSubject(userId)
            .claim("email", email)
            .claim("roles", roles)
            .setKeyId(UUID.randomUUID().toString())
            .compact()
    }

    fun verifyToken(token: String): Paseto {
        val processedToken = if (token.startsWith("Bearer ", ignoreCase = true)) {
            token.substring(7)
        } else {
            token
        }

        val parser: PasetoParser = Pasetos.parserBuilder()
            .setPublicKey(publicKey)
            .requireIssuer(issuer)
            .build()

        return parser.parse(processedToken)
    }

    fun getEmail(token: String): String {
        return verifyToken(token).claims["email"].toString()
    }

    fun getUserId(token: String): String {
        return verifyToken(token).claims["sub"].toString()
    }
}