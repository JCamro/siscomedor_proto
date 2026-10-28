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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.siscomedor.ui.checkout.CheckoutDialog
import com.example.siscomedor.ui.components.*
import com.example.siscomedor.ui.home.HomeScreen
import com.example.siscomedor.ui.login.LoginScreen
import com.example.siscomedor.ui.navigation.NavItem
import com.example.siscomedor.ui.navigation.Route
import com.example.siscomedor.ui.navigation.navItems
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.siscomedor.ui.payments.*
import com.example.siscomedor.ui.selection.*
import com.example.siscomedor.ui.tickets.*
import com.example.siscomedor.ui.theme.BrandYellow
import com.example.siscomedor.ui.theme.TicketGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SisComeApp(model: SisComeViewModel) {
    // rememberSaveable guarda la expansión informativa; las compras viven en el ViewModel.
    var disclosureOpen by rememberSaveable { mutableStateOf(false) }
    // NavController guarda la pila de pantallas; currentBackStackEntryAsState avisa cuál está arriba.
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onLogin = currentRoute == null || currentRoute == Route.Login.route
    // Back solo cierra superposiciones; la pila de pantallas la maneja NavHost.
    BackHandler(enabled = model.overlay != null) { model.closeOverlay() }
    // Cambio de pestaña: vuelve a Inicio guardando estado, sin duplicar destinos (patrón recomendado).
    val goToTab: (Route) -> Unit = { target ->
        if (model.prepareNavigation()) navController.navigate(target.route) {
            popUpTo(Route.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // La adaptación depende de la ventana, no del modelo del dispositivo; funciona también en pantalla dividida.
        val wide = maxWidth >= 840.dp
        Row(Modifier.fillMaxSize()) {
            if (wide && !onLogin) AppNavigationRail(currentRoute, !model.processing, goToTab)
            Scaffold(modifier = Modifier.weight(1f),
                topBar = {
                    if (!onLogin) TopAppBar(title = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = BrandYellow, contentColor = TicketGreen, shape = MaterialTheme.shapes.extraSmall) {
                                Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Text("S", style = MaterialTheme.typography.titleLarge) }
                            }
                            Text("SisCome", style = MaterialTheme.typography.titleLarge)
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                        actions = { TextButton(onClick = { model.show(Overlay.Connectivity) }, enabled = !model.processing,
                            modifier = Modifier.widthIn(max = 140.dp).semantics { contentDescription = "Conexión: ${model.connection.label}. Abrir simulador" }) {
                            // Una sola línea: con fuente grande el texto se recorta con "…" en vez de salirse de la barra.
                            Text(model.connection.label, style = MaterialTheme.typography.labelMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } })
                },
                bottomBar = {
                    // Carrito y navegación ocupan la misma zona inferior de Scaffold, sin superponerse.
                    if (!wide && !onLogin) Column {
                        if (currentRoute == Route.Home.route && model.records.lines.isNotEmpty()) SelectionBar(model)
                        AppNavigationBar(currentRoute, !model.processing, goToTab)
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
                    if (!onLogin) ConnectivityStatus(model)
                    // NavHost reemplaza el antiguo "when": cada ruta de texto dibuja su pantalla.
                    NavHost(navController, startDestination = Route.Login.route, modifier = Modifier.weight(1f)) {
                        composable(Route.Login.route) {
                            // Al ingresar se borra el login de la pila: Back desde Inicio no vuelve al formulario.
                            LoginScreen(onLoginSuccess = {
                                navController.navigate(Route.Home.route) { popUpTo(Route.Login.route) { inclusive = true } }
                            })
                        }
                        composable(Route.Home.route) { HomeScreen(model, wide, onShowTickets = { goToTab(Route.Tickets) }) }
                        composable(Route.Tickets.route) {
                            MyTicketsScreen(model, onOpenTicket = { id ->
                                if (model.prepareNavigation()) navController.navigate(Route.TicketDetail.create(id))
                            })
                        }
                        composable(Route.TicketDetail.route,
                            arguments = listOf(navArgument(Route.TicketDetail.ARG) { type = NavType.StringType })) { entry ->
                            val ticketId = entry.arguments?.getString(Route.TicketDetail.ARG).orEmpty()
                            TicketDetailScreen(ticketId, model, onBack = { navController.popBackStack() })
                        }
                        composable(Route.Payments.route) { PaymentsScreen(model) }
                        composable(Route.Wallets.route) { UnimplementedScreen("Billeteras", "Las cuentas disponibles son solo opciones de demostración para el pago. La vinculación, autorización y gestión de proveedores no están implementadas. No ingreses credenciales.") }
                        composable(Route.Account.route) { UnimplementedScreen("Mi cuenta", "Antonia Muñoz es una identidad ficticia. No hay autenticación, edición de perfil ni políticas institucionales de datos implementadas.") }
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
// El detalle "tickets/{ticketId}" mantiene marcada la pestaña Mis tickets.
private fun isSelected(item: NavItem, currentRoute: String?) =
    currentRoute == item.route.route || (item.route == Route.Tickets && currentRoute == Route.TicketDetail.route)

@Composable
fun AppNavigationBar(currentRoute: String?, enabled: Boolean, onNavigate: (Route) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        navItems.forEach { item ->
            NavigationBarItem(selected = isSelected(item, currentRoute),
                onClick = { onNavigate(item.route) }, enabled = enabled,
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary),
                icon = { AppIcon(item.icon) }, label = { Text(item.label, style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
fun AppNavigationRail(currentRoute: String?, enabled: Boolean, onNavigate: (Route) -> Unit) {
    NavigationRail(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.safeDrawingPadding(), header = { Text("SisCome", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp)) }) {
        navItems.forEach { item ->
            NavigationRailItem(selected = isSelected(item, currentRoute),
                onClick = { onNavigate(item.route) }, enabled = enabled,
                icon = { AppIcon(item.icon) }, label = { Text(item.label) })
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
