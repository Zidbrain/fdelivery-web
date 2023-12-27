package io.github.zidbrain.fdelivery.admin.viewmodel

import com.varabyte.kobweb.compose.file.readBytes
import io.github.zidbrain.fdelivery.admin.model.DeliveryFeeRequest
import io.github.zidbrain.fdelivery.admin.model.UploadImageResponse
import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.home.model.CategoryDto
import io.github.zidbrain.fdelivery.home.model.ItemDto
import io.github.zidbrain.fdelivery.home.model.ItemsWithCategories
import io.github.zidbrain.fdelivery.util.replace
import io.github.zidbrain.fdelivery.viewmodel.UseCase
import io.github.zidbrain.fdelivery.viewmodel.ViewModel
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import org.w3c.files.File
import kotlin.reflect.KClass

class AdminViewModel : ViewModel<AdminAction, AdminState, Nothing>() {
    private lateinit var originalCategories: List<CategoryDto>
    private lateinit var currentCategories: MutableList<CategoryDto>

    private val currentlyUploading = mutableListOf<String>()

    private var hasChanges: Boolean = false
    private var deliveryFee: String = ""

    private fun List<CategoryDto>.toState(): List<AdminState.CategoryState> = map {
        AdminState.CategoryState(
            id = it.id,
            name = it.name,
            items = it.items.map {
                AdminState.ItemState(
                    id = it.id,
                    name = it.name,
                    price = it.price?.toString(),
                    imagePath = it.imagePath,
                    priceIsValid = it.price != null,
                    imageUploading = it.id in currentlyUploading
                )
            }
        )
    }

    private val List<AdminState.CategoryState>.canSave: Boolean
        get() = hasChanges && all { it.isValid } && currentlyUploading.isEmpty() && deliveryFee.toDoubleOrNull() != null

    private fun <Action : AdminAction> UpdateUseCase(updateCategories: MutableList<CategoryDto>.(Action) -> Unit): UseCase<AdminAction, AdminState, Nothing> =
        SimpleUseCase {
            hasChanges = true
            currentCategories.updateCategories(it)
            val state = currentCategories.toState()
            emit(
                AdminState.Content(
                    categories = state,
                    canSave = state.canSave,
                    changesSaving = false,
                    deliveryFee = deliveryFee
                )
            )
        }

