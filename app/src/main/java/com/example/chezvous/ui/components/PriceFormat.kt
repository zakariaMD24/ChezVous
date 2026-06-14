package com.example.chezvous.ui.components

fun Double.asDhPrice(): String {
    return if (this % 1.0 == 0.0) {
        "${toInt()} DH"
    } else {
        "$this DH"
    }
}
