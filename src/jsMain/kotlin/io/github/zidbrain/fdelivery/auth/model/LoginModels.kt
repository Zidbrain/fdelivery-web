package io.github.zidbrain.fdelivery.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val jwt: String,
    val role: Role,
    val name: String,
    val lastName: String
)

@Serializable
enum class Role {
    User,
    DeliveryMan,
    Admin
}