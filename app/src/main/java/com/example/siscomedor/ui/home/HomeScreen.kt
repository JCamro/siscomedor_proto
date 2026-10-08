package com.example.siscomedor.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.*
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.selection.SelectionSummaryPanel
import com.example.siscomedor.ui.theme.TicketGreen
import com.example.siscomedor.ui.theme.TicketSecondary
import com.example.siscomedor.ui.theme.BrandYellow

@Composable
fun HomeScreen(model: SisComeViewModel, wide: Boolean) {
    // @Composable describe la pantalla a partir del estado: Compose vuelve a ejecutar los bloques afectados.
    // weight entrega al menú el ancho restante; el resumen lateral solo existe en ventanas amplias.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        WelcomeHeader()
        model.availableTickets.firstOrNull()?.let { ActiveTicketCard(it) { model.openTickets(it.id) } }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) { MenuSection(model) }
                Column(Modifier.width(320.dp)) { SelectionSummaryPanel(model) }
            }
        } else MenuSection(model)
        RecentActivitySection(model)
    }
}

@Composable
fun WelcomeHeader() = PageTitle("Hola, Antonia", "Miércoles, 7 de octubre")

@Composable
fun ActiveTicketCard(ticket: Ticket, onQr: () -> Unit) {
    Surface(color = TicketGreen, contentColor = Color.White, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("ticket", tint = TicketSecondary)
                Text("Ticket disponible", style = MaterialTheme.typography.labelMedium, color = TicketSecondary)
            }
            // Título y contexto se leen juntos; la identidad queda secundaria a la acción del ticket actual.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tu ticket de ${ticket.line.service.label.lowercase()}", style = MaterialTheme.typography.titleLarge)
                Text("${dateLabel(ticket.line.date)} · ${ticket.line.service.hours}", style = MaterialTheme.typography.bodySmall, color = TicketSecondary)
                Text("${ticket.line.variant.name} · ${ticket.id}", style = MaterialTheme.typography.bodySmall, color = TicketSecondary)
            }
            Button(onClick = onQr, colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, contentColor = TicketGreen), shape = MaterialTheme.shapes.small) {
                Text("Mostrar código QR")
                Spacer(Modifier.width(8.dp))
                AppIcon("arrow")
            }
        }
    }
}

@Composable
fun MenuSection(model: SisComeViewModel) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // La proximidad agrupa encabezado y explicación; las fechas, filtros y contenido tienen límites propios.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Menú del comedor", style = MaterialTheme.typography.titleLarge)
            Text("Selecciona servicios y paga todo junto. Agregar no cobra ni emite tickets.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DateSelector(model)
        ServiceSelector(model)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("calendar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Consumo: ${dateLabel(model.date)} · ${model.service.hours}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("clock", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Venta hasta las ${model.service.saleEnd}", style = MaterialTheme.typography.bodySmall)
            }
            if (model.date == DemoCatalog.today) Text("Hora simulada: ${DemoCatalog.time}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
        val key = SelectionKey(model.date, model.service)
        val owned = model.owned(key)
        val pending = model.pending(key)
        // El orden da prioridad al ticket y a operaciones pendientes; nunca ofrece volver a comprar una clave bloqueada.
        when {
            owned != null -> OwnedTicketNotice(owned) { model.openTickets(owned.id) }
            pending != null -> {
                Notice("Hay una operación pendiente", "${pending.reference}. Tu selección se conserva. Consulta la misma referencia; no pagues otra vez.", pending = true) {
                    OutlinedButton(onClick = { model.show(Overlay.PaymentDetail(pending.reference)) }) { Text("Ver operación") }
                }
            }
            model.connection == Connection.LOADING || model.menuLoading -> MenuLoadingSkeleton()
            model.connection != Connection.ONLINE -> {
                Notice(if (model.connection == Connection.OFFLINE) "Datos guardados sin conexión" else "No se pudo consultar el menú",
                    "La selección se conserva; el catálogo no está comprobado recientemente. No puedes iniciar un pago nuevo. Los tickets emitidos siguen accesibles.",
                    pending = model.connection == Connection.OFFLINE, error = model.connection == Connection.SERVER) {
                    Button(onClick = { model.connect(ConnectionFixture.NORMAL) }) { Text("Reintentar consulta") }
                    TextButton(onClick = { model.navigate(Destination.TICKETS) }) { Text("Ver mis tickets") }
                }
            }
            !DemoCatalog.published(model.date) -> EmptyState("Sin publicación", "No hay menú publicado para esta fecha. Elige una fecha con publicación.")
            DemoCatalog.closed(key) -> SaleClosedNotice(model.service,
                if (DemoCatalog.published("2026-10-08")) ({ model.selectDate("2026-10-08") }) else null)
            model.unavailable(key) -> {
                Notice("Publicado, pero agotado en esta demo", "No se inició un pago para esta clave. Revisa y quita la línea afectada; el resto de la selección se conserva.", error = true) {
                    if (model.records.lines.any { it.key == key }) OutlinedButton(onClick = { model.remove(key) }) { Text("Quitar línea agotada") }
                }
            }
            else -> ContentArrival("${model.date}|${model.service}") {
                DemoCatalog.variants(model.service).forEach { variant ->
                    MenuVariantCard(variant, model.service.cents,
                        model.records.lines.any { it.key == key && it.variant.id == variant.id }) { model.toggle(variant.id) }
                }
                AvailabilityNotice(model.service)
            }
        }
    }
    }
}

