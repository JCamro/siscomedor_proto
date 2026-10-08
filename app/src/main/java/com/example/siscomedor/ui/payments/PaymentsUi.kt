package com.example.siscomedor.ui.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionLineRow

@Composable
fun PaymentsScreen(model: SisComeViewModel) {
    val payments = model.records.operations.filter { it.payment != PaymentStatus.NOT_STARTED }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PageTitle("Pagos", "Operaciones de demo. La fecha del pago es distinta de la fecha de consumo.")
        val confirmed = payments.filter { it.payment == PaymentStatus.CONFIRMED }
        Text("${confirmed.size} confirmados · ${price(confirmed.sumOf { it.cents })}", style = MaterialTheme.typography.titleMedium)
        Text("${payments.count { it.payment == PaymentStatus.UNKNOWN }} por verificar · ${payments.count { it.payment == PaymentStatus.REJECTED }} rechazados")
        PaymentFilterBar(model.paymentFilter) { model.paymentFilter = it }
        val filtered = payments.filter { model.paymentFilter == null || it.payment == model.paymentFilter }
        if (filtered.isEmpty()) EmptyState("No hay pagos en este estado", "Selecciona otro filtro. La demo no consulta historiales institucionales.")
        filtered.forEach { PaymentOperationRow(it) { model.show(Overlay.PaymentDetail(it.reference)) } }
        Notice("Pago y ticket son estados independientes", "Un pago confirmado puede tener emisión pendiente. Consulta la misma referencia antes de intentar otro pago.", pending = true)
    }
}

@Composable
fun PaymentFilterBar(filter: PaymentStatus?, onSelect: (PaymentStatus?) -> Unit) {
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
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(operation.reference, style = MaterialTheme.typography.titleMedium)
            Text("${operation.createdAt} · ${price(operation.cents)}", style = MaterialTheme.typography.bodyMedium)
            Text(operation.lines.joinToString(" / ") { "${it.service.label} · ${it.variant.name}" }, style = MaterialTheme.typography.bodyMedium)
            Text(operation.payment.label, style = MaterialTheme.typography.titleSmall)
            Text(operation.issuance.label, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDetail) { Text("Ver detalle de operación") }
        }
    }
}

@Composable
fun PaymentDetailDialog(operation: PaymentOperation, model: SisComeViewModel) {
    AppDialog("Detalle de operación", model::closeOverlay) {
        Text(operation.reference, style = MaterialTheme.typography.titleLarge)
        Text("Fecha de operación: ${operation.createdAt}")
        WalletSummary()
        Text("Importe: ${price(operation.cents)}", style = MaterialTheme.typography.titleLarge)
        Notice("Estado del pago", operation.payment.label + when (operation.payment) {
            PaymentStatus.UNKNOWN -> ". No se sabe si hubo cargo. Consulta esta misma referencia."
            PaymentStatus.CONFIRMED -> ". Cargo simulado confirmado; no se mueve dinero real."
            else -> ". Sin cargo realizado."
        }, pending = operation.payment == PaymentStatus.UNKNOWN)
        Notice("Estado de emisión", operation.issuance.label, pending = operation.issuance == IssuanceStatus.PENDING)
        Text("Servicios incluidos · Fechas de consumo", style = MaterialTheme.typography.titleMedium)
        operation.lines.forEach { SelectionLineRow(it, false) }
        if (operation.unresolved) {
            Button(onClick = { model.query(operation.reference) }, enabled = model.connection == Connection.ONLINE,
                modifier = Modifier.fillMaxWidth()) { Text("Consultar misma referencia") }
            Text("Esta consulta no crea un segundo pago. La demo resuelve la selección original de esta operación.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
