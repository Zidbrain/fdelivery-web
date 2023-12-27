package io.github.zidbrain.fdelivery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.navigation.RoutePrefix
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.forms.InputStyle
import com.varabyte.kobweb.silk.components.forms.InputVars
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.components.style.ariaDisabled
import com.varabyte.kobweb.silk.components.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.components.style.hover
import com.varabyte.kobweb.silk.components.style.not
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.style.vars.color.ColorVar
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.replaceComponentStyleBase
import io.github.zidbrain.fdelivery.admin.viewmodel.AdminViewModel
import io.github.zidbrain.fdelivery.auth.viewmodel.AuthViewModel
import io.github.zidbrain.fdelivery.cart.CartViewModel
import io.github.zidbrain.fdelivery.delivery.viewmodel.DeliveryViewModel
import io.github.zidbrain.fdelivery.home.viewmodel.HomeViewModel
import io.github.zidbrain.fdelivery.order.viewmodel.OrderViewModel
import io.github.zidbrain.fdelivery.payment.viewmodel.PaymentViewModel
import io.github.zidbrain.fdelivery.profile.viewmodel.ProfileViewModel
import io.github.zidbrain.fdelivery.viewmodel.register
import org.jetbrains.compose.web.css.*

@App
@Composable
fun MyApp(content: @Composable () -> Unit) {
    remember {
        println(RoutePrefix.value)

        register { HomeViewModel() }
        register { AuthViewModel() }
        register { CartViewModel() }
        register { PaymentViewModel() }
        register {
            OrderViewModel(it.route.params["id"]!!)
        }
        register { ProfileViewModel() }
        register { DeliveryViewModel() }
        register { AdminViewModel()}
    }

    SilkApp {
        Surface(SmoothColorStyle.toModifier().minHeight(100.vh).backgroundColor(rgb(245, 244, 242))) {
            content()
        }
    }
}

@InitSilk
fun overrideSilkTheme(ctx: InitSilkContext) {
    ctx.theme.palettes.light.background = Colors.Transparent
    ctx.theme.replaceComponentStyle(ButtonStyle) {
        base {
            Modifier
                .color(ButtonVars.Color.value())
                .backgroundColor(Colors.Transparent)
                .lineHeight(1.2)
                .height(ButtonVars.Height.value())
                .minWidth(ButtonVars.Height.value())
                .whiteSpace(WhiteSpace.NoWrap)
                .padding(leftRight = ButtonVars.PaddingHorizontal.value())
                .verticalAlign(VerticalAlign.Middle)
                .borderRadius(0.375.cssRem)
                .border { width(0.px) }
                .userSelect(UserSelect.None)
        }
        (hover + not(ariaDisabled)) {
            Modifier
                .cursor(Cursor.Pointer)
        }
    }
    ctx.theme.replaceComponentStyleBase(InputStyle) {
        Modifier
            .styleModifier { property("appearance", "none") } // Disable browser styles
            .color(ColorVar.value())
            .height(InputVars.Height.value())
            .fontSize(InputVars.FontSize.value())
            .backgroundColor(Colors.White)
            .outline(0.px, LineStyle.Solid, Colors.Transparent) // Disable, we'll use box shadow instead
            .border(0.px, LineStyle.Solid, Colors.Transparent) // Overridden by variants
            .transition(
                CSSTransition.group(
                    listOf("border-color", "box-shadow", "background-color"),
                    duration = InputVars.ColorTransitionDuration.value()
                )
            )
    }
}
