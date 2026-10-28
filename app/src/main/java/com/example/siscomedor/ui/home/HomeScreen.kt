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

// Todo lo que Inicio necesita mostrar, como valores simples. El contenedor lo arma desde el ViewModel
// y las vistas previas lo arman desde DemoSession; la pantalla no sabe de dónde viene.
data class HomeUiState(
    val date: String,
    val service: Service,
    val connection: Connection,
    val menuLoading: Boolean,
    val lines: List<SelectionLine>,
    val tickets: List<Ticket>,
    val ownedTickets: Map<SelectionKey, Ticket>,
    val pendingOperations: Map<SelectionKey, PaymentOperation>,
    val unavailableKeys: Set<SelectionKey>,
    val hasNextAvailable: Boolean
) {
    val activeTicket: Ticket? get() = tickets.firstOrNull { it.status == TicketStatus.AVAILABLE }
}

// Eventos que Inicio envía hacia arriba. Cada valor por defecto no hace nada, útil en las vistas previas.
class HomeActions(
    val onOpenTicket: (String) -> Unit = {},
    val onSelectDate: (String) -> Unit = {},
    val onSelectService: (Service) -> Unit = {},
    val onToggleVariant: (String) -> Unit = {},
    val onRemoveLine: (SelectionKey) -> Unit = {},
    val onShowPayment: (String) -> Unit = {},
    val onRetryConnection: () -> Unit = {},
    val onShowTickets: () -> Unit = {},
    val onShowNextAvailable: () -> Unit = {}
)

// Recorre cada fecha y servicio del catálogo una sola vez y guarda qué claves están bloqueadas.
// Recibe las reglas como funciones para reutilizar las del dominio en lugar de copiarlas.
fun buildHomeUiState(
    date: String,
    service: Service,
    connection: Connection,
    menuLoading: Boolean,
    lines: List<SelectionLine>,
    tickets: List<Ticket>,
    hasNextAvailable: Boolean,
    owned: (SelectionKey) -> Ticket?,
    pending: (SelectionKey) -> PaymentOperation?,
    unavailable: (SelectionKey) -> Boolean
): HomeUiState {
    val ownedTickets = mutableMapOf<SelectionKey, Ticket>()
    val pendingOperations = mutableMapOf<SelectionKey, PaymentOperation>()
    val unavailableKeys = mutableSetOf<SelectionKey>()
    for (publishedDate in DemoCatalog.dates) {
        for (option in Service.entries) {
            val key = SelectionKey(publishedDate.date, option)
            val ticket = owned(key)
            if (ticket != null) ownedTickets[key] = ticket
            val operation = pending(key)
            if (operation != null) pendingOperations[key] = operation
            if (unavailable(key)) unavailableKeys.add(key)
        }
    }
    return HomeUiState(date, service, connection, menuLoading, lines, tickets,
        ownedTickets, pendingOperations, unavailableKeys, hasNextAvailable)
}

@Composable
fun HomeScreen(model: SisComeViewModel, wide: Boolean, onShowTickets: () -> Unit) {
    // onShowTickets llega desde NavHost: la pantalla no conoce el NavController.
    // Contenedor ("props down, events up", como en React): solo lee el ViewModel y conecta acciones.
    // HomeContent no conoce el ViewModel; recibe valores y lambdas, por eso se puede abrir en Preview.
    val records = model.records
    val state = buildHomeUiState(
        date = model.date,
        service = model.service,
        connection = model.connection,
        menuLoading = model.menuLoading,
        lines = records.lines,
        tickets = records.tickets,
        hasNextAvailable = model.hasNextAvailable,
        owned = model::owned,
        pending = model::pending,
        unavailable = model::unavailable
    )
    val actions = HomeActions(
        onOpenTicket = { ticketId -> model.openTickets(ticketId) },
        onSelectDate = model::selectDate,
        onSelectService = model::selectService,
        onToggleVariant = model::toggle,
        onRemoveLine = model::remove,
        onShowPayment = { reference -> model.show(Overlay.PaymentDetail(reference)) },
        onRetryConnection = { model.connect(ConnectionFixture.NORMAL) },
        onShowTickets = onShowTickets,
        onShowNextAvailable = model::showNextAvailable
    )
    HomeContent(state, wide, actions, sidePanel = { SelectionSummaryPanel(model) })
}

