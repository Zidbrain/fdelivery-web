package io.github.zidbrain.fdelivery.admin.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryFeeRequest(
    val deliveryFee: Double
)
