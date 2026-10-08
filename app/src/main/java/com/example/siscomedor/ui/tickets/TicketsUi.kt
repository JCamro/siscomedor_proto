package com.example.siscomedor.ui.tickets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun MyTicketsScreen(model: SisComeViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        PageTitle("Mis tickets", "Historial ficticio por fecha de consumo. El pago no equivale a un ticket emitido.")
        Text("${model.availableTickets.size} disponibles · ${model.records.tickets.count { it.status == TicketStatus.CONSUMED }} consumidos",
            style = MaterialTheme.typography.titleMedium)
        TicketFilterBar(model.ticketFilter) { model.ticketFilter = it }
        val filtered = model.records.tickets.filter { model.ticketFilter == null || it.status == model.ticketFilter }
            .sortedWith(compareBy({ it.line.date }, { it.line.service.ordinal }))
        if (filtered.isEmpty()) EmptyState("No hay tickets en este estado", "Prueba otro filtro. Estos registros no están sincronizados con una institución.")
        filtered.forEach { TicketCard(it) { model.openTickets(it.id) } }
        Notice("Código sin validez institucional", "Puedes abrir y descargar los códigos ficticios incluso sin conexión. No acreditan vigencia, consumo ni ingreso real.")
    }
}

@Composable
fun TicketFilterBar(filter: TicketStatus?, onSelect: (TicketStatus?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = filter == null, onClick = { onSelect(null) }, label = { Text("Todos") })
        TicketStatus.entries.forEach { state ->
            FilterChip(selected = filter == state, onClick = { onSelect(state) }, label = { Text(state.label) })
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onQr: () -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppIcon("ticket"); Text(ticket.status.label, style = MaterialTheme.typography.labelLarge) }
            Text(ticket.line.service.label, style = MaterialTheme.typography.titleLarge)
            Text("${dateLabel(ticket.line.date)} · ${ticket.line.service.hours}")
            Text(ticket.line.variant.name, style = MaterialTheme.typography.bodyMedium)
            Text("${DemoCatalog.student} · ${ticket.id}", style = MaterialTheme.typography.bodySmall)
            Text("Emisión de demo: ${ticket.issuedAt}", style = MaterialTheme.typography.bodySmall)
            if (ticket.canShowQr) Button(onClick = onQr, modifier = Modifier.fillMaxWidth()) { Text("Ver QR ficticio") }
            else {
                Text("Este registro no ofrece un código para usar", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = onQr) { Text("Ver registro") }
            }
        }
    }
}

