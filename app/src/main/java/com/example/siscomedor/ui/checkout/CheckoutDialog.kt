package com.example.siscomedor.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionLineRow

@Composable
fun CheckoutDialog(model: SisComeViewModel) {
    // El diálogo observa el ViewModel: revisar, procesar y mostrar resultado son estados, no pantallas duplicadas.
    val result = model.result
    val resultTitle = result?.let {
        when {
            it.payment == PaymentStatus.UNKNOWN -> "Pago por verificar"
            it.unresolved -> "Emisión pendiente"
            it.payment == PaymentStatus.REJECTED -> "Pago rechazado"
            it.payment == PaymentStatus.NOT_STARTED -> "Menú agotado"
            else -> if (it.lines.size == 1) "Ticket emitido correctamente" else "Tickets emitidos correctamente"
        }
    }
    AppDialog(if (model.processing) "Operación en curso" else resultTitle ?: "Revisa antes de confirmar",
        model::closeOverlay, dismissible = !model.processing,
        kicker = if (model.processing || result != null) null else "Confirmación de pago",
        showTitle = result == null) {
        when {
            model.processing -> OperationProgress(model.stage, model.querying,
                model.review?.lines?.sumOf { it.cents } ?: 0, model.review?.lines?.size ?: 0)
            model.result != null -> ContentArrival(model.result!!.reference to model.result!!.payment) { OperationResult(model.result!!, model) }
            else -> {
                model.review?.let { review ->
                    // Referencia e introducción pertenecen al mismo contexto; líneas y decisión tienen separadores.
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Una operación para toda la selección. Pago y emisión se comprueban por separado.", style = MaterialTheme.typography.bodyMedium)
                        Text("Nueva operación: ${review.reference}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider()
                    Column {
                        review.lines.forEachIndexed { index, line ->
                            if (index > 0) HorizontalDivider()
                            SelectionLineRow(line, false)
                        }
                    }
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WalletSummary(model.selectedWallet, model::selectWallet)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(price(review.lines.sumOf { it.cents }), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DemoScenarioPicker(model.scenario, model::chooseScenario)
                    model.notice?.let { Notice("No se inició el pago", it) }
                    Text("No se realizará ningún cargo real.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = model::confirm, enabled = model.connection == Connection.ONLINE,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Confirmar pago")
                        Spacer(Modifier.width(8.dp))
                        AppIcon("arrow")
                    }
                }
            }
        }
    }
}

@Composable
fun DemoScenarioPicker(selected: Scenario, onSelect: (Scenario) -> Unit) {
    // rememberSaveable conserva solo la expansión al recrear la Activity; el escenario financiero vive en el ViewModel.
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { expanded = !expanded }) { Text("Opciones de demostración · ${if (expanded) "Ocultar" else "Mostrar"}") }
        if (expanded) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Scenario.entries.forEach { scenario ->
                    FilterChip(selected = selected == scenario, onClick = { onSelect(scenario) }, label = { Text(scenario.label, style = MaterialTheme.typography.labelMedium) })
                }
            }
            Text("${selected.label}: ${selected.hint}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OperationProgress(stage: Int, querying: Boolean, cents: Int, ticketCount: Int) {
    // Una consulta no repite el pago: muestra un paso propio. La barra cuenta etapas terminadas, no tiempo inventado.
    val labels = if (querying) listOf("Consultando la misma operación")
        else listOf("Verificando menú y disponibilidad", "Procesando pago", "Emitiendo tickets")
    val completed by animateFloatAsState(if (querying) 0f else ((stage - 1).coerceAtLeast(0) / 3f), tween(300), label = "Completed stages")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                if (motionEnabled()) CircularProgressIndicator(Modifier.fillMaxSize(), strokeWidth = 3.dp)
                else CircularProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxSize(), strokeWidth = 3.dp)
                AppIcon(if (querying) "clock" else if (stage == 3) "ticket" else if (stage == 2) "wallet" else "utensils", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Text(if (querying) "Consulta de la referencia original" else "${price(cents)} · $ticketCount ${if (ticketCount == 1) "ticket" else "tickets"}",
            Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (!querying) LinearProgressIndicator(progress = { completed }, modifier = Modifier.fillMaxWidth())
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            labels.forEachIndexed { index, label ->
                val done = !querying && index + 1 < stage
                val current = querying || index + 1 == stage
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.extraSmall,
                        color = if (done || current) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            if (done) AppIcon("check") else Text((index + 1).toString(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(label, style = if (current) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (current || done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (done) "Completado" else if (current) "En curso" else "Por iniciar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Text(if (querying) "Misma referencia, selección original. No se inicia un segundo pago." else "Espera el resultado antes de continuar.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun OperationResult(operation: PaymentOperation, model: SisComeViewModel) {
    // El resultado reemplaza el encabezado genérico; los estados inciertos mantienen sus instrucciones de recuperación.
    val failed = operation.payment == PaymentStatus.REJECTED || operation.payment == PaymentStatus.NOT_STARTED
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(color = if (failed) MaterialTheme.colorScheme.errorContainer else if (operation.unresolved) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { AppIcon(if (failed) "close" else if (operation.unresolved) "clock" else "check") }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(when {
            operation.payment == PaymentStatus.UNKNOWN -> "Pago por verificar"
            operation.unresolved -> "Emisión pendiente"
            operation.payment == PaymentStatus.REJECTED -> "Pago rechazado"
            operation.payment == PaymentStatus.NOT_STARTED -> "Menú agotado"
            operation.lines.size == 1 -> "Ticket emitido correctamente"
            else -> "Tickets emitidos correctamente"
        }, modifier = Modifier.fillMaxWidth().semantics { heading() }, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.titleLarge)
        val message = when {
            operation.unresolved && operation.payment == PaymentStatus.UNKNOWN ->
                "No se sabe si hubo cargo y no se emitieron tickets. Se conserva la selección; consulta esta misma referencia antes de intentar otro pago."
            operation.unresolved ->
                "Pago confirmado; emisión pendiente. Selección conservada. Consulta la emisión con la misma referencia; no pagues otra vez."
            operation.payment == PaymentStatus.REJECTED ->
                "No hubo cargo ni se emitieron tickets. Se conserva la selección; puedes revisar un nuevo intento con una nueva referencia de demostración."
            operation.payment == PaymentStatus.NOT_STARTED ->
                "Sin cargo ni emisión. Selección conservada hasta que quites la línea agotada. Revisa los demás servicios antes de una nueva operación."
            else ->
                "Pago confirmado y tickets emitidos. Los servicios emitidos se retiraron de tu selección; los demás se conservan."
        }
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite })
    }
    HorizontalDivider()
    // El importe describe la instantánea de todos los resultados, no un cargo garantizado ni el carrito mutable.
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Importe de la operación", style = MaterialTheme.typography.labelMedium)
        Text(price(operation.cents), style = MaterialTheme.typography.headlineSmall)
        Text("Referencia: ${operation.reference}", style = MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        // La referencia limita los identificadores y la acción a esta operación, incluso en consultas históricas.
        if (operation.issuance == IssuanceStatus.COMPLETED) {
            Text(model.pickerTickets(operation.reference).joinToString(" · ") { it.id }, style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
    when {
        operation.unresolved -> Button(onClick = { model.query(operation.reference) }, enabled = model.connection == Connection.ONLINE,
            modifier = Modifier.fillMaxWidth()) { Text(if (operation.payment == PaymentStatus.CONFIRMED) "Consultar emisión" else "Consultar esta operación") }
        operation.payment == PaymentStatus.REJECTED -> Button(onClick = model::retryRejected, enabled = model.canCheckout,
            modifier = Modifier.fillMaxWidth()) { Text("Revisar nuevo intento de demo") }
        operation.payment == PaymentStatus.NOT_STARTED -> {
            operation.lines.firstOrNull { it.key == operation.affectedKey }?.let {
                Text("Línea afectada: ${it.service.label} · ${dateLabel(it.date)} · ${it.variant.name}")
            }
            Button(onClick = model::recoverUnavailable, modifier = Modifier.fillMaxWidth()) { Text("Quitar y revisar selección") }
        }
        else -> Button(onClick = { model.openOperationTickets(operation.reference) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver mis tickets")
        }
    }
}
