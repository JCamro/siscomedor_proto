package com.example.siscomedor.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionSummaryPanel

@Composable
fun HomeScreen(model: SisComeViewModel, wide: Boolean) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        WelcomeHeader()
        model.availableTickets.firstOrNull()?.let { ActiveTicketCard(it) { model.openTickets(it.id) } }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) { MenuSection(model) }
                Column(Modifier.width(320.dp)) { SelectionSummaryPanel(model) }
            }
        } else MenuSection(model)
        RecentActivitySection(model)
        DemoNotice()
    }
}

@Composable
fun WelcomeHeader() = PageTitle("Hola, Antonia", "Miércoles, 7 de octubre de 2026 · Hora de demo ${DemoCatalog.time}")

@Composable
fun ActiveTicketCard(ticket: Ticket, onQr: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppIcon("ticket"); Text("Disponible para usar", style = MaterialTheme.typography.labelLarge) }
            Text("Tu ticket de ${ticket.line.service.label.lowercase()}", style = MaterialTheme.typography.headlineSmall)
            Text("${dateLabel(ticket.line.date)} · ${ticket.line.service.hours}")
            Text(ticket.line.variant.name, style = MaterialTheme.typography.bodyMedium)
            Text(ticket.id, style = MaterialTheme.typography.labelMedium)
            Button(onClick = onQr, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)) {
                Text("Mostrar QR ficticio")
                Spacer(Modifier.width(8.dp))
                AppIcon("arrow")
            }
        }
    }
}

@Composable
fun MenuSection(model: SisComeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageTitle("Menú del comedor", "Combina servicios de fechas publicadas. Agregar no cobra ni emite un ticket.")
        DateSelector(model)
        ServiceSelector(model)
        Text("Consumo: ${dateLabel(model.date)} · Venta hasta ${model.service.saleEnd}", style = MaterialTheme.typography.bodyMedium)
        Text("Catálogo ficticio · Revisión de demo: 7 oct, 11:42", style = MaterialTheme.typography.bodySmall)
        val key = SelectionKey(model.date, model.service)
        val owned = model.owned(key)
        val pending = model.pending(key)
        when {
            owned != null -> OwnedTicketNotice(owned) { model.openTickets(owned.id) }
            pending != null -> {
                Notice("Esta clave tiene una operación pendiente", "${pending.reference}. Tu selección se conserva. Consulta la misma referencia; no pagues otra vez.", pending = true)
                OutlinedButton(onClick = { model.show(Overlay.PaymentDetail(pending.reference)) }) { Text("Ver operación") }
            }
            model.connection == Connection.LOADING || model.menuLoading -> MenuLoadingSkeleton()
            model.connection != Connection.ONLINE -> {
                Notice(if (model.connection == Connection.OFFLINE) "Datos guardados sin conexión" else "No se pudo consultar el menú",
                    "La selección se conserva; el catálogo no está comprobado recientemente. No puedes iniciar un pago nuevo. Los tickets emitidos siguen accesibles.")
                Button(onClick = { model.connect(ConnectionFixture.NORMAL) }) { Text("Reintentar consulta") }
            }
            !DemoCatalog.published(model.date) -> EmptyState("Sin publicación", "No hay menú publicado para esta fecha. Elige una fecha con publicación.")
            DemoCatalog.closed(key) -> SaleClosedNotice(model.service) { model.selectDate("2026-10-08") }
            model.unavailable(key) -> {
                Notice("Publicado, pero agotado en esta demo", "No se inició un pago para esta clave. Revisa y quita la línea afectada; el resto de la selección se conserva.")
                if (model.records.lines.any { it.key == key }) OutlinedButton(onClick = { model.remove(key) }) { Text("Quitar línea agotada") }
            }
            else -> {
                DemoCatalog.variants(model.service).forEach { variant ->
                    MenuVariantCard(variant, model.service.cents,
                        model.records.lines.any { it.key == key && it.variant.id == variant.id }) { model.toggle(variant.id) }
                }
                AvailabilityNotice(model.service)
            }
        }
    }
}