@Composable
fun TicketPickerDialog(model: SisComeViewModel, operationReference: String? = null) {
    val targets = model.pickerTickets(operationReference)
    AppDialog("Selecciona un ticket", model::closeOverlay) {
        Text(if (operationReference == null) "${targets.size} tickets disponibles en esta demo."
            else "${targets.size} registros emitidos por $operationReference. Consulta su estado de uso.")
        if (targets.isEmpty()) EmptyState("No hay registros en este contexto", "No se mostrarán tickets de otra operación. Puedes cerrar este detalle y consultar el historial.")
        targets.forEach { ticket ->
            OutlinedButton(onClick = { model.openTickets(ticket.id, operationReference) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${ticket.line.service.label} · ${dateLabel(ticket.line.date)}")
                    Text(ticket.line.variant.name)
                    Text(ticket.id, style = MaterialTheme.typography.labelMedium)
                    Text(ticket.status.label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun TicketDetailDialog(ticket: Ticket, model: SisComeViewModel, operationReference: String? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportId by rememberSaveable { mutableStateOf<String?>(null) }
    var exportMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        val frozenTicket = model.records.tickets.firstOrNull { it.id == exportId }
        scope.launch {
            val message = when {
                uri == null -> "Exportación cancelada. El ticket se conserva."
                frozenTicket == null -> "El ticket no está disponible en esta sesión. No se exportó el código."
                !frozenTicket.canShowQr -> "Este registro no tiene un QR disponible para usar o descargar."
                else -> runCatching { QrExport.write(context.contentResolver, uri, frozenTicket) }
                    .fold({ "PNG ficticio guardado en la ubicación elegida." }, { "No se pudo guardar el PNG. El ticket se conserva; puedes reintentar." })
            }
            exportMessage = message
            exportId = null
        }
    }
    AppDialog("Ticket de ${ticket.line.service.label.lowercase()}", model::closeOverlay) {
        if (model.pickerTickets(operationReference).size > 1) {
            TextButton(onClick = { model.show(Overlay.TicketPicker(operationReference)) }) {
                Text(if (operationReference == null) "Todos los tickets disponibles" else "Tickets de esta operación")
            }
        }
        operationReference?.let { Text("Operación: $it", style = MaterialTheme.typography.labelLarge) }
        Text(ticket.status.label, style = MaterialTheme.typography.titleSmall)
        Text("${dateLabel(ticket.line.date)} · ${ticket.line.service.hours}")
        Text(ticket.line.variant.name, style = MaterialTheme.typography.titleMedium)
        Text(DemoCatalog.student, style = MaterialTheme.typography.titleMedium)
        Text(ticket.id)
        if (ticket.canShowQr) {
            QrDisplay(ticket)
            QrRefreshControls(ticket, model)
            Button(onClick = { exportId = ticket.id; exportMessage = "Guardando el código ficticio…"; export.launch("ticket-${ticket.id}.png") },
                enabled = exportId == null, modifier = Modifier.fillMaxWidth()) {
                AppIcon("download")
                Spacer(Modifier.width(8.dp))
                Text("Descargar QR como PNG")
            }
        } else {
            Notice("Registro de solo lectura", "Ticket ${ticket.status.label.lowercase()}. No se muestra, recarga ni descarga un QR para usar. Este estado no libera el cupo ni inicia otro pago.")
        }
        exportMessage?.let { Notice("Exportación de código", it) }
    }
}

@Composable
fun QrRefreshControls(ticket: Ticket, model: SisComeViewModel) {
    val state = model.qrState(ticket)
    val loading = state.status == QrRefreshStatus.LOADING
    val message = when {
        loading -> "Consultando el mismo QR ficticio… Se conserva el código guardado mientras esperamos."
        model.connection != Connection.ONLINE || state.status == QrRefreshStatus.OFFLINE ->
            "Sin conexión en este intento. Se conserva el QR guardado, no comprobado recientemente."
        state.status == QrRefreshStatus.FAILED ->
            "La consulta de demo falló. Se conserva el QR guardado, no comprobado recientemente. Puedes reintentar."
        state.status == QrRefreshStatus.REFRESHED ->
            "Consulta de demo completada. El identificador y el contenido del QR son los mismos."
        else -> "QR guardado en esta sesión; todavía no se consultó recientemente."
    }
    Notice("Consulta del QR ficticio", "$message No hay validación institucional.", pending = loading)
    state.lastConsultedAt?.let { Text("Última consulta exitosa de demo: $it", style = MaterialTheme.typography.bodySmall) }
    Text("Simular resultado de recarga", style = MaterialTheme.typography.titleSmall)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QrRefreshFixture.entries.forEach { fixture ->
            FilterChip(selected = model.qrFixture == fixture, onClick = { model.chooseQrFixture(fixture) },
                enabled = !loading, label = { Text(fixture.label) })
        }
    }
    OutlinedButton(onClick = { model.refreshQr(ticket.id) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
        Text(if (loading) "Consultando mismo QR…" else "Recargar mismo QR")
    }
}

@Composable
fun QrDisplay(ticket: Ticket) {
    if (!ticket.canShowQr) return
    Canvas(Modifier.fillMaxWidth().heightIn(max = 240.dp).aspectRatio(1f)
        .semantics { contentDescription = "Código QR ficticio del ticket ${ticket.id}, sin validez institucional" }) {
        drawRect(Color.White)
        val cell = size.minDimension / (DemoQr.size + 8)
        val left = (size.width - cell * DemoQr.size) / 2
        val top = (size.height - cell * DemoQr.size) / 2
        for (y in 0 until DemoQr.size) for (x in 0 until DemoQr.size) {
            if (DemoQr.cell(ticket.qrContent, x, y)) drawRect(Color(0xFF102F27), Offset(left + x * cell, top + y * cell), Size(cell, cell))
        }
    }
}
