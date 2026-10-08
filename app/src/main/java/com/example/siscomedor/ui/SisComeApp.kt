package com.example.siscomedor.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.siscomedor.ui.checkout.CheckoutDialog
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.home.HomeScreen
import com.example.siscomedor.ui.payments.*
import com.example.siscomedor.ui.selection.*
import com.example.siscomedor.ui.tickets.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SisComeApp(model: SisComeViewModel) {
    BackHandler(enabled = model.overlay != null || model.destination != Destination.HOME) {
        if (model.overlay != null) model.closeOverlay() else model.navigate(Destination.HOME)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        Row(Modifier.fillMaxSize()) {
            if (wide) AppNavigationRail(model)
            Scaffold(modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(title = { Text("SisCome", style = MaterialTheme.typography.titleLarge) },
                        actions = { TextButton(onClick = { model.show(Overlay.Connectivity) }, enabled = !model.processing) { Text(model.connection.label) } })
                },
                bottomBar = {
                    if (!wide) Column {
                        if (model.destination == Destination.HOME && model.records.lines.isNotEmpty()) SelectionBar(model)
                        AppNavigationBar(model)
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                    ConnectivityStatus(model)
                    when (model.destination) {
                        Destination.HOME -> HomeScreen(model, wide)
                        Destination.TICKETS -> MyTicketsScreen(model)
                        Destination.PAYMENTS -> PaymentsScreen(model)
                        Destination.WALLETS -> UnimplementedScreen("Billeteras", "Solo se usa Wallet Demo ···· 2841, una billetera ficticia. La vinculación, autorización y gestión de proveedores no están implementadas. No ingreses credenciales.")
                        Destination.ACCOUNT -> UnimplementedScreen("Mi cuenta", "Antonia Muñoz es una identidad ficticia. No hay autenticación, edición de perfil ni políticas institucionales de datos implementadas.")
                    }
                }
            }
        }
    }
    when (val overlay = model.overlay) {
        Overlay.Selection -> SelectionSheet(model)
        Overlay.Checkout -> CheckoutDialog(model)
        is Overlay.TicketPicker -> TicketPickerDialog(model, overlay.operationReference)
        is Overlay.TicketDetail -> model.records.tickets.firstOrNull { it.id == overlay.id }?.let {
            TicketDetailDialog(it, model, overlay.operationReference)
        }
        is Overlay.PaymentDetail -> model.records.operations.firstOrNull { it.reference == overlay.reference }?.let { PaymentDetailDialog(it, model) }
        Overlay.Connectivity -> ConnectivityDialog(model)
        null -> Unit
    }
}

@Composable
fun AppNavigationBar(model: SisComeViewModel) {
    NavigationBar {
        Destination.entries.forEach { destination ->
            NavigationBarItem(selected = model.destination == destination,
                onClick = { model.navigate(destination) }, enabled = !model.processing,
                icon = { AppIcon(destination.icon) }, label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
fun AppNavigationRail(model: SisComeViewModel) {
    NavigationRail(modifier = Modifier.safeDrawingPadding(), header = { Text("SisCome", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp)) }) {
        Destination.entries.forEach { destination ->
            NavigationRailItem(selected = model.destination == destination,
                onClick = { model.navigate(destination) }, enabled = !model.processing,
                icon = { AppIcon(destination.icon) }, label = { Text(destination.label) })
        }
    }
}

@Composable
fun ConnectivityStatus(model: SisComeViewModel) {
    if (model.connection != Connection.ONLINE) Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(when (model.connection) {
                Connection.LOADING -> "Consultando estado de conexión… Se conserva tu selección."
                Connection.OFFLINE -> "Sin conexión: no puedes iniciar un pago nuevo. Tickets guardados disponibles, sin validación reciente."
                else -> "Consulta del servicio fallida: no puedes iniciar un pago nuevo. Conservamos selección y operaciones previas."
            }, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ConnectivityDialog(model: SisComeViewModel) {
    AppDialog("Simular conexión", model::closeOverlay) {
        Text("Fixtures locales. El estado sin internet del dispositivo también bloquea pagos nuevos.")
        ConnectionFixture.entries.forEach { fixture ->
            FilterChip(selected = model.fixture == fixture, onClick = { model.connect(fixture); model.closeOverlay() }, label = { Text(fixture.label) })
        }
    }
}

@Composable
fun UnimplementedScreen(title: String, description: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        PageTitle(title, "Área no implementada en este prototipo")
        Notice("Sin integración real", description)
        DemoNotice()
    }
}
