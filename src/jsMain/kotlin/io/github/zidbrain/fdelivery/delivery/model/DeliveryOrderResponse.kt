package io.github.zidbrain.fdelivery.delivery.model

import io.github.zidbrain.fdelivery.payment.model.OrderDto
import kotlinx.serialization.Serializable

@Serializable
data class DeliveryOrderResponse(
    val order: OrderDto?
)