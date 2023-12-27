package io.github.zidbrain.fdelivery.admin.model

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageResponse(
    val relativePath: String
)
