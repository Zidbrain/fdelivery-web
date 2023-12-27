@file:OptIn(ExperimentalSerializationApi::class)

package io.github.zidbrain.fdelivery.admin.model

import io.github.zidbrain.fdelivery.home.model.ItemDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed class UpdateCategoryAction {

    @Serializable
    @SerialName("create")
    data class Create(val name: String, val description: String, val items: List<ItemDto>) :
        UpdateCategoryAction()

    @Serializable
    @SerialName("update")
    data class Update(
        val id: String,
        val name: String,
        val description: String,
        val items: List<UpdateItemAction>
    ) :
        UpdateCategoryAction()

    @Serializable
    @SerialName("delete")
    data class Delete(val id: String) : UpdateCategoryAction()
}

@Serializable
@JsonClassDiscriminator("type")
sealed class UpdateItemAction {

    @Serializable
    @SerialName("create")
    data class Create(val item: ItemDto) : UpdateItemAction()

    @Serializable
    @SerialName("update")
    data class Update(val item: ItemDto) : UpdateItemAction()

    @Serializable
    @SerialName("delete")
    data class Delete(val id: String) : UpdateItemAction()
}
