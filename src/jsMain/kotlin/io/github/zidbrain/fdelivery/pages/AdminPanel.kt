package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Visibility
import com.varabyte.kobweb.compose.css.visibility
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaPen
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.admin.viewmodel.AdminAction
import io.github.zidbrain.fdelivery.admin.viewmodel.AdminState
import io.github.zidbrain.fdelivery.admin.viewmodel.AdminViewModel
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.attributes.accept
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.FileInput
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.get

@Page
@Composable
fun AdminPanel(viewModel: AdminViewModel = viewModel()) = Column(modifier = Modifier.fillMaxSize()) {
    NavBar()

    val state by viewModel.state.collectAsState()
    when (val it = state) {
        is AdminState.Content -> AdminContent(
            content = it,
            onSaveChanges = { viewModel.sendAction(AdminAction.SaveChanges) },
            onDiscardChanges = { viewModel.sendAction(AdminAction.DiscardChanges) },
            setCategoryName = { id, name -> viewModel.sendAction(AdminAction.SetCategoryName(id, name)) },
            deleteCategory = { viewModel.sendAction(AdminAction.DeleteCategory(it)) },
            addCategory = { viewModel.sendAction(AdminAction.AddCategory) },
            setItemData = { id, name, price -> viewModel.sendAction(AdminAction.SetItemData(id, name, price)) },
            setItemImage = { id, file -> viewModel.sendAction(AdminAction.SetItemImage(id, file)) },
            deleteItem = { viewModel.sendAction(AdminAction.DeleteItem(it)) },
            addItem = { viewModel.sendAction(AdminAction.AddItem(it)) },
            setDeliveryFee = { viewModel.sendAction(AdminAction.SetDeliveryFee(it)) }
        )

        AdminState.Error -> {}
        AdminState.Loading -> LoadingIndicator()
    }
}

@Composable
private fun AdminContent(
    content: AdminState.Content,
    onSaveChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    setCategoryName: (String, String) -> Unit,
    deleteCategory: (String) -> Unit,
    addCategory: () -> Unit,
    setItemData: (String, name: String?, price: String?) -> Unit,
    setItemImage: (String, File) -> Unit,
    deleteItem: (String) -> Unit,
    addItem: (String) -> Unit,
    setDeliveryFee: (String) -> Unit
) = with(content) {
    var input by remember { mutableStateOf<HTMLInputElement?>(null) }
    FileInput {
        style { visibility(Visibility.Hidden) }
        accept("image/*")
        ref {
            input = it
            onDispose {
                input = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().margin(top = 10.px).padding(leftRight = 20.px)) {
        Row(
            modifier = HeavyText.toModifier().margin(left = 10.px, bottom = 16.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Стоимость доставки:")
            TextInput(
                modifier = Modifier.margin(left = 10.px),
                text = deliveryFee,
                onTextChanged = setDeliveryFee,
                placeholder = "Стоимость доставки",
                valid = deliveryFeeValid
            )
        }

        categories.forEach { originalCategory ->
            Row(
                modifier = Modifier.fillMaxWidth().borderRadius(16.px).backgroundColor(rgb(210, 210, 210))
                    .padding(10.px).margin(bottom = 16.px), verticalAlignment = Alignment.CenterVertically
            ) {
                TextInput(
                    text = originalCategory.name,
                    onTextChanged = { changedText ->
                        setCategoryName(originalCategory.id, changedText)
                    },
                    placeholder = "Категория",
                    valid = originalCategory.isValid
                )

                Spacer()
                Button(
                    modifier = HoverButton.toModifier(),
                    onClick = {
                        deleteCategory(originalCategory.id)
                    }
                ) {
                    FaTrash()
                }
            }

            SimpleGrid(numColumns(4), modifier = Modifier.margin(bottom = 10.px)) {
                originalCategory.items.forEach { item ->
                    Box(
                        modifier = HoverCard.toModifier(HoverCardShadow).width(214.px).height(304.px)
                            .margin(right = 10.px, bottom = 10.px)
                            .padding(10.px)
                            .cursor(Cursor.Default)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(70.percent)) {
                                if (item.imageUploading)
                                    LoadingIndicator()
                                else
                                    Image(
                                        src = item.imagePath.onStatic,
                                        modifier = Modifier.align(Alignment.Center).fillMaxSize()
                                    )

                                Button(
                                    onClick = {
                                        input?.onchange = {
                                            setItemImage(item.id, input!!.files!![0]!!)
                                        }
                                        input?.click()
                                    },
                                    modifier = HoverButton.toModifier(GrayHoverButton).align(Alignment.BottomEnd)
                                        .size(40.px),
                                    enabled = !item.imageUploading
                                ) {
                                    FaPen(size = IconSize.SM)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().margin(top = 8.px),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextInput(
                                    text = item.price.orEmpty(),
                                    onTextChanged = { changedText ->
                                        setItemData(item.id, null, changedText)
                                    },
                                    placeholder = "Цена",
                                    valid = item.priceIsValid,
                                    modifier = Modifier.width(60.px)
                                )
                                Text(" ₽", modifier = RegularText.toModifier().margin(left = 8.px))
                            }
                            TextInput(
                                text = item.name,
                                onTextChanged = { changedText ->
                                    setItemData(item.id, changedText, null)
                                },
                                placeholder = "Название",
                                valid = item.name.isNotBlank(),
                                modifier = HeavyText.toModifier().margin(top = 10.px).fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = { deleteItem(item.id) },
                            modifier = HoverButton.toModifier(GrayHoverButton).align(Alignment.TopEnd).size(40.px)
                        ) {
                            FaTrash(size = IconSize.SM)
                        }
                    }
                }

                Box(
                    HoverCard.toModifier(HoverCardShadow).width(214.px).height(304.px)
                        .margin(right = 10.px, bottom = 10.px)
                        .padding(10.px).onClick {
                            addItem(originalCategory.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    FaPlus(size = IconSize.X5)
                }
            }
        }

        Button(
            onClick = {
                addCategory()
            },
            modifier = HoverButton.toModifier().margin(top = 10.px)
        ) {
            FaPlus()
        }

        Row(modifier = Modifier.fillMaxWidth().margin(top = 16.px), horizontalArrangement = Arrangement.End) {
            val canSave = !content.changesSaving && content.canSave
            Button(
                onClick = {
                    onDiscardChanges()
                },
                modifier = HoverButton.toModifier(GrayHoverButton).margin(right = 16.px),
                enabled = canSave
            ) {
                Text("Отмена")
            }
            Button(
                enabled = canSave,
                onClick = {
                    onSaveChanges()
                },
                modifier = HoverButton.toModifier(YellowHoverButton)
            ) {
                Box {
                    if (content.changesSaving)
                        LoadingIndicator()
                    Text("Сохранить изменения", modifier = Modifier.visible(!content.changesSaving))
                }
            }
        }

    }
}

@Composable
fun Modifier.visible(visible: Boolean): Modifier =
    visibility(if (visible) Visibility.Visible else Visibility.Hidden)