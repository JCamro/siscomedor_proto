package com.example.siscomedor.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionLineRow

@Composable
fun CheckoutDialog(model: SisComeViewModel) {
    AppDialog("Operación simulada", model::closeOverlay, dismissible = !model.processing) {
        when {
            model.processing -> OperationProgress(model.stage, model.querying)
            model.result != null -> OperationResult(model.result!!, model)
            else -> {
                Text("Revisa antes de confirmar", style = MaterialTheme.typography.titleLarge)
                Text("Un solo pago simulado para toda la selección. El pago y la emisión se verifican por separado.")
                model.review?.let { review ->
                    Text("Nueva operación: ${review.reference}", style = MaterialTheme.typography.labelLarge)
                    review.lines.forEach { SelectionLineRow(it, false) }
                    WalletSummary()
                    Text("Total: ${price(review.lines.sumOf { it.cents })}", style = MaterialTheme.typography.titleLarge)
                }
                DemoScenarioPicker(model.scenario, model::chooseScenario)
                model.notice?.let { Notice("No se inició el pago", it) }
                Button(onClick = model::confirm, enabled = model.connection == Connection.ONLINE,
                    modifier = Modifier.fillMaxWidth()) { Text("Confirmar pago simulado") }
                Text("No se mueve dinero real. SisCome no solicita PIN, contraseñas ni códigos de tu billetera.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DemoScenarioPicker(selected: Scenario, onSelect: (Scenario) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Escenario de demostración", style = MaterialTheme.typography.titleSmall)
        Scenario.entries.forEach { scenario ->
            FilterChip(selected = selected == scenario, onClick = { onSelect(scenario) }, label = { Text(scenario.label) })
        }
        Text(selected.hint, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OperationProgress(stage: Int, querying: Boolean) {
    val labels = if (querying) listOf("Consultando la misma referencia", "Sin iniciar un segundo pago", "Comprobando emisión de la selección original")
        else listOf("Verificando menú, precio y disponibilidad", "Procesando pago simulado", "Comprobando emisión de tickets")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(if (querying) "Consulta en curso" else "Operación en curso", style = MaterialTheme.typography.titleLarge)
        labels.forEachIndexed { index, label -> Text("${index + 1}. $label${if (index + 1 == stage) " · En curso" else if (index + 1 < stage) " · Completado" else ""}") }
        Text("Espera el resultado. La confirmación duplicada está bloqueada.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OperationResult(operation: PaymentOperation, model: SisComeViewModel) {
    Text(operation.payment.label, style = MaterialTheme.typography.headlineSmall)
    Text(operation.issuance.label, style = MaterialTheme.typography.titleMedium)
    Text("${operation.reference} · ${price(operation.cents)}", style = MaterialTheme.typography.titleSmall)
    val message = when {
        operation.unresolved && operation.payment == PaymentStatus.UNKNOWN ->
            "No se confirmó si hubo cargo. No se emitieron tickets. Tu selección se conserva. Consulta esta misma referencia; no vuelvas a pagar."
        operation.unresolved ->
            "El cargo simulado está confirmado, pero los tickets siguen pendientes de emisión. Tu selección se conserva. Consulta la emisión con la misma referencia; no pagues otra vez."
        operation.payment == PaymentStatus.REJECTED ->
            "No hubo cargo y no se emitieron tickets. Tu selección se conserva. Esta demo permite revisar un nuevo intento con una referencia nueva; no representa una política institucional."
        operation.payment == PaymentStatus.NOT_STARTED ->
            "Un menú se agotó antes del pago. No hubo cargo ni emisión. La selección se conserva hasta que elijas quitar la línea afectada y revisar las demás."
        else ->
            "Cargo simulado confirmado y ${operation.lines.size} tickets emitidos. Las líneas emitidas se quitaron de la selección; las demás se conservan. Consulta los registros de esta misma operación y su estado de uso. No se inicia otro pago."
    }
    Notice("Resultado de esta operación", message, pending = operation.unresolved)
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
            Text("Ver tickets de esta operación")
        }
    }
}
