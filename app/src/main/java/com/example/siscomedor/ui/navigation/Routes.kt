package com.example.siscomedor.ui.navigation

// Rutas de texto para NavHost. sealed class limita las rutas a esta lista conocida.
sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Home : Route("home")
    data object Tickets : Route("tickets")
    data object Payments : Route("payments")
    data object Wallets : Route("wallets")
    data object Account : Route("account")

    // Detalle con argumento: "tickets/{ticketId}" es el patrón; create arma la ruta real.
    data object TicketDetail : Route("tickets/{ticketId}") {
        const val ARG = "ticketId"
        fun create(ticketId: String) = "tickets/$ticketId"
    }
}

// Datos de cada pestaña de la barra inferior: ruta, texto e icono.
data class NavItem(val route: Route, val label: String, val icon: String)

val navItems = listOf(
    NavItem(Route.Home, "Inicio", "home"),
    NavItem(Route.Tickets, "Mis tickets", "ticket"),
    NavItem(Route.Payments, "Pagos", "card"),
    NavItem(Route.Wallets, "Billeteras", "wallet"),
    NavItem(Route.Account, "Mi cuenta", "user")
)
