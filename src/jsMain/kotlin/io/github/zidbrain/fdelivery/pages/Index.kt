package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaMinus
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.home.viewmodel.HomeAction
import io.github.zidbrain.fdelivery.home.viewmodel.HomeState
import io.github.zidbrain.fdelivery.home.viewmodel.HomeViewModel
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text

@Page
@Composable
fun BrowsePage(viewModel: HomeViewModel = viewModel()) {
    Column(Modifier.fillMaxSize()) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val s = state) {
            is HomeState.Content -> BrowsePage(
                content = s,
                onItemAdd = {
                    viewModel.sendAction(HomeAction.AddItem(it))
                },
                onItemRemove = {
                    viewModel.sendAction(HomeAction.RemoveItem(it))
                },
                onBasketClear = {
                    viewModel.sendAction(HomeAction.ClearBasket)
                })

            HomeState.Loading -> LoadingIndicator()
        }

    }
}

val HeaderText by ComponentStyle {
    base {
        Modifier.fontFamily("YSText", "helvetica", "arial", "sans-serif")
            .fontWeight(800)
            .fontSize(29.px)
            .color(rgb(33, 32, 31))
    }
}

val HeavyText by ComponentStyle {
    base {
        Modifier.fontFamily("YSText", "helvetica", "arial", "sans-serif")
            .fontWeight(500)
            .fontSize(20.px)
            .color(rgb(33, 32, 31))
    }
}

val RegularText by ComponentStyle {
    base {
        Modifier.fontFamily("YSText", "helvetica", "arial", "sans-serif")
            .fontWeight(400)
            .fontSize(18.px)
            .color(rgb(33, 32, 31))
    }
}

val CardBackground by ComponentStyle {
    base {
        Modifier.backgroundColor(rgb(248, 247, 245))
            .transition(
                CSSTransition(
                    property = "background-color",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseOut
                )
            )
    }
    hover {
        Modifier.backgroundColor(rgb(241, 240, 237))
    }
}

@Composable
private fun BrowsePage(
    content: HomeState.Content,
    onItemAdd: (id: String) -> Unit,
    onItemRemove: (id: String) -> Unit,
    onBasketClear: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(left = 20.px).weight(1)) {
            content.items.categories.forEach {
                Box(modifier = HeaderText.toModifier().padding(top = 19.px, bottom = 22.px)) {
                    Text(it.name)
                }
                SimpleGrid(numColumns(4)) {
                    it.items.forEach { item ->
                        Column(
                            modifier = HoverCard.toModifier(HoverCardShadow).width(214.px)
                                .margin(right = 10.px, bottom = 10.px)
                                .padding(10.px)
                                .cursor(Cursor.Default)
                        ) {
                            Image(
                                src = item.imagePath.onStatic,
                                modifier = Modifier.fillMaxWidth().height(70.percent)
                            )
                            Box(modifier = HeavyText.toModifier()) {
                                Text(item.price.toString().substringBefore(",").substringBefore(".") + " ₽")
                            }
                            Box(modifier = HeavyText.toModifier().padding(top = 10.px)) {
                                Text(item.name)
                            }
                            val amount = content.itemsInBasket[item.id]
                            if (amount != null && amount != 0) {
                                Row(
                                    modifier = HoverButton.toModifier(GrayHoverButton).margin(top = 16.px).height(44.px)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onItemRemove(item.id) },
                                        modifier = Modifier.backgroundColor(Colors.Transparent)
                                    ) {
                                        FaMinus(size = IconSize.SM)
                                    }
                                    Text(content.itemsInBasket[item.id]!!.toString())
                                    Button(
                                        onClick = { onItemAdd(item.id) },
                                        modifier = Modifier.backgroundColor(Colors.Transparent)
                                    ) {
                                        FaPlus(size = IconSize.SM)
                                    }
                                }
                            } else {
                                SimpleButton(
                                    modifier = Modifier.margin(top = 16.px).fillMaxWidth().height(44.px),
                                    onClick = { onItemAdd(item.id) }) {
                                    Text("В корзину")
                                }
                            }
                        }
                    }
                }
            }
        }

        val basket = content.items.categories.flatMap { it.items }
            .mapNotNull { item -> content.itemsInBasket[item.id]?.let { item to it } }.toMap()
        Basket(
            basket = basket,
            onItemRemove = onItemRemove,
            onItemAdd = onItemAdd,
            deliveryFee = content.items.deliveryFee,
            clearBasketButton = {
                Button(
                    onClick = {
                        onBasketClear()
                    },
                    modifier = HoverButton.toModifier(GrayHoverButton)
                ) {
                    FaTrash()
                }
            }
        )
    }
}

@Composable
private fun SimpleButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = modifier then HoverButton.toModifier(GrayHoverButton)
    ) {
        Box(modifier = RegularText.toModifier().fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}