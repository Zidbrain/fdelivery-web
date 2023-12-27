package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaArrowsToDot
import com.varabyte.kobweb.silk.components.icons.fa.FaCartPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaCheck
import com.varabyte.kobweb.silk.components.icons.fa.FaPersonWalking
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.order.viewmodel.OrderState
import io.github.zidbrain.fdelivery.order.viewmodel.OrderViewModel
import io.github.zidbrain.fdelivery.payment.model.OrderStatus
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.util.roundedMoney
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

val GrayText by ComponentStyle {
    base {
        Modifier.color(Colors.Gray)
    }
}

@Page
@Composable
fun Order(viewModel: OrderViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val it = state) {
            is OrderState.Content -> {
                Column(modifier = RegularText.toModifier().width(450.px).margin(top = 40.px)) {
                    val address = it.order.deliveryAddress
                    Text("Адрес", modifier = GrayText.toModifier().margin(bottom = 10.px))
                    Text("${address.destination}, подъезд ${address.entrance}, этаж ${address.floor}, кв. ${address.apartment}")

                    Text("Создан", modifier = GrayText.toModifier().margin(top = 10.px, bottom = 10.px))
                    Text(it.order.createdAt.toLocaleString("ru-ru"))

                    Text("Статус", modifier = GrayText.toModifier().margin(top = 10.px, bottom = 10.px))
                    Row(modifier = Modifier.margin(top = 10.px)) {
                        val entries = remember { OrderStatus.entries.filter { it != OrderStatus.Canceled } }
                        entries.forEach {status ->
                            Box(
                                modifier = HoverButton.toModifier(
                                    when {
                                        status == it.order.status -> YellowHoverButton
                                        status.ordinal < it.order.status.ordinal -> GrayHoverButton
                                        else -> null
                                    }
                                )
                                    .borderRadius(50.percent).size(48.px).margin(right = 32.px).cursor(
                                        Cursor.Default
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (status) {
                                    OrderStatus.Created -> FaCartPlus()
                                    OrderStatus.Assembling -> FaArrowsToDot()
                                    OrderStatus.Delivering -> FaPersonWalking()
                                    OrderStatus.Delivered -> FaCheck()
                                    else -> {}
                                }
                            }
                        }
                    }
                    Text(it.order.status.localizedString, modifier = Modifier.margin(top = 12.px).fontSize(24.px))

                    it.order.assignedTo?.let { deliveryMan ->
                        Text("Доставляет", modifier = GrayText.toModifier().margin(top = 10.px, bottom = 10.px))
                        Text("${deliveryMan.name} ${deliveryMan.lastName}")
                    }

                    HorizontalDivider(modifier = Modifier.margin(top = 26.px))

                    Text("Состав заказа", modifier = GrayText.toModifier().margin(top = 26.px))
                    it.order.order.forEach { (item, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().margin(top = 10.px),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                src = item?.let { it.imagePath.onStatic } ?: DEFAULT_IMAGE_URL,
                                modifier = Modifier.size(64.px)
                            )
                            Column(modifier = Modifier.fillMaxHeight().margin(left = 16.px)) {
                                Text(item?.name ?: "Товар был удален")
                                Spacer()
                                Text("$amount шт")
                            }
                            Spacer()
                            Text(item?.let { "$amount x ${it.price!!.roundedMoney}" } ?: "amount x ???")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.margin(top = 26.px, bottom = 26.px))
                    Text("Оплата", modifier = GrayText.toModifier().margin(bottom = 10.px))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Стоимость товаров")
                        Spacer()
                        Text((it.order.totalCost - it.order.deliveryFee).roundedMoney)
                    }
                    Row(modifier = Modifier.fillMaxWidth().margin(top = 10.px)) {
                        Text("Стоимость доставки")
                        Spacer()
                        Text(it.order.deliveryFee.roundedMoney)
                    }
                    Row(modifier = Modifier.fillMaxWidth().fontWeight(FontWeight.Bolder).margin(top = 26.px)) {
                        Text("Итого")
                        Spacer()
                        Text(it.order.totalCost.roundedMoney)
                    }
                }
            }

            OrderState.Error -> {}
            OrderState.Loading -> LoadingIndicator()
        }
    }
}

val OrderStatus.localizedString: String
    get() = when (this) {
            OrderStatus.Assembling -> "Собирается"
            OrderStatus.Created -> "Создан"
            OrderStatus.Delivering -> "В доставке"
            OrderStatus.Delivered -> "Доставлен"
            OrderStatus.Canceled -> "Отменен"
        }