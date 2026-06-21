package com.example.chezvous.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chezvous.ui.theme.ChezVousSpacing
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun PickupCodeCard(
    orderId: String,
    pickupCode: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val payload = remember(orderId, pickupCode) {
        "chezvous://orders/$orderId/pickup/$pickupCode"
    }
    val bitmap = remember(payload) { createQrBitmap(payload) }

    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = pickupCode,
                modifier = Modifier.size(168.dp)
            )

            Text(
                text = pickupCode,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun createQrBitmap(payload: String, size: Int = 512): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val matrix = runCatching {
        QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
    }.getOrNull()

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (matrix?.get(x, y) == true) Color.BLACK else Color.WHITE
            )
        }
    }

    return bitmap
}