    override val initialState: AdminState = AdminState.Loading
    override val actions: Map<KClass<out AdminAction>, UseCase<AdminAction, AdminState, Nothing>> = mapOf(
        AdminAction.Init::class to SimpleUseCase<AdminAction.Init> {
            val content by currentState.convert<AdminState.Content>()

            if (content == null)
                emit(AdminState.Loading)

            hasChanges = false
            val response = ApiClient.get<ItemsWithCategories>("/api/categories")
            originalCategories = response.categories
            deliveryFee = response.deliveryFee.toString()
            currentCategories = originalCategories.toMutableList()

            val state = currentCategories.toState()
            emit(
                AdminState.Content(
                    categories = state,
                    canSave = state.canSave,
                    changesSaving = false,
                    deliveryFee = deliveryFee
                )
            )
        },
        AdminAction.SaveChanges::class to SimpleUseCase<AdminAction.SaveChanges> {
            val content by currentState.ensure<AdminState.Content>()
            emit(content.copy(changesSaving = true))

            ApiClient.put<List<CategoryDto>>(
                relativePath = "/api/categories",
                model = currentCategories
            )
            ApiClient.put<DeliveryFeeRequest>(
                relativePath = "/api/deliveryFee",
                model = DeliveryFeeRequest(deliveryFee = deliveryFee.toDouble())
            )
            sendAction(AdminAction.Init)
        }.catch {
            emit(AdminState.Error)
            throw it
        },
        AdminAction.DiscardChanges::class to SimpleUseCase<AdminAction> {
            hasChanges = false
            currentCategories = originalCategories.toMutableList()

            val state = currentCategories.toState()
            emit(
                AdminState.Content(
                    categories = state,
                    canSave = state.canSave,
                    changesSaving = false,
                    deliveryFee = deliveryFee
                )
            )
        },

        AdminAction.SetCategoryName::class to UpdateUseCase<AdminAction.SetCategoryName> { action ->
            replace({ it.id == action.id }) { it.copy(name = action.name) }
        },

        AdminAction.DeleteCategory::class to UpdateUseCase<AdminAction.DeleteCategory> { action -> removeAll { it.id == action.id } },

        AdminAction.AddCategory::class to UpdateUseCase<AdminAction.AddCategory> {
            add(
                CategoryDto(
                    id,
                    "",
                    emptyList()
                )
            )
        },

        AdminAction.SetItemData::class to UpdateUseCase<AdminAction.SetItemData> { action ->
            replace({ it.items.any { it.id == action.id } }) {
                it.copy(items = it.items.toMutableList().apply {
                    replace({ it.id == action.id }) {
                        it.copy(
                            name = action.name ?: it.name,
                            price = if (action.price == null) it.price else action.price.toDoubleOrNull(),
                        )
                    }
                })
            }
        },

        AdminAction.DeleteItem::class to UpdateUseCase<AdminAction.DeleteItem> { action ->
            replace({ it.items.any { it.id == action.id } }) {
                it.copy(items = it.items.toMutableList().apply {
                    removeAll { it.id == action.id }
                })
            }
        },

        AdminAction.AddItem::class to UpdateUseCase<AdminAction.AddItem> { action ->
            replace({ it.id == action.categoryId }) {
                it.copy(items = it.items.toMutableList().apply {
                    add(
                        ItemDto(
                            id = id,
                            name = "",
                            price = null,
                            imagePath = "/static/image-regular.svg"
                        )
                    )
                })
            }
        },

        AdminAction.SetItemImage::class to SimpleUseCase<AdminAction.SetItemImage> { action ->
            val content by currentState.ensure<AdminState.Content>()
            currentlyUploading.add(action.id)
            emit(content.copy(categories = currentCategories.toState()))

            val fileName = action.file.name.substringAfterLast('/')
            val bytes = action.file.readBytes()
            val form = formData {
                append(
                    key = "image",
                    filename = fileName,
                    contentType = ContentType.fromFileExtension(fileName.substringAfterLast('.')).first()
                ) {
                    writeFully(bytes)
                }
            }
            val response = ApiClient.postFormData<UploadImageResponse>("/api/admin/upload-image", form)

            currentlyUploading.remove(action.id)
            hasChanges = true

            currentCategories.replace({ it.items.any { it.id == action.id } }) {
                it.copy(items = it.items.toMutableList().apply {
                    replace({ it.id == action.id }) {
                        it.copy(
                            imagePath = response.relativePath
                        )
                    }
                })
            }
            val state = currentCategories.toState()
            emit(
                AdminState.Content(
                    categories = state,
                    canSave = state.canSave,
                    changesSaving = false,
                    deliveryFee = deliveryFee
                )
            )
        }.catch {
            emit(AdminState.Error)
            throw it
        },

        AdminAction.SetDeliveryFee::class to SimpleUseCase<AdminAction.SetDeliveryFee> { action ->
            hasChanges = true
            deliveryFee = action.deliveryFee
            emitAs<AdminState.Content> {
                it.copy(deliveryFee = deliveryFee, canSave = it.categories.canSave)
            }
        }
    )

    private var _id = 0
    val id: String
        get() {
            _id += 1
            return _id.toString()
        }

    override fun onActionsBound() {
        sendAction(AdminAction.Init)
    }
}

sealed class AdminAction {
    internal data object Init : AdminAction()
    data object SaveChanges : AdminAction()
    data object DiscardChanges : AdminAction()

    data class SetCategoryName(val id: String, val name: String) : AdminAction()
    data class DeleteCategory(val id: String) : AdminAction()
    data object AddCategory : AdminAction()

    data class SetItemData(val id: String, val name: String? = null, val price: String? = null) : AdminAction()
    data class SetItemImage(val id: String, val file: File) : AdminAction()
    data class DeleteItem(val id: String) : AdminAction()
    data class AddItem(val categoryId: String) : AdminAction()

    data class SetDeliveryFee(val deliveryFee: String) : AdminAction()
}

sealed class AdminState {
    data object Loading : AdminState()
    data object Error : AdminState()

    data class Content(
        val categories: List<CategoryState>,
        val canSave: Boolean,
        val changesSaving: Boolean,
        val deliveryFee: String
    ) : AdminState() {
        val deliveryFeeValid = deliveryFee.toDoubleOrNull() != null
    }

    data class CategoryState(
        val id: String,
        val name: String,
        val items: List<ItemState>
    ) {
        val isValid = name.isNotBlank() && items.all { it.isValid }
    }

    data class ItemState(
        val id: String,
        val name: String,
        val price: String?,
        val priceIsValid: Boolean,
        val imagePath: String,
        val imageUploading: Boolean
    ) {
        val isValid = priceIsValid && price != null && name.isNotBlank()
    }
}