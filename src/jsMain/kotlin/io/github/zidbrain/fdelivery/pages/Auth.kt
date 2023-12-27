package io.github.zidbrain.fdelivery.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.auth.model.Role
import io.github.zidbrain.fdelivery.auth.viewmodel.AuthAction
import io.github.zidbrain.fdelivery.auth.viewmodel.AuthEvent
import io.github.zidbrain.fdelivery.auth.viewmodel.AuthState
import io.github.zidbrain.fdelivery.auth.viewmodel.AuthViewModel
import io.github.zidbrain.fdelivery.components.GrayHoverButton
import io.github.zidbrain.fdelivery.components.HoverButton
import io.github.zidbrain.fdelivery.components.LoadingIndicator
import io.github.zidbrain.fdelivery.components.YellowHoverButton
import io.github.zidbrain.fdelivery.viewmodel.viewModel
import kotlinx.browser.window
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text

@Page
@Composable
fun Auth(viewModel: AuthViewModel = viewModel()) = Box(modifier = Modifier.fillMaxSize()) {
    val ctx = rememberPageContext()
    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                is AuthEvent.Success -> {
                    if (ctx.route.params["after"] != null)
                        ctx.router.navigateTo(ctx.route.params["after"]!!)
                    else ctx.router.navigateTo(
                        when (it.response.role) {
                            Role.User -> "/"
                            Role.DeliveryMan -> "delivery"
                            Role.Admin -> "adminpanel"
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = RegularText.toModifier()
            .border(width = 1.px, style = LineStyle.Solid, color = Colors.LightGray)
            .borderRadius(10.px)
            .align(Alignment.TopCenter)
            .margin(top = 5.percent)
            .padding(10.px)
            .width(350.px)
            .backgroundColor(Colors.White)
    ) {
        val state by viewModel.state.collectAsState()

        var login by remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.px)) {
            Text("Имя пользователя")

            TextInput(text = login, onTextChanged = { login = it }, modifier = Modifier.fillMaxWidth())
        }

        var password by remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.px)) {
            Text("Пароль")
            TextInput(
                text = password,
                onTextChanged = { password = it },
                password = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.height(50.px).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (state) {
                AuthState.Content -> {}
                AuthState.Error -> Box(modifier = Modifier.color(Colors.Red)) { Text("Ошибка при попытке входа") }
                AuthState.Loading -> LoadingIndicator()
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.px).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = HoverButton.toModifier(GrayHoverButton).fillMaxWidth().margin(leftRight = 10.px),
                onClick = {
                    window.history.back()
                }) {
                Text("Назад")
            }
            Button(modifier = HoverButton.toModifier(YellowHoverButton).fillMaxWidth().margin(leftRight = 10.px),
                onClick = {
                    viewModel.sendAction(
                        AuthAction.Login(
                            login = login,
                            password = password
                        )
                    )
                }) {
                Text("Войти")
            }
        }
    }
}