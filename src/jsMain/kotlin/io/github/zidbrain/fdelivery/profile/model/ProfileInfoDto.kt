package io.github.zidbrain.fdelivery.profile.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileInfoDto(
    val name: String? = null,
    val lastName: String? = null,
    val password: String? = null
)