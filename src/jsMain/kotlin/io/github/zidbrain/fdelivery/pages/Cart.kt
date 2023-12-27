package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.cart.CartAction
import io.github.zidbrain.fdelivery.cart.CartState
import io.github.zidbrain.fdelivery.cart.CartViewModel
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.util.roundedMoney
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun Cart(viewModel: CartViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val it = state) {
            is CartState.Content -> {
                Row {
                    Basket(modifier = Modifier.width(600.px),
                        basket = it.cart,
                        deliveryFee = it.deliveryFee,
                        onItemAdd = {
                            viewModel.sendAction(CartAction.AddItem(it))
                        },
                        onItemRemove = {
                            viewModel.sendAction(CartAction.RemoveItem(it))
                        },
                        buyButton = {}
                    )

                    val ctx = rememberPageContext()
                    PaymentTotals(
                        totalPrice = it.totalPrice,
                        deliveryFee = it.deliveryFee,
                        modifier = Modifier.margin(left = 40.px, top = 40.px)
                    ) {
                        Button(
                            onClick = {
                                paymentNavigation(ctx)
                            },
                            modifier = HoverButton.toModifier(YellowHoverButton).fillMaxWidth()
                        ) {
                            Text("Оплатить")
                            Spacer()
                            Text(it.totalPrice.roundedMoney)
                        }
                    }
                }
            }

            CartState.Error -> {}
            CartState.Loading -> LoadingIndicator()
        }
    }
}

@Composable
fun PaymentTotals(
    totalPrice: Double,
    deliveryFee: Double,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit
) {
    Column(modifier = modifier then RegularText.toModifier().width(300.px)) {
        Text("Итого", modifier = HeaderText.toModifier())
        HorizontalDivider(modifier = Modifier.margin(top = 16.px))
        Row(modifier = Modifier.fillMaxWidth().margin(top = 16.px)) {
            Text("Товары")
            Spacer()
            Text((totalPrice - deliveryFee).roundedMoney)
        }
        Row(modifier = Modifier.fillMaxWidth().margin(top = 16.px)) {
            Text("Доставка")
            Spacer()
            Text(deliveryFee.roundedMoney)
        }
        HorizontalDivider(modifier = Modifier.margin(top = 16.px))

        Row(modifier = Modifier.fillMaxWidth().margin(top = 16.px).fontWeight(FontWeight.Bolder)) {
            Text("К оплате")
            Spacer()
            Text(totalPrice.roundedMoney)
        }

        Box(modifier = Modifier.fillMaxWidth().margin(bottom = 20.px, top = 20.px)) {
            button()
        }
    }
}