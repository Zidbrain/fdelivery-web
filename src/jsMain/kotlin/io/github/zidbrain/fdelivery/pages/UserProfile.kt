package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.CSSTransition
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
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.addVariantBase
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.client.onStatic
import io.github.zidbrain.fdelivery.components.*
import io.github.zidbrain.fdelivery.payment.model.OrderDto
import io.github.zidbrain.fdelivery.payment.model.OrderStatus
import io.github.zidbrain.fdelivery.profile.viewmodel.ProfileAction
import io.github.zidbrain.fdelivery.profile.viewmodel.ProfileState
import io.github.zidbrain.fdelivery.profile.viewmodel.ProfileViewModel
import io.github.zidbrain.fdelivery.util.Text
import io.github.zidbrain.fdelivery.util.roundedMoney
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import org.jetbrains.compose.web.css.*

@Page
@Composable
fun UserProfile(viewModel: ProfileViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        NavBar()

        val state by viewModel.state.collectAsState()
        when (val it = state) {
            is ProfileState.Content -> UserProfileContent(it, saveSettingsChanges = { name, lastName, password ->
                viewModel.sendAction(ProfileAction.SaveChanges(name, lastName, password))
            })

            ProfileState.Error -> {}
            ProfileState.Loading -> LoadingIndicator()
        }
    }
}

@Composable
private fun UserProfileContent(content: ProfileState.Content, saveSettingsChanges: (String, String, String?) -> Unit) =
    Column(modifier = Modifier.margin(top = 64.px).width(600.px)) {
        Column(modifier = Modifier.width(400.px)) {
            Text("Данные", modifier = HeaderText.toModifier().margin(bottom = 24.px))
            var name by remember(content) { mutableStateOf(content.name) }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Имя", modifier = HeavyText.toModifier())
                Spacer()
                TextInput(text = name, onTextChanged = { name = it }, modifier = Modifier.width(250.px))
            }

            var lastName by remember(content) { mutableStateOf(content.lastName) }
            Row(
                modifier = Modifier.fillMaxWidth().margin(top = 12.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Фамилия", modifier = HeavyText.toModifier())
                Spacer()
                TextInput(
                    text = lastName,
                    onTextChanged = { lastName = it },
                    modifier = Modifier.width(250.px)
                )
            }

            var changePassword by remember { mutableStateOf(false) }
            var password by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().margin(top = 12.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Пароль", modifier = HeavyText.toModifier())
                Spacer()
                if (!changePassword)
                    Button(
                        onClick = { changePassword = true }, modifier = HoverButton.toModifier(GrayHoverButton)
                    ) {
                        Text("Изменить пароль")
                    }
                else {
                    Button(
                        onClick = {
                            changePassword = false
                            password = ""
                        }, modifier = HoverButton.toModifier(GrayHoverButton)
                    ) {
                        Text("Отмена")
                    }
                    TextInput(
                        text = password,
                        onTextChanged = { password = it },
                        modifier = Modifier.weight(1).margin(left = 4.px),
                        password = true
                    )
                }
            }

            Button(
                onClick = {
                    saveSettingsChanges(
                        name,
                        lastName,
                        password.takeIf { changePassword && password.isNotBlank() })
                    changePassword = false
                    password = ""
                },
                modifier = HoverButton.toModifier(YellowHoverButton).margin(top = 24.px)
            ) {
                if (content.changesSaving)
                    LoadingIndicator()
                Text("Cохранить изменения", modifier = Modifier.visible(!content.changesSaving))
            }
        }

        Column(modifier = Modifier.margin(top = 24.px).fillMaxWidth()) {
            Text("История заказов", modifier = HeaderText.toModifier().margin(bottom = 24.px))

            val ctx = rememberPageContext()
            content.orders.forEach { order ->
                OrderCard(modifier = Modifier.fillMaxWidth().margin(bottom = 16.px), order = order, onClick = {
                    ctx.router.navigateTo("/order?id=${order.id}")
                })
            }
        }
    }

val HoverCard by ComponentStyle {
    base {
        Modifier.backgroundColor(rgb(250, 250, 250))
            .borderRadius(16.px)
            .padding(16.px)
            .cursor(Cursor.Pointer)
            .transition(
                CSSTransition(
                    property = "background-color",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseInOut
                ),
                CSSTransition(
                    property = "box-shadow",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseInOut
                ),
            )
    }
    hover {
        Modifier.backgroundColor(Colors.White)
            .boxShadow(offsetY = 6.px, blurRadius = 8.px, color = rgba(0, 0, 0, 0.06))
    }
}

val HoverCardShadow by HoverCard.addVariantBase {
    Modifier.boxShadow(offsetY = 6.px, blurRadius = 8.px, color = rgba(0, 0, 0, 0.03))
}

@Composable
private fun OrderCard(order: OrderDto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier then HoverCard.toModifier().onClick { onClick() }) {
        Row(modifier = HeavyText.toModifier().fontWeight(FontWeight.Bolder).fillMaxWidth()) {
            Text(order.createdAt.toLocaleString())
            Spacer()
            Text(order.totalCost.roundedMoney)
        }
        Row(modifier = RegularText.toModifier().margin(top = 6.px)) {
            Spacer()
            val colorModifier = remember(order.status) {
                if (order.status == OrderStatus.Delivered) Modifier.color(Colors.Green)
                else Modifier
            }
            Text(order.status.localizedString, modifier = colorModifier)
        }
        Row(modifier = Modifier.margin(top = 10.px)) {
            order.order.take(5).forEach {
                Image(
                    src = it.item?.let { it.imagePath.onStatic } ?: DEFAULT_IMAGE_URL,
                    modifier = Modifier.size(24.px).margin(right = 8.px)
                )
            }
            if (order.order.size > 5) Box(modifier = RegularText.toModifier().fontSize(11.em).borderRadius(4.px)) {
                Text("+${order.order.size - 5}")
            }
        }
    }
}