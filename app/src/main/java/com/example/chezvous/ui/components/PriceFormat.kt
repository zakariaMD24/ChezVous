package com.example.chezvous.ui.components

import java.util.Locale
import kotlin.math.round

fun Double.asDhPrice(): String {
    val rounded = round(this * 100) / 100
    return if (rounded % 1.0 == 0.0) {
        "${rounded.toInt()} DH"
    } else {
        String.format(Locale.US, "%.2f DH", rounded)
    }
}