@Composable
fun DateSelector(model: SisComeViewModel) {
    // El desplazamiento horizontal conserva todas las fechas; rememberScrollState recuerda la posición local.
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
        modifier = Modifier.widthIn(min = 72.dp).heightIn(min = 48.dp).semantics {
            selected = active
            stateDescription = "$count servicios seleccionados"
        }) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // La insignia comparte la fila del día y reserva altura incluso vacía; no se superpone a texto ampliado.
            Row(Modifier.heightIn(min = 24.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(date.day, style = MaterialTheme.typography.labelMedium)
                if (count > 0) Surface(color = BrandYellow, contentColor = TicketGreen, shape = RoundedCornerShape(50)) {
                    Text(count.toString(), Modifier.padding(horizontal = 5.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall)
                } else Spacer(Modifier.width(20.dp))
            }
            Text(date.date.substring(8).toInt().toString(), style = MaterialTheme.typography.titleLarge)
            Text(if (date.published) "Octubre" else "Sin menú", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ServiceSelector(model: SisComeViewModel) {
    val fontScale = LocalDensity.current.fontScale
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon("utensils", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("Filtrar menú por servicio", style = MaterialTheme.typography.labelLarge)
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(13.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(6.dp)) {
                // Se calcula el ancho útil con la escala del usuario: si no cabe el texto, se apilan las tres opciones.
                val stacked = (maxWidth.value - 12f) / 3f / fontScale < 96f
                val options: @Composable (Modifier) -> Unit = { itemModifier ->
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
                        ServiceOptionCard(service, state, model.service == service, DemoCatalog.closed(key), itemModifier) { model.selectService(service) }
                    }
                }
                if (stacked) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { options(Modifier.fillMaxWidth()) }
                else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { options(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun ServiceOptionCard(service: Service, state: String, active: Boolean, closed: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit) {
    val color = when {
        active && closed -> MaterialTheme.colorScheme.surfaceVariant
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(onClick = onSelect, color = color, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.heightIn(min = 72.dp).semantics { selected = active }) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(service.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (state == "Ticket emitido" || state == "Seleccionado") AppIcon(if (state == "Ticket emitido") "ticket" else "check",
                    modifier = Modifier.size(16.dp), description = state)
            }
            Text(state, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MenuVariantCard(variant: Variant, cents: Int, active: Boolean, onToggle: () -> Unit) {
    val background by animateColorAsState(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tween(150), label = "Menu selection")
    Surface(onClick = onToggle, shape = RoundedCornerShape(12.dp),
        color = background,
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics {
            selected = active
            stateDescription = if (active) "Seleccionado. Tocar para quitar" else "Tocar para agregar o reemplazar la variante del servicio"
        }) {
        Column {
            Box {
                // El recorte sigue la forma exterior: el orden de tamaño y clip evita esquinas de foto rectas.
                MenuPhoto(variant.imageUrl, Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)))
                androidx.compose.animation.AnimatedVisibility(active, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    enter = fadeIn(tween(150)) + scaleIn(tween(150)), exit = fadeOut(tween(100))) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50)) {
                        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { AppIcon("check") }
                    }
                }
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(variant.name, style = MaterialTheme.typography.titleMedium)
            Text(variant.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // SpaceBetween separa precio y acción; weight deja al precio ceder ancho con texto ampliado.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(price(cents), modifier = Modifier.weight(1f).padding(end = 8.dp),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                OutlinedButton(onClick = onToggle, modifier = Modifier.heightIn(min = 48.dp).semantics { selected = active },
                    shape = RoundedCornerShape(50), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    AppIcon(if (active) "check" else "plus", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (active) "Agregado" else "Agregar")
                }
            }
            }
        }
    }
}

@Composable
fun OwnedTicketNotice(ticket: Ticket, onQr: () -> Unit) {
    // El estado poseído reúne la identidad y su acción; no es una variante seleccionable del menú.
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("ticket")
                Text("Ya tienes este ticket", style = MaterialTheme.typography.labelLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${ticket.line.service.label} · ${dateLabel(ticket.line.date)}", style = MaterialTheme.typography.titleMedium)
                Text("${ticket.id} · ${ticket.line.service.hours}. Solo se permite uno por servicio y fecha.", style = MaterialTheme.typography.bodySmall)
            }
            if (ticket.status == TicketStatus.AVAILABLE) OutlinedButton(onClick = onQr) { Text("Ver QR") }
            else Text(ticket.status.label)
        }
    }
}

@Composable
fun SaleClosedNotice(service: Service, onTomorrow: (() -> Unit)?) {
    // El cierre es informativo y neutral; solo ofrece mañana cuando esa publicación existe en el catálogo.
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("clock")
                Text("Venta cerrada", style = MaterialTheme.typography.labelLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("La venta de ${service.label.lowercase()} terminó a las ${service.saleEnd}", style = MaterialTheme.typography.titleMedium)
                Text("Son las ${DemoCatalog.time} en esta demo. Puedes elegir otra fecha publicada.", style = MaterialTheme.typography.bodySmall)
            }
            if (onTomorrow != null) OutlinedButton(onClick = onTomorrow) { Text("Ver publicación de mañana") }
        }
    }
}

