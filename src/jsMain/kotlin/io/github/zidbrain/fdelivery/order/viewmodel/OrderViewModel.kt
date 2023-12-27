package io.github.zidbrain.fdelivery.order.viewmodel

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.order.model.OrderStatusDto
import io.github.zidbrain.fdelivery.payment.model.OrderDto
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.mapNotNull
import kotlin.reflect.KClass

class OrderViewModel(private val orderId: String) : ViewModel<OrderAction, OrderState, Nothing>() {

    override val initialState: OrderState = OrderState.Loading
    override val actions: Map<KClass<out OrderAction>, UseCase<OrderAction, OrderState, Nothing>> = mapOf(
        OrderAction.Init::class to SimpleUseCase<OrderAction> {
            emit(OrderState.Loading)
            val response = ApiClient.get<OrderDto>("/api/orders/$orderId")
            emit(OrderState.Content(response))

            val content by currentState.ensure<OrderState.Content>()
            emitAll(ApiClient.webSocket<OrderStatusDto>("/api/orderStatus", viewModelScope).mapNotNull {
                val current = content
                if (it.orderId == current.order.id)
                    current.copy(
                        order = current.order.copy(
                            status = it.status,
                            assignedTo = it.assignedTo
                        )
                    )
                else null
            })
        }.catch {
            emit(OrderState.Error)
        }
    )

    override fun onActionsBound() {
        sendAction(OrderAction.Init)
    }
}

sealed class OrderAction {
    internal data object Init : OrderAction()
}

sealed class OrderState {
    data object Loading : OrderState()
    data object Error : OrderState()

    data class Content(val order: OrderDto) : OrderState()
}