@Composable
fun HomeContent(state: HomeUiState, wide: Boolean, actions: HomeActions, sidePanel: @Composable () -> Unit = {}) {
    // @Composable describe la pantalla a partir del estado: Compose vuelve a ejecutar los bloques afectados.
    // weight entrega al menú el ancho restante; el resumen lateral solo existe en ventanas amplias.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        WelcomeHeader()
        val activeTicket = state.activeTicket
        if (activeTicket != null) ActiveTicketCard(activeTicket) { actions.onOpenTicket(activeTicket.id) }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) { MenuSection(state, actions) }
                Column(Modifier.width(320.dp)) { sidePanel() }
            }
        } else MenuSection(state, actions)
        RecentActivitySection(state.tickets, actions.onOpenTicket, actions.onShowTickets)
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
fun MenuSection(state: HomeUiState, actions: HomeActions) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // La proximidad agrupa encabezado y explicación; las fechas, filtros y contenido tienen límites propios.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Menú del comedor", style = MaterialTheme.typography.titleLarge)
            Text("Selecciona servicios y paga todo junto. Agregar no cobra ni emite tickets.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DateSelector(state.date, state.lines, actions.onSelectDate)
        ServiceSelector(state, actions.onSelectService)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("calendar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Consumo: ${dateLabel(state.date)} · ${state.service.hours}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon("clock", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Venta hasta las ${state.service.saleEnd}", style = MaterialTheme.typography.bodySmall)
            }
            if (state.date == DemoCatalog.today) Text("Hora simulada: ${DemoCatalog.time}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
        val key = SelectionKey(state.date, state.service)
        val owned = state.ownedTickets[key]
        val pending = state.pendingOperations[key]
        // El orden da prioridad al ticket y a operaciones pendientes; nunca ofrece volver a comprar una clave bloqueada.
        when {
            owned != null -> OwnedTicketNotice(owned, onQr = { actions.onOpenTicket(owned.id) },
                onShowNext = if (state.hasNextAvailable) actions.onShowNextAvailable else null)
            pending != null -> {
                Notice("Hay una operación pendiente", "${pending.reference}. Tu selección se conserva. Consulta la misma referencia; no pagues otra vez.", pending = true) {
                    OutlinedButton(onClick = { actions.onShowPayment(pending.reference) }) { Text("Ver operación") }
                }
            }
            state.connection == Connection.LOADING || state.menuLoading -> MenuLoadingSkeleton()
            state.connection != Connection.ONLINE -> {
                Notice(if (state.connection == Connection.OFFLINE) "Datos guardados sin conexión" else "No se pudo consultar el menú",
                    "La selección se conserva; el catálogo no está comprobado recientemente. No puedes iniciar un pago nuevo. Los tickets emitidos siguen accesibles.",
                    pending = state.connection == Connection.OFFLINE, error = state.connection == Connection.SERVER) {
                    Button(onClick = actions.onRetryConnection) { Text("Reintentar consulta") }
                    TextButton(onClick = actions.onShowTickets) { Text("Ver mis tickets") }
                }
            }
            !DemoCatalog.published(state.date) -> EmptyState("Sin publicación", "No hay menú publicado para esta fecha. Elige una fecha con publicación.")
            DemoCatalog.closed(key) -> SaleClosedNotice(state.service,
                if (DemoCatalog.published("2026-10-08")) ({ actions.onSelectDate("2026-10-08") }) else null)
            key in state.unavailableKeys -> {
                Notice("Publicado, pero agotado en esta demo", "No se inició un pago para esta clave. Revisa y quita la línea afectada; el resto de la selección se conserva.", error = true) {
                    if (state.lines.any { it.key == key }) OutlinedButton(onClick = { actions.onRemoveLine(key) }) { Text("Quitar línea agotada") }
                }
            }
            else -> ContentArrival("${state.date}|${state.service}") {
                DemoCatalog.variants(state.service).forEach { variant ->
                    MenuVariantCard(variant, state.service.cents,
                        state.lines.any { it.key == key && it.variant.id == variant.id }) { actions.onToggleVariant(variant.id) }
                }
                AvailabilityNotice(state.service)
            }
        }
    }
    }
}

@Composable
fun DateSelector(selectedDate: String, lines: List<SelectionLine>, onSelect: (String) -> Unit) {
    // El desplazamiento horizontal conserva todas las fechas; rememberScrollState recuerda la posición local.
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DemoCatalog.dates.forEach { date ->
            DateOptionItem(date, selectedDate == date.date, lines.count { it.date == date.date }) { onSelect(date.date) }
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
            stateDescription = plural(count, "servicio seleccionado", "servicios seleccionados")
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
fun ServiceSelector(state: HomeUiState, onSelect: (Service) -> Unit) {
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
                        val key = SelectionKey(state.date, service)
                        val label = when {
                            key in state.ownedTickets -> "Ticket emitido"
                            key in state.pendingOperations -> "Por resolver"
                            DemoCatalog.closed(key) -> "Venta cerrada"
                            key in state.unavailableKeys -> "Agotado"
                            state.lines.any { it.key == key } -> "Seleccionado"
                            else -> service.hours
                        }
                        ServiceOptionCard(service, label, state.service == service, DemoCatalog.closed(key), itemModifier) { onSelect(service) }
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
fun OwnedTicketNotice(ticket: Ticket, onQr: () -> Unit, onShowNext: (() -> Unit)? = null) {
    // El estado poseído reúne la identidad y su acción; no es una variante seleccionable del menú.
    // onShowNext es opcional: si no queda otra fecha/servicio por comprar, no se muestra el atajo.
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
            if (onShowNext != null) TextButton(onClick = onShowNext) { Text("Ver próximo menú disponible") }
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
fun RecentActivitySection(tickets: List<Ticket>, onOpenTicket: (String) -> Unit, onShowHistory: () -> Unit) {
    // La actividad usa el ticket almacenado, no la variante de la última compra ni el carrito actual.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Actividad reciente", style = MaterialTheme.typography.titleLarge)
                Text("Tus tickets más recientes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onShowHistory) { Text("Ver historial") }
        }
        if (tickets.isEmpty()) EmptyState("Aún no hay tickets", "Los tickets emitidos aparecerán aquí.")
        tickets.sortedByDescending { it.issuedAt }.take(3).forEach { ticket ->
            // Un separador delimita registros; no hace falta una tarjeta pesada por cada fila del historial breve.
            HorizontalDivider()
            Surface(onClick = { onOpenTicket(ticket.id) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
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
