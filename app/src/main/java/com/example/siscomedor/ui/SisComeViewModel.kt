package com.example.siscomedor.ui

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Destination(val label: String, val icon: String) {
    HOME("Inicio", "home"), TICKETS("Mis tickets", "ticket"), PAYMENTS("Pagos", "card"),
    WALLETS("Billeteras", "wallet"), ACCOUNT("Mi cuenta", "user")
}
enum class Connection(val label: String) {
    LOADING("Conectando…"), ONLINE("En línea"), OFFLINE("Sin conexión"), SERVER("Servicio no disponible")
}
enum class ConnectionFixture(val label: String) {
    NORMAL("Normal"), SLOW("Carga lenta"), OFFLINE("Sin conexión"), SERVER("Error del servidor")
}
sealed interface Overlay {
    data object Selection : Overlay
    data object Checkout : Overlay
    data class TicketPicker(val operationReference: String? = null) : Overlay
    data class TicketDetail(val id: String, val operationReference: String? = null) : Overlay
    data class PaymentDetail(val reference: String) : Overlay
    data object Connectivity : Overlay
}
data class SessionRecords(val lines: List<SelectionLine>, val tickets: List<Ticket>, val operations: List<PaymentOperation>)
data class CheckoutReview(val reference: String, val lines: List<SelectionLine>)

fun ticketOverlay(ticketIds: List<String>, operationReference: String? = null): Overlay =
    if (ticketIds.size == 1) Overlay.TicketDetail(ticketIds.single(), operationReference)
    else Overlay.TicketPicker(operationReference)

