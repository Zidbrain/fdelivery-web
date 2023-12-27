package io.github.zidbrain.fdelivery.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.background
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.toModifier
import io.github.zidbrain.fdelivery.auth.model.Role
import io.github.zidbrain.fdelivery.client.ApiClient
import io.github.zidbrain.fdelivery.home.viewmodel.BasketHolder
import io.github.zidbrain.fdelivery.home.viewmodel.HomeViewModel
import io.github.zidbrain.fdelivery.viewmodel.ViewModelHolder
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Text

val NavbarBrandText by ComponentStyle {
    base {
        Modifier.fontFamily("Helvetica Neue", "Helvetica", "Arial")
            .fontSize(18.px)
            .color(rgb(157, 157, 157))
            .background(Colors.Transparent)
    }
    hover {
        Modifier.color(Color.white).backgroundColor(Colors.Transparent)
    }
}

@Composable
fun NavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.background(rgb(51, 51, 51)).fillMaxWidth().height(50.px)
            .padding(left = 20.px, right = 20.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ctx = rememberPageContext()
        Button(modifier = NavbarBrandText.toModifier(), onClick = { ctx.router.navigateTo("/") }) {
            Text("F-Delivery")
        }

        val creds by ApiClient.credentialsFlow.collectAsState()
        Spacer()

        creds?.let {
            when (it.role) {
                Role.User -> {}
                Role.DeliveryMan -> Button(
                    modifier = NavbarBrandText.toModifier(),
                    onClick = { ctx.router.navigateTo("delivery") }
                ) {
                    Text("Панель курьера")
                }

                Role.Admin -> Button(
                    modifier = NavbarBrandText.toModifier(),
                    onClick = { ctx.router.navigateTo("adminpanel") }
                ) {
                    Text("Панель администратора")
                }
            }
            Button(modifier = NavbarBrandText.toModifier(), onClick = { ctx.router.navigateTo("userprofile") }) {
                Text("${it.name} ${it.lastName}")
            }
            Button(modifier = NavbarBrandText.toModifier(), onClick = {
                ApiClient.clear()
                ViewModelHolder.remove<HomeViewModel>()
                BasketHolder.putIntoSessionStorage(emptyMap())
                if (ctx.route.path == "/")
                    window.location.reload()
                else
                    ctx.router.navigateTo("/")
            }) {
                Text("Выход")
            }
        } ?: run {
            Button(modifier = NavbarBrandText.toModifier(), onClick = { ctx.router.navigateTo("auth") }) {
                Text("Войти")
            }
            Button(modifier = NavbarBrandText.toModifier(), onClick = { ctx.router.navigateTo("register") }) {
                Text("Регистрация")
            }
        }
    }
}