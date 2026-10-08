package com.example.siscomedor.ui.selection

import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.siscomedor.domain.SelectionLine
import com.example.siscomedor.ui.Overlay
import com.example.siscomedor.ui.SisComeViewModel
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.theme.BrandYellow
import com.example.siscomedor.ui.theme.TicketGreen
import com.example.siscomedor.ui.theme.TicketSecondary

@Composable
fun SelectionBar(model: SisComeViewModel) {
    // Dos acciones distintas: el resumen abre la selección, Pagar abre la revisión sin cobrar todavía.
    Surface(color = TicketGreen, contentColor = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).heightIn(min = 48.dp).semantics { contentDescription = "Abrir selección" }
                .clickable { model.show(Overlay.Selection) }.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = BrandYellow, contentColor = TicketGreen, shape = RoundedCornerShape(50), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(model.records.lines.size, transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) }, label = "Selection count") {
                            Text(it.toString(), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(model.records.lines.map { it.service.label }.distinct().joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = TicketSecondary)
                    AnimatedContent(model.records.lines.sumOf { it.cents }, transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) }, label = "Selection total") {
                        Text(price(it), style = MaterialTheme.typography.titleMedium)
                    }
                }
                AppIcon("arrow", modifier = Modifier.size(18.dp))
            }
            Button(onClick = model::beginCheckout, enabled = model.canCheckout,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TicketGreen,
                    disabledContainerColor = Color(0xFFD1DDD7), disabledContentColor = TicketGreen)) { Text("Pagar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(model: SisComeViewModel) {
    // Un solo cuerpo desplazable permite llegar al total incluso con muchas líneas o texto ampliado.
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val maxHeight = with(LocalDensity.current) { windowHeight.toDp() * 0.82f }
    ModalBottomSheet(onDismissRequest = model::closeOverlay,
        containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().heightIn(max = maxHeight).navigationBarsPadding()) {
            Box(Modifier.padding(horizontal = 20.dp)) { SelectionHeader(model, model::closeOverlay) }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(20.dp)) {
                SelectionSummaryPanel(model, showHeader = false)
            }
        }
    }
}

@Composable
fun SelectionSummaryPanel(model: SisComeViewModel, showHeader: Boolean = true) {
    // Panel y hoja comparten contenido, no dos desplazamientos anidados ni un pie fijo que falle en poca altura.
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (showHeader) SelectionHeader(model)
        if (model.records.lines.isEmpty()) {
            AppIcon("ticket", tint = MaterialTheme.colorScheme.primary)
            EmptyState("Aún no has agregado servicios", "Elige un menú. Puedes combinar servicios de distintos días y pagar todo junto.")
        }
        Column {
            model.records.lines.forEachIndexed { index, line ->
                if (index > 0) HorizontalDivider()
                SelectionLineRow(line, model.pending(line.key) != null) { model.remove(line.key) }
                if (model.unavailable(line.key)) Text("Agotado en esta demo: quita esta línea antes de pagar", style = MaterialTheme.typography.labelLarge)
            }
        }
        if (model.records.lines.isNotEmpty()) {
            // Una línea pendiente no se borra ni paga otra vez; la explicación acompaña los controles deshabilitados.
            val pending = model.records.lines.any { model.pending(it.key) != null }
            HorizontalDivider()
            // Importe, billetera y confirmación forman el grupo de decisión, separado de las líneas.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(price(model.records.lines.sumOf { it.cents }), style = MaterialTheme.typography.headlineSmall)
                }
                WalletSummary(model.selectedWallet, model::selectWallet)
                Button(onClick = model::beginCheckout, enabled = model.canCheckout, modifier = Modifier.fillMaxWidth()) { Text("Revisar y pagar") }
            }
            if (pending) {
                Notice("Selección conservada", "Consulta la misma referencia desde Pagos. No puedes vaciar ni volver a pagar mientras la operación esté pendiente.", pending = true)
            }
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Un ticket por servicio y fecha", style = MaterialTheme.typography.labelMedium)
            Text("Otra variante reemplaza la selección; tocar la misma la quita. Agregar no realiza un cobro.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectionHeader(model: SisComeViewModel, onClose: (() -> Unit)? = null) {
    // Título, cantidad y limpieza comparten contexto; el cierre reserva su propia área de 48 dp.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tu selección", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text("${model.records.lines.size} servicios seleccionados", style = MaterialTheme.typography.bodySmall)
        }
        if (model.records.lines.isNotEmpty()) TextButton(onClick = model::clearSelection,
            enabled = model.records.lines.none { model.pending(it.key) != null }) { Text("Vaciar") }
        if (onClose != null) IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            AppIcon("close", description = "Cerrar selección")
        }
    }
}

@Composable
fun SelectionLineRow(line: SelectionLine, locked: Boolean, onRemove: (() -> Unit)? = null) {
    // weight permite que nombre y fecha se ajusten; la columna de precio no pierde su ancho ni su acción accesible.
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            MenuPhoto(line.variant.imageUrl, Modifier.size(40.dp).clip(MaterialTheme.shapes.extraSmall), thumbnail = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${line.service.label} · ${dateLabel(line.date)}", style = MaterialTheme.typography.labelMedium)
                Text(line.variant.name, style = MaterialTheme.typography.titleSmall)
                Text(line.service.hours, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(price(line.cents), style = MaterialTheme.typography.titleSmall)
                if (onRemove != null) IconButton(onClick = onRemove, enabled = !locked) {
                    AppIcon("close", description = "Quitar ${line.service.label} del ${dateLabel(line.date)}")
                }
            }
        }
        if (locked) Text("Conservado para consultar la operación", style = MaterialTheme.typography.labelMedium)
    }
}
