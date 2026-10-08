package com.example.siscomedor.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.siscomedor.ui.checkout.CheckoutDialog
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.home.HomeScreen
import com.example.siscomedor.ui.payments.*
import com.example.siscomedor.ui.selection.*
import com.example.siscomedor.ui.tickets.*
import com.example.siscomedor.ui.theme.BrandYellow
import com.example.siscomedor.ui.theme.TicketGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SisComeApp(model: SisComeViewModel) {
    // rememberSaveable guarda la expansión informativa; compras y navegación se mantienen en el ViewModel.
    var disclosureOpen by rememberSaveable { mutableStateOf(false) }
    // Back cierra primero la superposición. El ViewModel rechaza el cierre durante procesamiento.
    BackHandler(enabled = model.overlay != null || model.destination != Destination.HOME) {
        if (model.overlay != null) model.closeOverlay() else model.navigate(Destination.HOME)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // La adaptación depende de la ventana, no del modelo del dispositivo; funciona también en pantalla dividida.
        val wide = maxWidth >= 840.dp
        Row(Modifier.fillMaxSize()) {
            if (wide) AppNavigationRail(model)
            Scaffold(modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(title = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = BrandYellow, contentColor = TicketGreen, shape = MaterialTheme.shapes.extraSmall) {
                                Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Text("S", style = MaterialTheme.typography.titleLarge) }
                            }
                            Text("SisCome", style = MaterialTheme.typography.titleLarge)
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                        actions = { TextButton(onClick = { model.show(Overlay.Connectivity) }, enabled = !model.processing,
                            modifier = Modifier.widthIn(max = 140.dp).semantics { contentDescription = "Conexión: ${model.connection.label}. Abrir simulador" }) {
                            Text(model.connection.label, style = MaterialTheme.typography.labelMedium)
                        } })
                },
                bottomBar = {
                    // Carrito y navegación ocupan la misma zona inferior de Scaffold, sin superponerse.
                    if (!wide) Column {
                        if (model.destination == Destination.HOME && model.records.lines.isNotEmpty()) SelectionBar(model)
                        AppNavigationBar(model)
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                // Primero se aplica el espacio reservado; consumirlo evita que hijos sumen otra vez los mismos insets.
                Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                    TextButton(onClick = { disclosureOpen = !disclosureOpen }, modifier = Modifier.fillMaxWidth()) {
                        AppIcon("info")
                        Spacer(Modifier.width(8.dp))
                        Text("Prototipo · Sin dinero real", style = MaterialTheme.typography.labelMedium)
                    }
                    if (disclosureOpen) Text("Identidad, billetera, pagos y tickets de demostración. Los códigos no tienen validez institucional. El estado se pierde al cerrar el proceso.",
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
                    ConnectivityStatus(model)
                    when (model.destination) {
                        Destination.HOME -> HomeScreen(model, wide)
                        Destination.TICKETS -> MyTicketsScreen(model)
                        Destination.PAYMENTS -> PaymentsScreen(model)
                        Destination.WALLETS -> UnimplementedScreen("Billeteras", "Las cuentas disponibles son solo opciones de demostración para el pago. La vinculación, autorización y gestión de proveedores no están implementadas. No ingreses credenciales.")
                        Destination.ACCOUNT -> UnimplementedScreen("Mi cuenta", "Antonia Muñoz es una identidad ficticia. No hay autenticación, edición de perfil ni políticas institucionales de datos implementadas.")
                    }
                }
            }
        }
    }
    // El renderizado depende del estado: los diálogos se montan encima del destino sin cambiarlo silenciosamente.
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        Destination.entries.forEach { destination ->
            NavigationBarItem(selected = model.destination == destination,
                onClick = { model.navigate(destination) }, enabled = !model.processing,
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary),
                icon = { AppIcon(destination.icon) }, label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
fun AppNavigationRail(model: SisComeViewModel) {
    NavigationRail(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.safeDrawingPadding(), header = { Text("SisCome", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp)) }) {
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
        // La limitación específica basta aquí; el aviso global ya explica el entorno de demostración.
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
