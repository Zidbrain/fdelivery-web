package io.github.zidbrain.fdelivery.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaMinus
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.home.model.ItemDto
import io.github.zidbrain.fdelivery.pages.HeaderText
import io.github.zidbrain.fdelivery.pages.HeavyText
import io.github.zidbrain.fdelivery.pages.RegularText
import io.github.zidbrain.fdelivery.util.roundedMoney
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text

@Composable
fun Basket(
    modifier: Modifier = Modifier,
    basket: Map<ItemDto, Int>,
    deliveryFee: Double,
    onItemAdd: (String) -> Unit,
    onItemRemove: (String) -> Unit,
    buyButton: @Composable () -> Unit = { BasketBuyButton(basket, deliveryFee) },
    clearBasketButton: @Composable () -> Unit = { }
) {
    Column(modifier = modifier.fillMaxHeight().width(500.px).padding(leftRight = 24.px)) {
        Row(
            modifier = HeaderText.toModifier().fontSize(24.px).padding(top = 24.px).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Корзина")
            clearBasketButton()
        }
        basket.forEach { (item, amount) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    src = item.imagePath.onStatic,
                    modifier = Modifier.size(60.px)
                )
                Column(modifier = Modifier.weight(1).padding(left = 16.px)) {
                    Box(modifier = RegularText.toModifier()) {
                        Text(item.name)
                    }
                    Box(modifier = HeavyText.toModifier().padding(top = 5.px)) {
                        Text(item.price.toString().substringBefore(",").substringBefore(".") + " ₽")
                    }
                }
                Row(
                    modifier = HoverButton.toModifier().backgroundColor(Colors.LightGray).height(44.px)
                        .width(100.px) then RegularText.toModifier(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onItemRemove(item.id) },
                        modifier = Modifier.size(44.px).backgroundColor(Colors.LightGray)
                    ) {
                        FaMinus(size = IconSize.XS)
                    }
                    Text(amount.toString())
                    Button(
                        onClick = { onItemAdd(item.id) },
                        modifier = Modifier.size(44.px).backgroundColor(Colors.LightGray)
                    ) {
                        FaPlus(size = IconSize.XS)
                    }
                }
            }
        }
        Spacer()

        buyButton()
    }
}

@Composable
fun BasketBuyButton(
    basket: Map<ItemDto, Int>,
    deliveryFee: Double
) {
    Row(
        modifier = RegularText.toModifier().padding(16.px).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Цена за доставку")
        Spacer()
        Text(deliveryFee.roundedMoney)
    }

    val ctx = rememberPageContext()
    Button(
        onClick = {
            ctx.router.navigateTo("cart")
        },
        modifier = HoverButton.toModifier().backgroundColor(
            if (basket.isEmpty()) Colors.LightGray
            else Colors.White
        ).fillMaxWidth().height(54.px).margin(bottom = 20.px),
        enabled = basket.isNotEmpty()
    ) {
        if (basket.isEmpty()) Text("В корзине пока пусто")
        else {
            Text("Перейти в корзину")
            Spacer()
            Text((deliveryFee + basket.entries.sumOf { (item, amount) -> item.price!! * amount }).toString() + " ₽")
        }
    }
}