@Composable
fun AvailabilityNotice(service: Service) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIcon("check", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Disponible para seleccionar · Venta hasta ${service.saleEnd}", style = MaterialTheme.typography.labelMedium)
            Text("Publicación, precio y disponibilidad se comprueban antes del pago. No se reserva cupo.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MenuLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Consultando menú publicado…", style = MaterialTheme.typography.bodyMedium)
        repeat(2) {
            ShimmerBlock(Modifier.fillMaxWidth().height(170.dp))
        }
    }
}

@Composable
fun RecentActivitySection(model: SisComeViewModel) {
    // La actividad usa el ticket almacenado, no la variante de la última compra ni el carrito actual.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Actividad reciente", style = MaterialTheme.typography.titleLarge)
                Text("Tus tickets más recientes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { model.navigate(Destination.TICKETS) }) { Text("Ver historial") }
        }
        if (model.records.tickets.isEmpty()) EmptyState("Aún no hay tickets", "Los tickets emitidos aparecerán aquí.")
        model.records.tickets.sortedByDescending { it.issuedAt }.take(3).forEach { ticket ->
            // Un separador delimita registros; no hace falta una tarjeta pesada por cada fila del historial breve.
            HorizontalDivider()
            Surface(onClick = { model.openTickets(ticket.id) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${ticket.line.service.label} · ${ticket.line.variant.name}", style = MaterialTheme.typography.titleSmall)
                            Text("Ticket ${ticket.id}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(price(ticket.line.cents), style = MaterialTheme.typography.titleSmall)
                    }
                    Text("Fecha de consumo · ${dateLabel(ticket.line.date)}", style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(ticket.status.label, neutral = ticket.status != TicketStatus.AVAILABLE)
                        Text("Ver detalle", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
