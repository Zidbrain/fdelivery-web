package io.github.zidbrain.fdelivery.util

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier

@Composable
fun Text(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
       org.jetbrains.compose.web.dom.Text(text)
    }
}