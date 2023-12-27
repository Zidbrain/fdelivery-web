package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.components.HoverButton
import io.github.zidbrain.fdelivery.components.LoadingIndicator
import io.github.zidbrain.fdelivery.components.NavBar
import io.github.zidbrain.fdelivery.components.YellowHoverButton
import io.github.zidbrain.fdelivery.payment.viewmodel.PaymentAction
import io.github.zidbrain.fdelivery.payment.viewmodel.PaymentEvent
import io.github.zidbrain.fdelivery.payment.viewmodel.PaymentState
import io.github.zidbrain.fdelivery.payment.viewmodel.PaymentViewModel
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun Payment(viewModel: PaymentViewModel = viewModel()) {
    val ctx = rememberPageContext()
    LaunchedEffect(viewModel) {
        viewModel.events.collect {
            when (it) {
                is PaymentEvent.Success -> ctx.router.navigateTo("/order?id=${it.paymentId}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val it = state) {
            is PaymentState.Content -> Row {
                var address by remember { mutableStateOf("") }
                var apartment by remember { mutableStateOf("") }
                var entrance by remember { mutableStateOf("") }
                var floor by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxHeight().width(600.px).padding(right = 24.px)) {
                    Box(modifier = HeaderText.toModifier().fontSize(32.px).padding(top = 24.px)) {
                        Text("Оформление заказа")
                    }

                    Text("Адрес доставки", modifier = HeaderText.toModifier().fontSize(22.px).padding(top = 20.px))
                    TextInput(
                        placeholder = "Улица, номер дома",
                        text = address,
                        onTextChanged = { address = it },
                        modifier = Modifier.margin(top = 16.px).fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().margin(top = 16.px),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextInput(
                            placeholder = "Кв/офис",
                            text = apartment,
                            onTextChanged = { apartment = it },
                            valid = apartment.isBlank() || apartment.toIntOrNull() != null
                        )
                        TextInput(
                            placeholder = "Подъезд",
                            text = entrance,
                            onTextChanged = { entrance = it },
                            modifier = Modifier.margin(left = 5.px),
                            valid = entrance.isBlank() || entrance.toIntOrNull() != null
                        )
                        TextInput(
                            placeholder = "Этаж",
                            text = floor,
                            onTextChanged = { floor = it },
                            modifier = Modifier.margin(left = 5.px),
                            valid = floor.isBlank() || floor.toIntOrNull() != null
                        )
                    }
                }

                PaymentTotals(
                    totalPrice = it.itemsPrice + it.deliveryFee,
                    deliveryFee = it.deliveryFee,
                    modifier = Modifier.margin(left = 40.px, top = 40.px)
                ) {
                    Button(
                        onClick = {
                            viewModel.sendAction(
                                PaymentAction.Pay(
                                    address,
                                    apartment.toInt(),
                                    floor.toInt(),
                                    entrance.toInt()
                                )
                            )
                        },
                        modifier = HoverButton.toModifier(YellowHoverButton).fillMaxWidth(),
                        enabled = address.isNotBlank() &&
                                apartment.isNotBlank() && apartment.toIntOrNull() != null &&
                                entrance.isNotBlank() && entrance.toIntOrNull() != null &&
                                floor.isNotBlank() && floor.toIntOrNull() != null
                    ) {
                        Text("Оплатить")
                    }
                }
            }

            PaymentState.Error -> {}
            PaymentState.Loading -> LoadingIndicator()
        }
    }
}