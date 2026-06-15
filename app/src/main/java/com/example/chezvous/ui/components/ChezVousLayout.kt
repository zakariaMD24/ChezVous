package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.example.chezvous.ui.theme.ChezVousSpacing

fun Modifier.chezVousScreenPadding(): Modifier {
    return padding(horizontal = ChezVousSpacing.screenHorizontal)
}

fun Modifier.chezVousSheetPadding(): Modifier {
    return padding(horizontal = ChezVousSpacing.sheetHorizontal)
}
