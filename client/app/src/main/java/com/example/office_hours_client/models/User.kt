package com.example.office_hours_client.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    Doctor, Student
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