class SisComeViewModel(application: Application) : AndroidViewModel(application) {
    private val session = DemoSession(includePaymentHistory = true)
    var records by mutableStateOf(SessionRecords(session.lines, session.tickets, session.operations))
        private set
    var destination by mutableStateOf(Destination.HOME)
        private set
    var overlay by mutableStateOf<Overlay?>(null)
        private set
    var date by mutableStateOf(DemoCatalog.today)
        private set
    var service by mutableStateOf(Service.DINNER)
        private set
    var menuLoading by mutableStateOf(false)
        private set
    var connection by mutableStateOf(Connection.LOADING)
        private set
    var fixture by mutableStateOf(ConnectionFixture.NORMAL)
        private set
    var scenario by mutableStateOf(Scenario.SUCCESS)
        private set
    var review by mutableStateOf<CheckoutReview?>(null)
        private set
    var result by mutableStateOf<PaymentOperation?>(null)
        private set
    var processing by mutableStateOf(false)
        private set
    var querying by mutableStateOf(false)
        private set
    var stage by mutableIntStateOf(0)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var ticketFilter by mutableStateOf<TicketStatus?>(null)
    var paymentFilter by mutableStateOf<PaymentStatus?>(null)
    var qrFixture by mutableStateOf(QrRefreshFixture.SUCCESS)
        private set
    var qrStates by mutableStateOf<Map<String, QrRefreshState>>(emptyMap())
        private set
    private var referenceSequence = 78421
    private var menuJob: Job? = null
    private var connectionJob: Job? = null
    private val networkManager = application.getSystemService(ConnectivityManager::class.java)
    private var networkAvailable = hasNetwork()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateNetwork()
        override fun onLost(network: Network) = updateNetwork()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = updateNetwork()
    }

    init {
        networkManager.registerDefaultNetworkCallback(callback)
        connect(ConnectionFixture.NORMAL)
    }

    private fun hasNetwork(): Boolean = networkManager.getNetworkCapabilities(networkManager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

    private fun updateNetwork() {
        viewModelScope.launch {
            networkAvailable = hasNetwork()
            if (!networkAvailable) {
                connectionJob?.cancel()
                connection = Connection.OFFLINE
            } else connect(fixture)
        }
    }

    fun connect(target: ConnectionFixture) {
        fixture = target
        connectionJob?.cancel()
        connection = Connection.LOADING
        connectionJob = viewModelScope.launch {
            delay(if (target == ConnectionFixture.SLOW) 3200 else 1100)
            networkAvailable = hasNetwork()
            connection = when {
                !networkAvailable || target == ConnectionFixture.OFFLINE -> Connection.OFFLINE
                target == ConnectionFixture.SERVER -> Connection.SERVER
                else -> Connection.ONLINE
            }
        }
    }

    fun navigate(target: Destination) {
        if (processing) return
        overlay = null
        destination = target
    }

    fun show(target: Overlay) {
        if (!processing) overlay = target
    }

    fun closeOverlay() {
        if (!processing) overlay = null
    }

    fun selectDate(value: String) {
        date = value
        loadMenu()
    }

    fun selectService(value: Service) {
        service = value
        loadMenu()
    }

    private fun loadMenu() {
        menuJob?.cancel()
        menuLoading = true
        menuJob = viewModelScope.launch { delay(480); menuLoading = false }
    }

    fun toggle(variantId: String) {
        if (connection != Connection.ONLINE || menuLoading || processing) return
        session.toggle(DemoCatalog.line(date, service, variantId))
        refresh()
    }

    fun remove(key: SelectionKey) {
        if (!processing) { session.remove(key); refresh() }
    }

    fun clearSelection() {
        if (!processing) { session.clear(); refresh() }
    }

    fun owned(key: SelectionKey) = session.owned(key)
    fun pending(key: SelectionKey) = session.pendingFor(key)
    fun unavailable(key: SelectionKey) = session.unavailable(key)
    val canCheckout get() = !processing && connection == Connection.ONLINE && session.canSubmit(records.lines)
    val availableTickets get() = records.tickets.filter { it.status == TicketStatus.AVAILABLE }

    fun beginCheckout() {
        if (!canCheckout) return
        review = CheckoutReview("DEMO-${referenceSequence++}", records.lines.toList())
        result = null
        stage = 0
        notice = null
        scenario = Scenario.SUCCESS
        overlay = Overlay.Checkout
    }

    fun chooseScenario(value: Scenario) {
        if (!processing && result == null) scenario = value
    }

    fun confirm() {
        val frozen = review ?: return
        if (processing || result != null) return
        if (connection != Connection.ONLINE || !session.canSubmit(frozen.lines)) {
            notice = "No se inició un pago. La selección se conserva; revisa la conexión y el menú antes de continuar."
            return
        }
        val selectedScenario = scenario
        processing = true
        querying = false
        viewModelScope.launch {
            stage = 1
            delay(900)
            if (connection != Connection.ONLINE) {
                notice = "La conexión cambió antes del pago. No hubo cargo nuevo; la selección se conserva."
                processing = false
                stage = 0
                return@launch
            }
            if (selectedScenario != Scenario.SOLD_OUT) {
                stage = 2
                delay(900)
                if (selectedScenario == Scenario.SUCCESS || selectedScenario == Scenario.ISSUANCE) {
                    stage = 3
                    delay(900)
                }
            }
            result = session.submit(frozen.reference, frozen.lines,
                if (connection == Connection.ONLINE) selectedScenario else Scenario.UNKNOWN)
            refresh()
            processing = false
        }
    }

    fun query(reference: String) {
        val operation = records.operations.firstOrNull { it.reference == reference } ?: return
        if (processing || connection != Connection.ONLINE || !operation.unresolved) return
        review = CheckoutReview(reference, operation.lines)
        overlay = Overlay.Checkout
        result = null
        notice = null
        processing = true
        querying = true
        stage = 1
        viewModelScope.launch {
            delay(1100)
            result = if (connection == Connection.ONLINE) session.query(reference) else operation
            refresh()
            processing = false
        }
    }

    fun retryRejected() {
        if (result?.payment != PaymentStatus.REJECTED) return
        beginCheckout()
    }

    fun recoverUnavailable() {
        result?.let { session.recoverUnavailable(it.reference) }
        refresh()
        closeOverlay()
    }

    fun pickerTickets(operationReference: String? = null) =
        if (operationReference == null) availableTickets else session.ticketsForOperation(operationReference)

    fun openTickets(id: String? = null, operationReference: String? = null) {
        if (processing) return
        if (id != null) {
            val targets = if (operationReference == null) records.tickets else pickerTickets(operationReference)
            if (targets.any { it.id == id }) overlay = Overlay.TicketDetail(id, operationReference)
        } else overlay = ticketOverlay(pickerTickets(operationReference).map { it.id }, operationReference)
    }

    fun openOperationTickets(reference: String) = openTickets(operationReference = reference)

    fun qrState(ticket: Ticket) = qrStates[ticket.id] ?: QrRefreshState(ticket)

    fun chooseQrFixture(value: QrRefreshFixture) {
        if (qrStates.values.none { it.status == QrRefreshStatus.LOADING }) qrFixture = value
    }

    fun refreshQr(ticketId: String) {
        val ticket = records.tickets.firstOrNull { it.id == ticketId } ?: return
        val current = qrState(ticket)
        if (!ticket.canShowQr || current.status == QrRefreshStatus.LOADING) return
        val selectedFixture = qrFixture
        val startedOnline = connection == Connection.ONLINE
        qrStates = qrStates + (ticketId to current.begin())
        viewModelScope.launch {
            delay(900)
            qrStates = qrStates + (ticketId to qrStates.getValue(ticketId).finish(
                selectedFixture, online = startedOnline && connection == Connection.ONLINE,
                checkedAt = "7 oct 2026 · ${DemoCatalog.time} (reloj de demo)"))
        }
    }

    private fun refresh() {
        records = SessionRecords(session.lines, session.tickets, session.operations)
    }

    override fun onCleared() {
        networkManager.unregisterNetworkCallback(callback)
    }
}
