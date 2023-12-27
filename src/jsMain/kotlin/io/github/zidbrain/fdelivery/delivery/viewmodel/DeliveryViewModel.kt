package io.github.zidbrain.fdelivery.delivery.viewmodel

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.delivery.model.DeliveryManSetStatusRequest
import io.github.zidbrain.fdelivery.delivery.model.DeliveryManStatus
import io.github.zidbrain.fdelivery.delivery.model.DeliveryManStatusDto
import io.github.zidbrain.fdelivery.delivery.model.DeliveryOrderResponse
import io.github.zidbrain.fdelivery.payment.model.OrderDto
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

class DeliveryViewModel : ViewModel<DeliveryAction, DeliveryState, Nothing>() {
    override val initialState: DeliveryState = DeliveryState.Loading
    override val actions: Map<KClass<out DeliveryAction>, UseCase<DeliveryAction, DeliveryState, Nothing>> = mapOf(
        DeliveryAction.Init::class to SimpleUseCase<DeliveryAction> {
            emit(DeliveryState.Loading)
            val response = ApiClient.get<DeliveryManStatusDto>("/api/delivery/status")
            val order = ApiClient.get<DeliveryOrderResponse>("/api/delivery/order")
            emit(
                DeliveryState.Content(
                    isReady = response.status != DeliveryManStatus.NotAvailable,
                    canChangeStatus = response.status != DeliveryManStatus.Busy,
                    assignedOrder = order.order,
                    changesLoading = false,
                    orderStatusLoading = false
                )
            )

            val content by currentState.ensure<DeliveryState.Content>()
            emitAll(ApiClient.webSocket<OrderDto>("/api/delivery/deliveryStatus", viewModelScope).map {
                content.copy(
                    assignedOrder = it
                )
            })
        }.catch {
            emit(DeliveryState.Error)
            throw it
        },
        DeliveryAction.SwitchStatus::class to SimpleUseCase<DeliveryAction.SwitchStatus> {
            val content = currentState as DeliveryState.Content
            emit(content.copy(isReady = it.isReady))
        },
        DeliveryAction.SaveChanges::class to SimpleUseCase<DeliveryAction> {
            val content by ReadOnlyProperty { _, _ -> currentState as DeliveryState.Content }
            emit(content.copy(changesLoading = true))
            val request = DeliveryManSetStatusRequest(isReady = content.isReady)
            ApiClient.post<DeliveryManSetStatusRequest, Unit>("/api/delivery/status", request)
            emit(content.copy(changesLoading = false))
        },
        DeliveryAction.EscalateStatus::class to SimpleUseCase<DeliveryAction> {
            val content by ReadOnlyProperty { _, _ -> currentState as DeliveryState.Content }
            emit(content.copy(orderStatusLoading = true))
            ApiClient.post<Unit, Unit>("/api/delivery/escalate-status", Unit)
            val order = ApiClient.get<DeliveryOrderResponse>("/api/delivery/order")
            val response = ApiClient.get<DeliveryManStatusDto>("/api/delivery/status")
            emit(
                DeliveryState.Content(
                    isReady = response.status != DeliveryManStatus.NotAvailable,
                    canChangeStatus = response.status != DeliveryManStatus.Busy,
                    assignedOrder = order.order,
                    changesLoading = false,
                    orderStatusLoading = false
                )
            )
        }.catch {
            emit(DeliveryState.Error)
            throw it
        }
    )

    override fun onActionsBound() {
        sendAction(DeliveryAction.Init)
    }
}

sealed class DeliveryAction {
    internal data object Init : DeliveryAction()
    data class SwitchStatus(val isReady: Boolean) : DeliveryAction()
    data object SaveChanges : DeliveryAction()
    data object EscalateStatus : DeliveryAction()
}

sealed class DeliveryState {
    data object Loading : DeliveryState()
    data object Error : DeliveryState()
    data class Content(
        val isReady: Boolean,
        val canChangeStatus: Boolean,
        val assignedOrder: OrderDto?,
        val changesLoading: Boolean,
        val orderStatusLoading: Boolean
    ) :
        DeliveryState()
}