@Composable
fun DateSelector(model: SisComeViewModel) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DemoCatalog.dates.forEach { date ->
            DateOptionItem(date, model.date == date.date, model.records.lines.count { it.date == date.date }) { model.selectDate(date.date) }
        }
    }
}

@Composable
fun DateOptionItem(date: PublishedDate, active: Boolean, count: Int, onSelect: () -> Unit) {
    Surface(onClick = onSelect, shape = RoundedCornerShape(12.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.widthIn(min = 76.dp).semantics { selected = active }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(date.day, style = MaterialTheme.typography.labelMedium)
            Text(date.date.substring(8).toInt().toString(), style = MaterialTheme.typography.titleLarge)
            Text(if (date.published) "Octubre" else "Sin menú", style = MaterialTheme.typography.labelSmall)
            if (count > 0) Text("$count elegidos", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ServiceSelector(model: SisComeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Filtrar menú por servicio", style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Service.entries.forEach { service ->
                val key = SelectionKey(model.date, service)
                val state = when {
                    model.owned(key) != null -> "Ticket emitido"
                    model.pending(key) != null -> "Por resolver"
                    DemoCatalog.closed(key) -> "Venta cerrada"
                    model.unavailable(key) -> "Agotado"
                    model.records.lines.any { it.key == key } -> "Seleccionado"
                    else -> service.hours
                }
                ServiceOptionCard(service, state, model.service == service, DemoCatalog.closed(key)) { model.selectService(service) }
            }
        }
    }
}

@Composable
fun ServiceOptionCard(service: Service, state: String, active: Boolean, closed: Boolean, onSelect: () -> Unit) {
    val color = when {
        active && closed -> MaterialTheme.colorScheme.surfaceVariant
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(onClick = onSelect, color = color, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.widthIn(min = 128.dp).semantics { selected = active }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(service.label, style = MaterialTheme.typography.titleSmall)
            Text(state, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun MenuVariantCard(variant: Variant, cents: Int, active: Boolean, onToggle: () -> Unit) {
    Surface(onClick = onToggle, shape = RoundedCornerShape(16.dp),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics { selected = active }) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppIcon("utensils")
                Text(variant.name, style = MaterialTheme.typography.titleMedium)
            }
            Text(variant.description, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(price(cents), style = MaterialTheme.typography.titleMedium)
                Text(if (active) "Seleccionado · Quitar" else "Agregar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun OwnedTicketNotice(ticket: Ticket, onQr: () -> Unit) {
    Notice("Ya tienes este ticket", "${ticket.line.service.label} · ${dateLabel(ticket.line.date)} · ${ticket.id}. Solo se permite uno por servicio y fecha.")
    if (ticket.status == TicketStatus.AVAILABLE) OutlinedButton(onClick = onQr) { Text("Ver QR ficticio") }
    else Text(ticket.status.label)
}

@Composable
fun SaleClosedNotice(service: Service, onTomorrow: () -> Unit) {
    Notice("Venta cerrada", "La venta de ${service.label.lowercase()} terminó a las ${service.saleEnd}. Son las ${DemoCatalog.time} en esta demo.")
    OutlinedButton(onClick = onTomorrow) { Text("Ver publicación de mañana") }
}

@Composable
fun AvailabilityNotice(service: Service) = Notice("Disponible para seleccionar",
    "Venta hasta ${service.saleEnd}. Publicación, precio y disponibilidad se vuelven a comprobar antes del pago. No se reserva cupo.")

@Composable
fun MenuLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Consultando menú publicado…", style = MaterialTheme.typography.bodyMedium)
        repeat(2) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(112.dp)) {}
        }
    }
}

@Composable
fun RecentActivitySection(model: SisComeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Actividad reciente", style = MaterialTheme.typography.titleLarge)
        model.records.operations.take(2).forEach { operation ->
            Text("${operation.reference} · ${operation.payment.label} · ${operation.issuance.label}", style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { model.navigate(Destination.PAYMENTS) }) { Text("Ver historial de pagos") }
    }
}
