package com.example.siscomedor.ui.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionLineRow

@Composable
fun PaymentsScreen(model: SisComeViewModel) {
    // Una verificación agotada nunca inició pago: se excluye del historial, no se presenta como un rechazo.
    val payments = model.records.operations.filter { it.payment != PaymentStatus.NOT_STARTED }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PageTitle("Pagos", "Consulta el pago y la emisión de cada operación por separado.")
        // Se suma solo pago confirmado; la emisión se consulta por separado aunque ese pago ya esté confirmado.
        val confirmed = payments.filter { it.payment == PaymentStatus.CONFIRMED }
        OutlinedCard(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${confirmed.size} pagos confirmados", style = MaterialTheme.typography.labelLarge)
                    Text(price(confirmed.sumOf { it.cents }), style = MaterialTheme.typography.titleLarge)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge("${payments.count { it.payment == PaymentStatus.UNKNOWN }} por verificar", pending = true)
                    StatusBadge("${payments.count { it.payment == PaymentStatus.REJECTED }} rechazados", error = true)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Historial de operaciones", style = MaterialTheme.typography.titleLarge)
                PaymentFilterBar(model.paymentFilter) { model.paymentFilter = it }
            }
            val filtered = payments.filter { model.paymentFilter == null || it.payment == model.paymentFilter }
            if (filtered.isEmpty()) EmptyState("No hay pagos en este estado", "Selecciona otro filtro. La demo no consulta historiales institucionales.")
            filtered.forEach { PaymentOperationRow(it) { model.show(Overlay.PaymentDetail(it.reference)) } }
        }
        Text("Pago y ticket son estados independientes. Un pago confirmado puede tener emisión pendiente. Consulta la misma referencia antes de intentar otro pago.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PaymentFilterBar(filter: PaymentStatus?, onSelect: (PaymentStatus?) -> Unit) {
    // El filtro modifica la vista, no las operaciones; FilterChip aporta estado seleccionado y foco nativos.
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = filter == null, onClick = { onSelect(null) }, label = { Text("Todos") })
        listOf(PaymentStatus.CONFIRMED, PaymentStatus.UNKNOWN, PaymentStatus.REJECTED).forEach { status ->
            FilterChip(selected = filter == status, onClick = { onSelect(status) }, label = { Text(status.label) })
        }
    }
}

@Composable
fun PaymentOperationRow(operation: PaymentOperation, onDetail: () -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(operation.reference, style = MaterialTheme.typography.titleSmall)
                    Text(operation.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(price(operation.cents), style = MaterialTheme.typography.titleLarge)
            }
            Text(operation.lines.joinToString(" / ") { "${it.service.label} · ${it.variant.name}" }, style = MaterialTheme.typography.bodyMedium)
            // Dos hechos próximos siguen siendo independientes: no se deduce la emisión del estado financiero.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(operation.payment.label, pending = operation.payment == PaymentStatus.UNKNOWN, error = operation.payment == PaymentStatus.REJECTED)
                Text(operation.issuance.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDetail) { Text("Ver detalle de operación") }
        }
    }
}

@Composable
fun PaymentDetailDialog(operation: PaymentOperation, model: SisComeViewModel) {
    // El detalle usa la billetera y líneas congeladas de la operación, no preferencias actuales del estudiante.
    AppDialog("Detalle de operación", model::closeOverlay) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(operation.reference, style = MaterialTheme.typography.titleMedium)
            Text("Fecha de operación: ${operation.createdAt}", style = MaterialTheme.typography.bodySmall)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WalletSummary(operation.wallet, null)
            Text("Importe de la operación: ${price(operation.cents)}", style = MaterialTheme.typography.titleLarge)
        }
        HorizontalDivider()
        // Filas explícitas reemplazan dos avisos grandes; el estado incierto conserva su advertencia de cargo desconocido.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppIcon(if (operation.payment == PaymentStatus.UNKNOWN) "clock" else "card", tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Estado del pago", style = MaterialTheme.typography.labelMedium)
                    Text(operation.payment.label, style = MaterialTheme.typography.titleSmall)
                    Text(when (operation.payment) {
                        PaymentStatus.UNKNOWN -> "No se sabe si hubo cargo. Consulta esta misma referencia."
                        PaymentStatus.CONFIRMED -> "Cargo simulado confirmado; no se mueve dinero real."
                        else -> "Sin cargo realizado."
                    }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppIcon(if (operation.issuance == IssuanceStatus.PENDING) "clock" else "ticket", tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Estado de emisión", style = MaterialTheme.typography.labelMedium)
                    Text(operation.issuance.label, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Servicios incluidos · Fechas de consumo", style = MaterialTheme.typography.titleMedium)
            Column {
                operation.lines.forEachIndexed { index, line ->
                    if (index > 0) HorizontalDivider()
                    SelectionLineRow(line, false)
                }
            }
        }
        // La recuperación consulta esa misma referencia; no se ofrece pagar otra vez mientras siga incierta.
        if (operation.unresolved) {
            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { model.query(operation.reference) }, enabled = model.connection == Connection.ONLINE,
                    modifier = Modifier.fillMaxWidth()) { Text("Consultar misma referencia") }
                Text("Esta consulta no crea un segundo pago. La demo resuelve la selección original de esta operación.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
