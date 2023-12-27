package io.github.zidbrain.fdelivery.payment.model

import io.github.zidbrain.fdelivery.home.model.ItemDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

@Serializable
data class OrderDto(
    val id: String,
    val deliveryAddress: DeliveryAddressDto,
    val order: List<ItemWithAmountDto>,
    val status: OrderStatus,
    val assignedTo: ExposedUserDto?,
    val totalCost: Double,
    val deliveryFee: Double,
    @Serializable(DateSerializer::class)
    val createdAt: Date
)

@OptIn(ExperimentalSerializationApi::class)
@Serializer(Date::class)
object DateSerializer : KSerializer<Date> {

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.toISOString())
    }

    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeString())
}

@Serializable
data class OrderResponseDto(
    val id: String
)

@Serializable
data class OrderRequestDto(
    val deliveryAddress: DeliveryAddressDto,
    val order: List<OrderItemDto>
)

@Serializable
data class OrderItemDto(
    val id: String,
    val amount: Int
)

@Serializable
enum class OrderStatus {
    Created,
    Assembling,
    Delivering,
    Delivered,
    Canceled;

    val isComplete get() = this == Delivered || this == Canceled
}

@Serializable
data class ExposedUserDto(
    val name: String,
    val lastName: String
)

@Serializable
data class ItemWithAmountDto(
    val item: ItemDto?,
    val amount: Int
)