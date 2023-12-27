package io.github.zidbrain.fdelivery.delivery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeliveryManStatusDto(
    val status: DeliveryManStatus
)

@Serializable
enum class DeliveryManStatus {
    @SerialName("notAvailable")
    NotAvailable,
    @SerialName("busy")
    Busy,
    @SerialName("available")
    Available
}

@Serializable
data class DeliveryManSetStatusRequest(
    val isReady: Boolean
)