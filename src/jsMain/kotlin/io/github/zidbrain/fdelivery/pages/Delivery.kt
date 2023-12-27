package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Visibility
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.delivery.viewmodel.DeliveryAction
import io.github.zidbrain.fdelivery.delivery.viewmodel.DeliveryState
import io.github.zidbrain.fdelivery.delivery.viewmodel.DeliveryViewModel
import io.github.zidbrain.fdelivery.payment.model.OrderStatus
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.util.roundedMoney
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun Delivery(viewModel: DeliveryViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val it = state) {
            is DeliveryState.Content -> DeliveryContent(
                content = it,
                onStatusSwitch = {
                    viewModel.sendAction(DeliveryAction.SwitchStatus(it))
                },
                onSaveChanges = {
                    viewModel.sendAction(DeliveryAction.SaveChanges)
                },
                escalateStatus = {
                    viewModel.sendAction(DeliveryAction.EscalateStatus)
                }
            )

            DeliveryState.Error -> {}
            DeliveryState.Loading -> LoadingIndicator()
        }
    }
}

const val DEFAULT_IMAGE_URL = "/static/image-regular.svg"

@Composable
private fun DeliveryContent(
    content: DeliveryState.Content,
    onStatusSwitch: (Boolean) -> Unit,
    onSaveChanges: () -> Unit,
    escalateStatus: () -> Unit
) = Column(modifier = Modifier.margin(top = 64.px)) {
    Text("Настройки", modifier = HeaderText.toModifier())
    Row(modifier = HeavyText.toModifier().width(350.px).margin(top = 16.px)) {
        Text("Готовность собирать заказы")
        Spacer()
        Switch(
            checked = content.isReady,
            onCheckedChange = onStatusSwitch,
            enabled = content.canChangeStatus
        )
    }
    Button(
        onClick = { onSaveChanges() },
        modifier = HoverButton.toModifier(YellowHoverButton).height(44.px).margin(top = 16.px),
        enabled = content.canChangeStatus
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Сохранить изменения",
                modifier = Modifier.visibility(if (content.changesLoading) Visibility.Hidden else Visibility.Visible)
            )
            LoadingIndicator(modifier = Modifier.visibility(if (!content.changesLoading) Visibility.Hidden else Visibility.Visible))
        }
    }

    if (content.assignedOrder == null)
        Text(
            "На вас пока не назначен заказ",
            modifier = HeaderText.toModifier().margin(top = 32.px)
        )
    else Column(modifier = RegularText.toModifier().margin(top = 32.px).minWidth(450.px)) {
        with(content.assignedOrder) {
            Text("Вам назначен заказ", modifier = HeaderText.toModifier())

            Text("Адрес", modifier = GrayText.toModifier().margin(top = 16.px, bottom = 10.px))
            Text("${deliveryAddress.destination}, подъезд ${deliveryAddress.entrance}, этаж ${deliveryAddress.floor}, кв. ${deliveryAddress.apartment}")

            HorizontalDivider(modifier = Modifier.margin(top = 26.px))
            Text("Состав заказа", modifier = GrayText.toModifier().margin(top = 26.px))
            order.forEach { (item, amount) ->
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

            HorizontalDivider(modifier = Modifier.margin(top = 26.px))
            Text("Статус заказа", modifier = GrayText.toModifier().margin(top = 26.px))
            Row(modifier = Modifier.margin(top = 16.px)) {
                val entries = remember { OrderStatus.entries.filter { it != OrderStatus.Canceled } }
                entries.forEach {
                    Box(
                        modifier = HoverButton.toModifier(
                            when {
                                it == status -> YellowHoverButton
                                it.ordinal < status.ordinal -> GrayHoverButton
                                else -> null
                            }
                        )
                            .borderRadius(50.percent).size(48.px).margin(right = 32.px).cursor(
                                Cursor.Default
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (it) {
                            OrderStatus.Created -> FaCartPlus()
                            OrderStatus.Assembling -> FaArrowsToDot()
                            OrderStatus.Delivering -> FaPersonWalking()
                            OrderStatus.Delivered -> FaCheck()
                            else -> {}
                        }
                    }
                }

                if (!status.isComplete) {
                    Spacer()
                    Button(
                        onClick = { escalateStatus() },
                        modifier = HoverButton.toModifier(YellowHoverButton).margin(left = 20.px),
                        enabled = !content.orderStatusLoading
                    ) {
                        Box {
                            if (content.orderStatusLoading)
                                LoadingIndicator()

                            Row(modifier = Modifier.visibility(if (content.orderStatusLoading) Visibility.Hidden else Visibility.Visible)) {
                                Text(
                                    when (status) {
                                        OrderStatus.Created -> "Начать сборку"
                                        OrderStatus.Assembling -> "Взять в доставку"
                                        OrderStatus.Delivering -> "Заказ доставлен"
                                        OrderStatus.Delivered -> ""
                                        else -> ""
                                    }
                                )
                                FaArrowRight(modifier = Modifier.margin(left = 10.px))
                            }
                        }
                    }
                }
            }
            Text(status.localizedString, modifier = Modifier.margin(top = 20.px).fontSize(24.px))
        }
    }

}