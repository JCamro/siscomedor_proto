package com.example.siscomedor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import coil3.compose.SubcomposeAsyncImage

// Los importes del dominio son centavos enteros; el formato local se aplica solo al mostrarlos.
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
    "wallet" to "M3,7.5 H19 A2,2 0,0 1,21 9.5 V18 A2,2 0,0 1,19 20 H5 A2,2 0,0 1,3 18 V6 A2,2 0,0 1,5 4 H17 M16,12 H21 V16 H16 A2,2 0,0 1,16 12 Z",
    "user" to "M8,8 C8,3 16,3 16,8 C16,13 8,13 8,8 M4,21 C4,12 20,12 20,21",
    "utensils" to "M7,3 V11 M4,3 V8 A3,3 0,0 0,10 8 V3 M7,11 V21 M16,3 V21 M16,3 C19,4 20,7 20,10 H16",
    "calendar" to "M5,5 H19 A2,2 0,0 1,21 7 V19 A2,2 0,0 1,19 21 H5 A2,2 0,0 1,3 19 V7 A2,2 0,0 1,5 5 Z M16,3 V7 M8,3 V7 M3,10 H21",
    "clock" to "M12,3 A9,9 0,1 1,12 21 A9,9 0,1 1,12 3 M12,7 V12 L15,14",
    "info" to "M12,3 A9,9 0,1 1,12 21 A9,9 0,1 1,12 3 M12,11 V17 M12,7 H12.01",
    "plus" to "M12,5 V19 M5,12 H19",
    "check" to "M5,12 L9,16 L19,6",
    "close" to "M6,6 L18,18 M18,6 L6,18",
    "download" to "M12,3 L12,15 M7,10 L12,15 L17,10 M3,16 L3,21 L21,21 L21,16",
    "arrow" to "M5,12 L19,12 M14,7 L19,12 L14,17"
)

@Composable
fun AppIcon(name: String, modifier: Modifier = Modifier, description: String? = null, tint: Color = LocalContentColor.current) {
    // remember evita reconstruir el vector en cada recomposición; null oculta iconos decorativos a TalkBack.
    val vector = remember(name) {
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(PathParser().parsePathString(iconPaths.getValue(name)).toNodes(),
                stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    Icon(vector, description, modifier = modifier, tint = tint)
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
    // La región viva anuncia cambios sin quitar el foco; el texto explica el estado además del color.
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
fun WalletSummary(selected: String, onSelect: ((String) -> Unit)?) {
    // La apertura es estado local efímero; la cuenta elegida pertenece al ViewModel compartido.
    var open by remember { mutableStateOf(false) }
    Surface(onClick = { if (onSelect != null) open = true }, enabled = onSelect != null,
        color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        AppIcon("wallet")
        Column(Modifier.weight(1f)) {
            Text("Billetera seleccionada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(selected, style = MaterialTheme.typography.titleSmall)
        }
        if (onSelect != null) Text("Cambiar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
    }
    if (open && onSelect != null) AppDialog("Selecciona una billetera", { open = false }) {
        Text("Cuentas de demostración; no hay conexión con proveedores ni se solicitan credenciales.", style = MaterialTheme.typography.bodyMedium)
        com.example.siscomedor.data.DemoCatalog.wallets.forEach { wallet ->
            OutlinedButton(onClick = { onSelect(wallet); open = false }, modifier = Modifier.fillMaxWidth()) {
                Text(if (wallet == selected) "$wallet · Seleccionada" else wallet)
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, pending: Boolean = false, neutral: Boolean = false, error: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(50), color = when {
        error -> colors.errorContainer
        pending -> colors.tertiaryContainer
        neutral -> colors.surfaceContainerHigh
        else -> colors.primaryContainer
    }, contentColor = when {
        error -> colors.onErrorContainer
        pending -> colors.onTertiaryContainer
        neutral -> colors.onSurfaceVariant
        else -> colors.onPrimaryContainer
    }) { Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium) }
}

@Composable
fun motionEnabled(): Boolean = (rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f) > 0f

@Composable
fun ShimmerBlock(modifier: Modifier = Modifier) {
    // Las animaciones infinitas tienen una rama estática cuando Android desactiva el movimiento.
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlight = MaterialTheme.colorScheme.surfaceContainerLow
    if (!motionEnabled()) {
        Surface(modifier = modifier, color = base, shape = MaterialTheme.shapes.small) {}
        return
    }
    val transition = rememberInfiniteTransition(label = "Menu loading")
    val phase by transition.animateFloat(0f, 2f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "Shimmer")
    Box(modifier.clip(MaterialTheme.shapes.small).drawBehind {
        val start = size.width * (phase - 1f)
        drawRect(Brush.linearGradient(listOf(base, highlight, base), Offset(start, 0f), Offset(start + size.width, size.height)))
    })
}

@Composable
fun MenuPhoto(url: String, modifier: Modifier = Modifier, thumbnail: Boolean = false) {
    // La foto es ilustrativa: carga y error conservan el espacio sin bloquear descripción ni selección.
    SubcomposeAsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ShimmerBlock(Modifier.fillMaxSize())
                if (!thumbnail) Text("Cargando foto…", style = MaterialTheme.typography.labelMedium)
            }
        },
        error = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    AppIcon("utensils")
                    if (!thumbnail) Text("Foto no disponible", style = MaterialTheme.typography.labelMedium)
                }
            }
        })
}

@Composable
fun ContentArrival(identity: Any, content: @Composable ColumnScope.() -> Unit) {
    // La identidad reinicia solo la llegada del contenido; LaunchedEffect se cancela al salir de composición.
    val enabled = motionEnabled()
    val arrival = remember(identity) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(identity, enabled) {
        if (enabled) arrival.animateTo(1f, tween(220, easing = FastOutSlowInEasing)) else arrival.snapTo(1f)
    }
    Column(Modifier.fillMaxWidth().graphicsLayer {
        alpha = arrival.value
        translationY = (1f - arrival.value) * 6.dp.toPx()
    }, verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
fun AppDialog(title: String, onClose: () -> Unit, dismissible: Boolean = true, kicker: String? = null, showTitle: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    // Back, toque exterior y cierre comparten la misma restricción mientras se procesa una operación.
    Dialog(onDismissRequest = onClose, properties = DialogProperties(
        dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible, usePlatformDefaultWidth = false
    )) {
        Surface(shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
            .padding(16.dp).safeDrawingPadding().semantics { paneTitle = title }) {
            Box {
                Column(Modifier.heightIn(max = 720.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // El encabezado reserva ancho para la X; el texto puede crecer sin invadir su área táctil.
                    if (showTitle) Column(Modifier.fillMaxWidth().padding(end = 52.dp).heightIn(min = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        kicker?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    } else Spacer(Modifier.height(28.dp))
                    content()
                }
                // La X queda fija arriba, independiente del título, subtítulo y desplazamiento interno.
                IconButton(onClick = onClose, enabled = dismissible,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(48.dp)) {
                    AppIcon("close", description = "Cerrar $title")
                }
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
