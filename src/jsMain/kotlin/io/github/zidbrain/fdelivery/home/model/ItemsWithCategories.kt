package io.github.zidbrain.fdelivery.home.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemsWithCategories(
    val categories: List<CategoryDto>,
    val deliveryFee: Double
)

@Serializable
data class CategoryDto(val id: String, val name: String, val items: List<ItemDto>)

@Serializable
data class ItemDto(
    val id: String,
    val name: String,
    val price: Double?,
    val imagePath: String
)