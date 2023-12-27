package io.github.zidbrain.fdelivery.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val login: String,
    val password: String,
    val name: String,
    val lastName: String
)
