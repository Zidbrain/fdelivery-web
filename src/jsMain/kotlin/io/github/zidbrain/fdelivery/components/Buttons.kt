package io.github.zidbrain.fdelivery.components

import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.hover
import org.jetbrains.compose.web.css.*

val HoverButton by ComponentStyle {
    base {
        Modifier.fontFamily("YSText", "helvetica", "arial", "sans-serif")
            .fontWeight(400)
            .fontSize(18.px)
            .color(rgb(33, 32, 31))
            .height(54.px)
            .backgroundColor(Colors.White)
            .borderRadius(16.px)
            .boxShadow(offsetY = 3.px, blurRadius = 4.px, color = rgba(0, 0, 0, 0.06))
            .transition(
                CSSTransition(
                    property = "box-shadow",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseInOut
                )
            )
    }

    hover {
        Modifier.backgroundColor(Colors.White).boxShadow(offsetY = 3.px, blurRadius = 4.px, color = rgba(0, 0, 0, 0.12))
    }
}

val GrayHoverButton by HoverButton.addVariant {
    base {
        Modifier.backgroundColor(rgb(230, 230, 230))
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
                )
            )
    }
    hover {
        Modifier.backgroundColor(rgb(210, 210, 210))
    }
}

val YellowHoverButton by HoverButton.addVariant {
    base {
        Modifier.backgroundColor(rgb(252, 224, 0))
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
                )
            )
    }

    hover {
        Modifier.backgroundColor(rgb(242, 215, 0))
    }
}