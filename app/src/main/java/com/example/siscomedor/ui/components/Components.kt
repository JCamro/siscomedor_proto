package com.example.siscomedor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun price(cents: Int): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-PE")).apply {
    currency = Currency.getInstance("PEN")
}.format(cents / 100.0)

fun dateLabel(date: String): String {
    val month = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    return "${date.substring(8).toInt()} ${month[date.substring(5, 7).toInt() - 1]} ${date.substring(0, 4)}"
}

private val iconPaths = mapOf(
    "home" to "M3,11 L12,3 L21,11 M5,9 L5,21 L19,21 L19,9 M9,21 L9,14 L15,14 L15,21",
    "ticket" to "M3,5 L21,5 L21,9 C17,9 17,13 21,13 L21,18 L3,18 L3,13 C7,13 7,9 3,9 Z M13,8 L17,8 M13,12 L17,12 M7,9 L9,9 L9,13 L7,13 Z",
    "card" to "M3,5 L21,5 L21,19 L3,19 Z M3,10 L21,10 M6,15 L10,15",
    "wallet" to "M3,7 L20,7 L20,20 L3,20 L3,5 L17,3 L17,7 M20,12 L15,12 L15,16 L20,16",
    "user" to "M8,8 C8,3 16,3 16,8 C16,13 8,13 8,8 M4,21 C4,12 20,12 20,21",
    "utensils" to "M4,3 L4,8 C4,12 10,12 10,8 L10,3 M7,3 L7,21 M16,21 L16,3 C20,5 20,9 20,11 L16,11",
    "clock" to "M12,3 C24,3 24,21 12,21 C0,21 0,3 12,3 M12,7 L12,12 L16,14",
    "info" to "M12,3 C24,3 24,21 12,21 C0,21 0,3 12,3 M12,11 L12,17 M12,7 L12,8",
    "check" to "M5,12 L9,16 L19,6",
    "close" to "M6,6 L18,18 M18,6 L6,18",
    "download" to "M12,3 L12,15 M7,10 L12,15 L17,10 M3,16 L3,21 L21,21 L21,16",
    "arrow" to "M5,12 L19,12 M14,7 L19,12 L14,17"
)

@Composable
fun AppIcon(name: String, description: String? = null, tint: Color = LocalContentColor.current) {
    val vector = remember(name) {
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(PathParser().parsePathString(iconPaths.getValue(name)).toNodes(),
                stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f)
        }.build()
    }
    Icon(vector, description, tint = tint)
}

@Composable
fun PageTitle(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Notice(title: String, message: String, pending: Boolean = false) {
    Surface(color = if (pending) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppIcon(if (pending) "clock" else "info")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DemoNotice() = Notice("Entorno de demostración", "Identidad, menús, billetera, pagos y tickets ficticios. No se mueve dinero real. El estado se pierde al cerrar el proceso.")

@Composable
fun WalletSummary() {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppIcon("wallet")
        Column {
            Text("Billetera ficticia preferida", style = MaterialTheme.typography.labelMedium)
            Text("Wallet Demo ···· 2841", style = MaterialTheme.typography.titleSmall)
            Text("Sin vinculación ni autorización real", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AppDialog(title: String, onClose: () -> Unit, dismissible: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(
        dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible, usePlatformDefaultWidth = false
    )) {
        Surface(shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
            .padding(16.dp).safeDrawingPadding().semantics { paneTitle = title }) {
            Column(Modifier.heightIn(max = 720.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).semantics { heading() })
                    IconButton(onClick = onClose, enabled = dismissible) { AppIcon("close", "Cerrar $title") }
                }
                content()
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
