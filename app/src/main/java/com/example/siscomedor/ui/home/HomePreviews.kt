package com.example.siscomedor.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.DemoSession
import com.example.siscomedor.domain.Service
import com.example.siscomedor.ui.Connection
import com.example.siscomedor.ui.selection.SelectionActions
import com.example.siscomedor.ui.selection.SelectionSummaryContent
import com.example.siscomedor.ui.selection.SelectionUiState
import com.example.siscomedor.ui.theme.SisComeTheme

// Vistas previas de Inicio. Usan DemoSession (Kotlin puro): sin red, sin ViewModel.
// En Interactive Mode se puede tocar una fecha, un servicio o una variante y la selección cambia.

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.background) { content() }
    }
}

// Sesión de demo con una línea ya agregada, para que el contador de fechas y el resumen tengan contenido.
private fun previewSession(): DemoSession {
    val session = DemoSession(includePaymentHistory = true)
    session.toggle(DemoCatalog.line("2026-10-08", Service.LUNCH, "vegetarian"))
    return session
}

@Composable
private fun InteractiveHome(
    wide: Boolean,
    connection: Connection = Connection.ONLINE,
    startDate: String = "2026-10-08",
    startService: Service = Service.BREAKFAST
) {
    val session = remember { previewSession() }
    var date by remember { mutableStateOf(startDate) }
    var service by remember { mutableStateOf(startService) }
    // DemoSession no es estado de Compose: se copian sus líneas a un estado local para redibujar el preview.
    var lines by remember { mutableStateOf(session.lines) }
    val state = buildHomeUiState(
        date = date,
        service = service,
        connection = connection,
        menuLoading = false,
        lines = lines,
        tickets = session.tickets,
        hasNextAvailable = true,
        owned = session::owned,
        pending = session::pendingFor,
        unavailable = session::unavailable
    )
    val actions = HomeActions(
        onSelectDate = { date = it },
        onSelectService = { service = it },
        onToggleVariant = { variantId ->
            session.toggle(DemoCatalog.line(date, service, variantId))
            lines = session.lines
        },
        onRemoveLine = { key ->
            session.remove(key)
            lines = session.lines
        },
        onShowNextAvailable = {
            date = "2026-10-08"
            service = Service.BREAKFAST
        }
    )
    val selectionActions = SelectionActions(onRemove = { key ->
        session.remove(key)
        lines = session.lines
    })
    HomeContent(state, wide, actions, sidePanel = {
        SelectionSummaryContent(
            SelectionUiState(lines, emptySet(), emptySet(), DemoCatalog.wallet, canCheckout = true),
            selectionActions
        )
    })
}

@Preview(name = "Inicio · teléfono", showBackground = true, widthDp = 400, heightDp = 2000)
@Composable
private fun HomePhonePreview() {
    PreviewFrame { InteractiveHome(wide = false) }
}

@Preview(name = "Inicio · ventana amplia", showBackground = true, widthDp = 900, heightDp = 1600)
@Composable
private fun HomeWidePreview() {
    PreviewFrame { InteractiveHome(wide = true) }
}

@Preview(name = "Inicio · sin conexión", showBackground = true, widthDp = 400, heightDp = 1400)
@Composable
private fun HomeOfflinePreview() {
    PreviewFrame { InteractiveHome(wide = false, connection = Connection.OFFLINE) }
}

@Preview(name = "Inicio · error del servidor", showBackground = true, widthDp = 400, heightDp = 1400)
@Composable
private fun HomeServerErrorPreview() {
    PreviewFrame { InteractiveHome(wide = false, connection = Connection.SERVER) }
}

@Preview(name = "Inicio · ticket ya comprado", showBackground = true, widthDp = 400, heightDp = 1400)
@Composable
private fun HomeOwnedTicketPreview() {
    // Cena de hoy ya tiene ticket: se ve "Ya tienes este ticket" y el atajo al próximo menú disponible.
    PreviewFrame { InteractiveHome(wide = false, startDate = DemoCatalog.today, startService = Service.DINNER) }
}

@Preview(name = "MenuVariantCard · sin seleccionar y seleccionada", showBackground = true, widthDp = 360)
@Composable
private fun MenuVariantCardPreview() {
    val variants = DemoCatalog.variants(Service.LUNCH)
    var selectedId by remember { mutableStateOf("vegetarian") }
    PreviewFrame {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            variants.forEach { variant ->
                MenuVariantCard(variant, Service.LUNCH.cents, selectedId == variant.id) { selectedId = variant.id }
            }
        }
    }
}

@Preview(name = "DateOptionItem", showBackground = true, widthDp = 420)
@Composable
private fun DateOptionItemPreview() {
    var selected by remember { mutableStateOf(DemoCatalog.today) }
    PreviewFrame {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoCatalog.dates.take(3).forEachIndexed { index, date ->
                DateOptionItem(date, selected == date.date, count = index) { selected = date.date }
            }
        }
    }
}
