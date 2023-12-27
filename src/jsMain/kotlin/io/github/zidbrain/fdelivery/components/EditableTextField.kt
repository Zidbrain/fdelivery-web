package io.github.zidbrain.fdelivery.components

import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.focus
import com.varabyte.kobweb.silk.components.style.hover
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.s

val EditableTextFieldStyle by ComponentStyle {
    base {
        Modifier.fontFamily("YSText", "helvetica", "arial", "sans-serif")
            .fontWeight(500)
            .fontSize(20.px)
            .color(rgb(33, 32, 31))
            .border { color(Colors.Transparent) }
            .backgroundColor(Colors.Transparent)
            .borderRadius(8.px)
            .transition(
                CSSTransition(
                    property = "border-color",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseInOut
                ),
                CSSTransition(
                    property = "background-color",
                    duration = 0.15.s,
                    timingFunction = AnimationTimingFunction.EaseInOut
                )
            )
    }
    (hover + focus) {
        Modifier.border { color(Colors.Black) }.backgroundColor(rgb(230, 230, 230))
    }
}