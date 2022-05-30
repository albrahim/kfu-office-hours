package com.example.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.UserData
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Application.configureSecurity() {
    authentication {
            jwt {
//                val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
//                val issuer = this@configureSecurity.environment.config.property("jwt.domain").getString()
//                realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
                val secret = jwt.secret
                val issuer = jwt.issuer
                val audience = jwt.audience
                realm = jwt.realm
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(secret))
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(audience)) JWTPrincipal(credential.payload) else null
                }
            }
        }
}

object jwt {
    val secret = "secret"
    val issuer = "http://0.0.0.0:8080/"
    val audience = "http://0.0.0.0:8080/"
    val realm = "Access to api"
}

fun issueTokenForUser(user: UserData): String {
    return JWT.create()
        .withAudience(jwt.audience)
        .withIssuer(jwt.issuer)
        .withClaim("userid", user.id)
        .withExpiresAt(
            Date(
                Clock.System.now().plus(180.toDuration(DurationUnit.DAYS)).toEpochMilliseconds()
            )
        )
        .sign(Algorithm.HMAC256(jwt.secret))
}