package com.example.siscomedor.ui.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.SelectionLine
import com.example.siscomedor.ui.Overlay
import com.example.siscomedor.ui.SisComeViewModel
import com.example.siscomedor.ui.components.*

@Composable
fun SelectionBar(model: SisComeViewModel) {
    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp), shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f).clickable { model.show(Overlay.Selection) }.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(model.records.lines.size.toString(), style = MaterialTheme.typography.titleMedium) }
                }
                Column(Modifier.weight(1f)) {
                    Text("Ver selección", style = MaterialTheme.typography.labelLarge)
                    Text(model.records.lines.map { it.service.label }.distinct().joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                    Text(price(model.records.lines.sumOf { it.cents }), style = MaterialTheme.typography.titleLarge)
                }
            }
            Button(onClick = model::beginCheckout, enabled = model.canCheckout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)) { Text("Pagar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(model: SisComeViewModel) {
    ModalBottomSheet(onDismissRequest = model::closeOverlay,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().heightIn(max = 640.dp).verticalScroll(rememberScrollState())
            .padding(20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tu selección", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = model::closeOverlay) { AppIcon("close", "Cerrar selección") }
            }
            SelectionSummaryPanel(model)
        }
    }
}

@Composable
fun SelectionSummaryPanel(model: SisComeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${model.records.lines.size} servicios seleccionados", style = MaterialTheme.typography.titleMedium)
        if (model.records.lines.isEmpty()) EmptyState("Aún no agregaste servicios", "Elige una variante del menú publicado. No se admiten cantidades.")
        model.records.lines.forEach { line ->
            SelectionLineRow(line, model.pending(line.key) != null) { model.remove(line.key) }
            if (model.unavailable(line.key)) Text("Agotado en esta demo: quita esta línea antes de pagar", style = MaterialTheme.typography.labelLarge)
        }
        if (model.records.lines.isNotEmpty()) {
            TextButton(onClick = model::clearSelection) { Text("Vaciar selecciones no pendientes") }
            HorizontalDivider()
            Text("Total: ${price(model.records.lines.sumOf { it.cents })}", style = MaterialTheme.typography.titleLarge)
            WalletSummary()
            Button(onClick = model::beginCheckout, enabled = model.canCheckout, modifier = Modifier.fillMaxWidth()) { Text("Revisar y pagar") }
            if (model.records.lines.any { model.pending(it.key) != null }) {
                Notice("Hay una operación pendiente", "Consulta la misma referencia desde Pagos. No se puede volver a pagar esta selección.", pending = true)
            }
        }
        Notice("Un ticket por servicio y fecha", "Elegir otra variante reemplaza la selección; elegir la misma otra vez la quita. Agregar no cobra ni emite un ticket.")
    }
}

@Composable
fun SelectionLineRow(line: SelectionLine, locked: Boolean, onRemove: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("${line.service.label} · ${dateLabel(line.date)}", style = MaterialTheme.typography.titleSmall)
                Text(line.variant.name, style = MaterialTheme.typography.bodyMedium)
                Text(line.service.hours, style = MaterialTheme.typography.bodySmall)
            }
            if (onRemove != null) IconButton(onClick = onRemove, enabled = !locked) {
                AppIcon("close", "Quitar ${line.service.label} del ${dateLabel(line.date)}")
            }
        }
        Text(price(line.cents), style = MaterialTheme.typography.titleSmall)
        if (locked) Text("Conservado para consultar la operación", style = MaterialTheme.typography.labelMedium)
    }
}
