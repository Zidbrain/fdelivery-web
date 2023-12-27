package io.github.zidbrain.fdelivery.util

import kotlin.math.roundToInt

val Double.roundedMoney: String
    get() = this.roundToInt().toString() + " ₽"