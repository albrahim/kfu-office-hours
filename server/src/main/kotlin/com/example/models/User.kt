package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op

@Serializable
enum class UserRole {
    Doctor, Student
}

object Users : IntIdTable() {
    val username = varchar("username", 128)
    val password = varchar("password", 128)
    val email = varchar("email", 128)
    val firstName = varchar("firstName", 128)
    val lastName = varchar("lastName", 128)
    val role = enumeration("role", UserRole::class)
}

class User(id: EntityID<Int>) : Entity<Int>(id) {
    companion object: EntityClass<Int, User>(Users)

    var username by Users.username
    var password by Users.password
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var role by Users.role

    val data get() = UserData(
        id = id.value,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = role
    )
}

@Serializable
data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)

val UserData.public get() = UserPublicData(
    id = this.id,
    firstName = this.firstName,
    lastName = this.lastName,
//    email = this.email,
    role = this.role,
)

@Serializable
data class UserPublicData(
    val id: Int,
    val firstName: String,
    val lastName: String,
//    val email: String,
    val role: UserRole,
)

@Serializable
data class UserSignupRequest(
    val username: String,
    val password: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
)

@Serializable
data class UserSignInRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserSignInResponseSuccess(
    val token: String,
    val success: String,
    val user: UserData,
)

@Serializable
data class UserSignInResponseFail(
    val fail: String
)

@Serializable
data class UserEditRequest(
    val username: String?,
    val password: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
)
