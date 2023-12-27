package io.github.zidbrain.fdelivery.profile.viewmodel

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.order.model.OrderStatusDto
import io.github.zidbrain.fdelivery.payment.model.OrderDto
import io.github.zidbrain.fdelivery.profile.model.ProfileInfoDto
import io.github.zidbrain.fdelivery.util.replace
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

class ProfileViewModel : ViewModel<ProfileAction, ProfileState, Nothing>() {
    override val initialState: ProfileState = ProfileState.Loading

    override val actions: Map<KClass<out ProfileAction>, UseCase<ProfileAction, ProfileState, Nothing>> = mapOf(
        ProfileAction.Init::class to SimpleUseCase<ProfileAction> {
            emit(ProfileState.Loading)

            val orders = ApiClient.get<List<OrderDto>>("/api/orders")
            emit(
                ProfileState.Content(
                    name = ApiClient.credentials!!.name,
                    lastName = ApiClient.credentials!!.lastName,
                    orders = orders,
                    changesSaving = false
                )
            )

            emitAll(ApiClient.webSocket<OrderStatusDto>("/api/orderStatus", viewModelScope).map { status ->
                val content by currentState.ensure<ProfileState.Content>()
                val current = content
                current.copy(
                    orders = current.orders.toMutableList().apply {
                        replace({ it.id == status.orderId }) {
                            it.copy(
                                status = status.status,
                                assignedTo = status.assignedTo
                            )
                        }
                    }
                )
            })
        }.catch {
            emit(ProfileState.Error)
        },
        ProfileAction.SaveChanges::class to SimpleUseCase<ProfileAction.SaveChanges> { action ->
            val content by currentState.ensure<ProfileState.Content>()
            emit(content.copy(changesSaving = true))

            val setName = action.name.takeIf { it != ApiClient.credentials!!.name }
            val setLastName = action.lastName.takeIf { it != ApiClient.credentials!!.lastName }
            val profileInfo = ProfileInfoDto(
                name = setName,
                lastName = setLastName,
                password = action.password
            )

            ApiClient.put("/api/auth/profileInfo", profileInfo)

            val oldCreds = ApiClient.credentials!!
            ApiClient.credentials = oldCreds.copy(
                name = setName ?: oldCreds.name,
                lastName = setLastName ?: oldCreds.lastName
            )
            emit(
                content.copy(
                    name = ApiClient.credentials!!.name,
                    lastName = ApiClient.credentials!!.lastName,
                    changesSaving = false
                )
            )
        }.catch {
            emit(ProfileState.Error)
        }
    )

    override fun onActionsBound() {
        sendAction(ProfileAction.Init)
    }
}

sealed class ProfileAction {
    internal data object Init : ProfileAction()
    data class SaveChanges(
        val name: String,
        val lastName: String,
        val password: String? = null
    ) : ProfileAction()
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Content(val name: String, val lastName: String, val orders: List<OrderDto>, val changesSaving: Boolean) :
        ProfileState()

    data object Error : ProfileState()
}