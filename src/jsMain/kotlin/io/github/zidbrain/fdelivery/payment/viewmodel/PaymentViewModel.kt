package io.github.zidbrain.fdelivery.payment.viewmodel

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.home.model.ItemsWithCategories
import io.github.zidbrain.fdelivery.home.viewmodel.BasketHolder
import io.github.zidbrain.fdelivery.payment.model.DeliveryAddressDto
import io.github.zidbrain.fdelivery.payment.model.OrderItemDto
import io.github.zidbrain.fdelivery.payment.model.OrderRequestDto
import io.github.zidbrain.fdelivery.payment.model.OrderResponseDto
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlin.reflect.KClass

class PaymentViewModel : ViewModel<PaymentAction, PaymentState, PaymentEvent>() {
    override val initialState: PaymentState = PaymentState.Loading
    override val actions: Map<KClass<out PaymentAction>, UseCase<PaymentAction, PaymentState, PaymentEvent>> = mapOf(
        PaymentAction.Init::class to SimpleUseCase<PaymentAction> {
            emit(PaymentState.Loading)
            val response = ApiClient.get<ItemsWithCategories>("/api/categories")
            val deliveryFee = response.deliveryFee
            val basket = response.categories.flatMap { it.items }
                .mapNotNull { item -> BasketHolder.items[item.id]?.let { item to it } }.toMap()
            val itemsPrice = basket.entries.sumOf { (item, amount) -> item.price!! * amount }
            emit(PaymentState.Content(deliveryFee = deliveryFee, itemsPrice = itemsPrice))
        }.catch {
            emit(PaymentState.Error)
        },
        PaymentAction.Pay::class to SimpleUseCase<PaymentAction.Pay> {
            emit(PaymentState.Loading)
            val request = OrderRequestDto(
                deliveryAddress = DeliveryAddressDto(
                    destination = it.address,
                    apartment = it.apartment,
                    entrance = it.entrance,
                    floor = it.floor
                ),
                order = BasketHolder.items.map { (id, amount) -> OrderItemDto(id = id, amount = amount) }
            )
            val response = ApiClient.post<OrderRequestDto, OrderResponseDto>("/api/orders", request)
            BasketHolder.putIntoSessionStorage(emptyMap())
            sendEvent(PaymentEvent.Success(response.id))
        }.catch {
            println(it.toString())
            emit(PaymentState.Error)
        }
    )

    override fun onActionsBound() {
        sendAction(PaymentAction.Init)
    }
}

sealed class PaymentAction {
    internal data object Init : PaymentAction()
    data class Pay(
        val address: String,
        val apartment: Int,
        val floor: Int,
        val entrance: Int
    ) : PaymentAction()
}

sealed class PaymentState {
    data object Loading : PaymentState()
    data object Error : PaymentState()

    data class Content(val deliveryFee: Double, val itemsPrice: Double) : PaymentState()
}

sealed class PaymentEvent {
    data class Success(val paymentId: String) : PaymentEvent()
}