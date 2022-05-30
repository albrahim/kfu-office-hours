package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.DatabaseFactory.dbQuery
import com.example.extensions.findOne
import com.example.models.*
import com.example.plugins.issueTokenForUser
import com.example.plugins.jwt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.security.crypto.bcrypt.BCrypt
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun resultRowToUser(row: ResultRow) = UserData(
    id = row[Users.id].value,
    username = row[Users.username],
    email = row[Users.email],
    firstName = row[Users.firstName],
    lastName = row[Users.lastName],
    role = row[Users.role]
)

fun Route.userRoutes() {
    route("/user") {
        authenticate {
            route("me") {
                getAuthedUserInfoRoute()
                editAuthedUserInfoRoute()
                deleteAuthedUserRoute()
            }
            renewUserAuthRoute()
            listUsersRoute()
            findUserRoute()
        }
        createUserRoute()
        authUserRoute()
        deleteUserRoute()
    }
}

fun Route.getAuthedUserInfoRoute() {
    get {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()
        dbQuery {
            User.findById(id)?.also {
                call.respond(it.data)
            }
        }
    }
}

fun Route.editAuthedUserInfoRoute() {
    patch {
        val (username, password, firstName, lastName, email) = call.receive<UserEditRequest>()
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        username?.also { username ->
            val usernameRegex = """^[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*${'$'}""".toRegex()
            val isValidUsername = username.matches(usernameRegex)
            if (!isValidUsername) return@patch call.respondText("Invalid username", status = HttpStatusCode.BadRequest)
        }

        email?.also { email ->
            val emailRegex = """^(?=.{1,64}@)[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})${'$'}""".toRegex()
            val isValidEmail = email.matches(emailRegex)
            if (!isValidEmail) return@patch call.respondText("Invalid email", status = HttpStatusCode.BadRequest)
        }

        password?.also { password ->
            val isValidPassword = password.isNotBlank() && password.length >= 8
            if (!isValidPassword) return@patch call.respondText("Invalid password", status = HttpStatusCode.BadRequest)
        }

        firstName?.also { firstName ->
            val isValidFirstName = firstName.isNotBlank()
            if (!isValidFirstName) return@patch call.respondText("Invalid first name", status = HttpStatusCode.BadRequest)
        }

        lastName?.also {
            val isValidLastName = lastName.isNotBlank()
            if (!isValidLastName) return@patch call.respondText("Invalid last name", status = HttpStatusCode.BadRequest)
        }

        dbQuery {
            User.findById(id)
                ?.also { user ->
                    user.username = username ?: user.username
                    user.password = password ?: user.password
                    user.email = email ?: user.email
                    user.firstName = firstName?.trim() ?: user.firstName
                    user.lastName = lastName?.trim() ?: user.lastName
                    return@dbQuery call.respond(HttpStatusCode.Accepted)
                }
        }
    }
}

fun Route.deleteAuthedUserRoute() {
    delete {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        dbQuery {
            val user = User.findById(id)
            if (user != null) {
                user.delete()
                call.respondText("User removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}

fun Route.renewUserAuthRoute() {
    get("renew-token") {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()
        dbQuery {
            User.findById(id)?.also { user ->
                val token = issueTokenForUser(user.data)
                call.respond(UserSignInResponseSuccess(token = token, success = "success", user = user.data))
            }
        }
    }
}

fun Route.createUserRoute() {
    post {
        val (_username, _password, _email, _firstName, _lastName, _role) = call.receive<UserSignupRequest>()

        val usernameRegex = """^[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*${'$'}""".toRegex()
        val isValidUsername = _username.matches(usernameRegex)
        if (!isValidUsername) return@post call.respondText("Invalid username", status = HttpStatusCode.BadRequest)

        val emailRegex = """^(?=.{1,64}@)[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})${'$'}""".toRegex()
        val isValidEmail = _email.matches(emailRegex)
        if (!isValidEmail) return@post call.respondText("Invalid email", status = HttpStatusCode.BadRequest)

        val isValidPassword = _password.isNotBlank() && _password.length >= 8
        if (!isValidPassword) return@post call.respondText("Invalid password", status = HttpStatusCode.BadRequest)

        val isValidFirstName = _firstName.isNotBlank()
        if (!isValidFirstName) return@post call.respondText("Invalid first name", status = HttpStatusCode.BadRequest)

        val isValidLastName = _lastName.isNotBlank()
        if (!isValidLastName) return@post call.respondText("Invalid last name", status = HttpStatusCode.BadRequest)

        dbQuery {
            val existingUser =
                User.findOne((Users.email.lowerCase() eq _email.lowercase()) or (Users.username.lowerCase() eq _username.lowercase()))

            val isUsedUsername = _username.lowercase() == existingUser?.username?.lowercase()
            if (isUsedUsername) return@dbQuery call.respondText("Username already in use", status = HttpStatusCode.Conflict)

            val isUsedEmail = _email.lowercase() == existingUser?.email?.lowercase()
            if (isUsedEmail) return@dbQuery call.respondText("Email already in use", status = HttpStatusCode.Conflict)

            val pw_hash = BCrypt.hashpw(_password, BCrypt.gensalt())

            val user = User.new {
                username = _username
                password = pw_hash
                email = _email
                firstName = _firstName.trim()
                lastName = _lastName.trim()
                role = _role
            }
            val token = issueTokenForUser(user.data)
            call.response.status(HttpStatusCode.Created)
            return@dbQuery call.respond(UserSignInResponseSuccess(token = token, success = "User signed up correctly", user = user.data))
        }
    }
}

fun Route.listUsersRoute() {
    get {
//        val principal = call.principal<JWTPrincipal>()
//        val id = principal!!.payload.getClaim("userid").asInt()
        dbQuery {
            User.all()
                .also {
                    if (!it.empty()) {
                        call.respond(it.map { it.data.public })
                    } else {
                        call.respondText("No Users found", status = HttpStatusCode.OK)
                    }
                }
        }
    }
}

fun Route.findUserRoute() {
    get("{id?}") {
//        val principal = call.principal<JWTPrincipal>()
//        val id = principal!!.payload.getClaim("userid").asInt()
        dbQuery {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@dbQuery call.respondText(
                "Missing ID",
                status = HttpStatusCode.BadRequest
            )
            val user = User.findById(id)?.data?.public ?: return@dbQuery call.respondText(
                "No User is with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(user)
        }
    }
}

fun Route.authUserRoute() {
    post("signin") {
        val (username, password) = call.receive<UserSignInRequest>()
        dbQuery {
            val user = User.findOne(Users.username.lowerCase() eq username.lowercase())
            if (user != null && BCrypt.checkpw(password, user.password)) {
                val token = issueTokenForUser(user.data)
                call.respond(UserSignInResponseSuccess(token = token, success = "success", user = user.data))
            } else {
                call.response.status(HttpStatusCode.NotFound)
                call.respond(UserSignInResponseFail(fail = "Wrong username or password"))
            }
        }

    }
}

fun Route.deleteUserRoute() {
    delete("{id?}") {
        return@delete call.respond(HttpStatusCode.Forbidden)
        val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
        dbQuery {
            val user = User.findById(id)
            if (user != null) {
                user.delete()
                call.respondText("User removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}