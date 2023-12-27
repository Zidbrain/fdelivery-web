package io.github.zidbrain.fdelivery.cart

import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.home.model.CategoryDto
import io.github.zidbrain.fdelivery.home.model.ItemDto
import io.github.zidbrain.fdelivery.home.model.ItemsWithCategories
import io.github.zidbrain.fdelivery.home.viewmodel.BasketHolder
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import kotlin.reflect.KClass

class CartViewModel : ViewModel<CartAction, CartState, Nothing>() {

    private lateinit var allItems: List<CategoryDto>
    private val cart: Map<ItemDto, Int>
        get() = allItems.flatMap { it.items }
            .mapNotNull { item -> BasketHolder.items[item.id]?.let { item to it } }.toMap()

    private fun price(deliveryFee: Double, basket: Map<ItemDto, Int>): Double =
        deliveryFee + basket.entries.sumOf { (item, amount) -> item.price!! * amount }

    override val initialState: CartState = CartState.Loading
    override val actions: Map<KClass<out CartAction>, UseCase<CartAction, CartState, Nothing>> = mapOf(
        CartAction.Init::class to SimpleUseCase<CartAction> {
            emit(CartState.Loading)
            val response = ApiClient.get<ItemsWithCategories>("/api/categories")
            val deliveryFee = response.deliveryFee
            allItems = response.categories

            emit(CartState.Content(cart, deliveryFee, price(deliveryFee, cart)))
        }.catch {
            emit(CartState.Error)
        },
        CartAction.AddItem::class to SimpleUseCase<CartAction.AddItem> {
            val content = currentState as CartState.Content
            val amount = (BasketHolder.items[it.id] ?: 0) + 1
            BasketHolder.putIntoSessionStorage(BasketHolder.items.toMutableMap().apply { put(it.id, amount) })
            emit(content.copy(cart = cart, totalPrice = price(content.deliveryFee, cart)))
        },
        CartAction.RemoveItem::class to SimpleUseCase<CartAction.RemoveItem> {
            val content = currentState as CartState.Content
            val amount = BasketHolder.items[it.id]!! - 1
            BasketHolder.putIntoSessionStorage(
                BasketHolder.items.toMutableMap().apply { if (amount == 0) remove(it.id) else put(it.id, amount) })
            emit(content.copy(cart = cart, totalPrice = price(content.deliveryFee, cart)))
        }
    )

    override fun onActionsBound() {
        sendAction(CartAction.Init)
    }
}

sealed class CartAction {
    internal data object Init : CartAction()
    data class AddItem(val id: String) : CartAction()
    data class RemoveItem(val id: String) : CartAction()
}

sealed class CartState {

    data object Loading : CartState()

    data class Content(
        val cart: Map<ItemDto, Int>,
        val deliveryFee: Double,
        val totalPrice: Double
    ) : CartState()

    data object Error : CartState()
}