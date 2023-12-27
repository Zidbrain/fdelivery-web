package io.github.zidbrain.fdelivery.order.model

import io.github.zidbrain.fdelivery.payment.model.ExposedUserDto
import io.github.zidbrain.fdelivery.payment.model.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class OrderStatusDto(
    val orderId: String,
    val status: OrderStatus,
    val assignedTo: ExposedUserDto?
)