package com.example.siscomedor.ui.tickets

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.graphics.createBitmap
import com.example.siscomedor.domain.DemoQr
import com.example.siscomedor.domain.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrExport {
    suspend fun write(resolver: ContentResolver, uri: Uri, ticket: Ticket) = withContext(Dispatchers.IO) {
        require(ticket.canShowQr) { "Read-only tickets cannot export a usable QR" }
        val cell = 16
        val padding = 4 * cell
        val dimension = (DemoQr.size + 8) * cell
        val bitmap = createBitmap(dimension, dimension + 48)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val paint = Paint().apply { color = Color.rgb(16, 47, 39) }
            for (y in 0 until DemoQr.size) for (x in 0 until DemoQr.size) {
                if (DemoQr.cell(ticket.qrContent, x, y)) canvas.drawRect(
                    (padding + x * cell).toFloat(), (padding + y * cell).toFloat(),
                    (padding + (x + 1) * cell).toFloat(), (padding + (y + 1) * cell).toFloat(), paint)
            }
            paint.textSize = 16f
            paint.isAntiAlias = true
            canvas.drawText(ticket.id, 20f, dimension.toFloat() + 16, paint)
            canvas.drawText("FICTICIO - SIN VALIDEZ INSTITUCIONAL", 20f, dimension.toFloat() + 38, paint)
            val stream = resolver.openOutputStream(uri, "w") ?: error("Cannot open the chosen document")
            stream.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally {
            bitmap.recycle()
        }
    }
}
