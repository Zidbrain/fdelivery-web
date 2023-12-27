package io.github.zidbrain.fdelivery.home.viewmodel

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.home.model.ItemsWithCategories
import io.github.zidbrain.fdelivery.home.viewmodel.BasketHolder.items
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlinx.browser.sessionStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.reflect.KClass

object BasketHolder {
    var items: Map<String, Int> = mapOf()
        private set

    init {
        sessionStorage["basket"]?.let {
            items = Json.decodeFromString(it)
        }
    }

    fun putIntoSessionStorage(values: Map<String, Int>) {
        sessionStorage["basket"] = Json.encodeToString(values)
        items = values
    }
}

class HomeViewModel : ViewModel<HomeAction, HomeState, Nothing>() {

    override val initialState: HomeState = HomeState.Loading

    override val actions: Map<KClass<out HomeAction>, UseCase<HomeAction, HomeState, Nothing>> = mapOf(
        HomeAction.Init::class to SimpleUseCase<HomeAction> {
            emit(HomeState.Loading)
            val response = ApiClient.get<ItemsWithCategories>("/api/categories")
            emit(HomeState.Content(response, items))
        },
        HomeAction.AddItem::class to SimpleUseCase<HomeAction.AddItem> {
            val content = currentState as HomeState.Content
            val amount = (items[it.id] ?: 0) + 1
            BasketHolder.putIntoSessionStorage(items.toMutableMap().apply { put(it.id, amount) })
            emit(content.copy(itemsInBasket = items))
        },
        HomeAction.RemoveItem::class to SimpleUseCase<HomeAction.RemoveItem> {
            val content = currentState as HomeState.Content
            val amount = items[it.id]!! - 1
            BasketHolder.putIntoSessionStorage(
                items.toMutableMap().apply { if (amount == 0) remove(it.id) else put(it.id, amount) })
            emit(content.copy(itemsInBasket = items))
        },
        HomeAction.ClearBasket::class to SimpleUseCase<HomeAction> {
            BasketHolder.putIntoSessionStorage(emptyMap())
            val content = currentState as HomeState.Content
            emit(content.copy(itemsInBasket = items))
        }
    )

    override fun onActionsBound() {
        sendAction(HomeAction.Init)
    }
}

sealed class HomeAction {
    internal data object Init : HomeAction()
    data class AddItem(val id: String) : HomeAction()
    data class RemoveItem(val id: String) : HomeAction()
    data object ClearBasket : HomeAction()
}

sealed class HomeState {
    data object Loading : HomeState()
    data class Content(val items: ItemsWithCategories, val itemsInBasket: Map<String, Int>) : HomeState()
}