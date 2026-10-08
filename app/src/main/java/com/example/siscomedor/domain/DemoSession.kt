package com.example.siscomedor.domain

import com.example.siscomedor.data.DemoCatalog

class DemoSession(includePaymentHistory: Boolean = false) {
    // Esta clase no conoce Compose: aplica reglas sobre copias de colecciones y puede probarse sin dispositivo.
    var selection: Map<SelectionKey, SelectionLine> = emptyMap()
        private set
    var operations: List<PaymentOperation> = if (includePaymentHistory) DemoCatalog.payments else emptyList()
        private set
    var tickets: List<Ticket> = DemoCatalog.tickets
        private set
    private var nextTicket = 1049
    val lines get() = selection.values.sortedWith(compareBy({ it.date }, { it.service.ordinal }))

    fun pendingFor(key: SelectionKey) = operations.firstOrNull { it.unresolved && it.lines.any { l -> l.key == key } }
    fun owned(key: SelectionKey) = tickets.firstOrNull { it.line.key == key }
    fun locked(key: SelectionKey) = owned(key) != null || pendingFor(key) != null
    fun unavailable(key: SelectionKey) = operations.any { it.payment == PaymentStatus.NOT_STARTED && it.affectedKey == key }

    // Recupera únicamente las claves de la operación; evita abrir tickets ajenos desde un resultado histórico.
    fun ticketsForOperation(reference: String): List<Ticket> {
        val operation = operations.firstOrNull { it.reference == reference } ?: return emptyList()
        if (operation.issuance != IssuanceStatus.COMPLETED) return emptyList()
        val keys = operation.lines.map { it.key }.toSet()
        return tickets.filter { it.line.key in keys }
    }

    fun toggle(line: SelectionLine) {
        if (!DemoCatalog.eligible(line) || locked(line.key) || unavailable(line.key)) return
        // + y - crean mapas nuevos: misma variante quita, otra variante reemplaza, sin cantidades duplicadas.
        selection = if (selection[line.key]?.variant?.id == line.variant.id) selection - line.key
        else selection + (line.key to line)
    }

    fun remove(key: SelectionKey) {
        if (pendingFor(key) == null) selection = selection - key
    }

    // Vaciar nunca borra líneas pendientes: deben conservarse para consultar el pago original con seguridad.
    fun clear() {
        selection = selection.filterKeys { pendingFor(it) != null }
    }

    fun canSubmit(snapshot: List<SelectionLine>) = snapshot.isNotEmpty() &&
        snapshot.map { it.key }.distinct().size == snapshot.size &&
        snapshot.all { DemoCatalog.eligible(it) && !locked(it.key) && !unavailable(it.key) && selection[it.key] == it }

    // La referencia es idempotente; primero se busca una operación existente para no cobrar ni emitir dos veces.
    fun submit(reference: String, snapshot: List<SelectionLine>, scenario: Scenario, wallet: String = DemoCatalog.wallet): PaymentOperation? {
        operations.firstOrNull { it.reference == reference }?.let { return it }
        if (!canSubmit(snapshot)) return null
        val payment = when (scenario) {
            Scenario.SUCCESS, Scenario.ISSUANCE -> PaymentStatus.CONFIRMED
            Scenario.UNKNOWN -> PaymentStatus.UNKNOWN
            Scenario.REJECTED -> PaymentStatus.REJECTED
            Scenario.SOLD_OUT -> PaymentStatus.NOT_STARTED
        }
        val issuance = when (scenario) {
            Scenario.SUCCESS -> IssuanceStatus.COMPLETED
            Scenario.ISSUANCE -> IssuanceStatus.PENDING
            else -> IssuanceStatus.NOT_STARTED
        }
        // toList congela las líneas revisadas junto con la billetera; consultas posteriores no usan el carrito nuevo.
        val operation = PaymentOperation(reference, "7 oct 2026 · ${DemoCatalog.time}", snapshot.toList(), payment, issuance, scenario,
            snapshot.last().key.takeIf { scenario == Scenario.SOLD_OUT }, wallet)
        operations = listOf(operation) + operations
        if (issuance == IssuanceStatus.COMPLETED) issue(operation)
        return operation
    }

    fun query(reference: String): PaymentOperation? {
        val operation = operations.firstOrNull { it.reference == reference } ?: return null
        if (!operation.unresolved) return operation
        // copy modifica solo los estados. map reemplaza la misma referencia, manteniendo historial y contexto originales.
        val resolved = operation.copy(payment = PaymentStatus.CONFIRMED, issuance = IssuanceStatus.COMPLETED)
        operations = operations.map { if (it.reference == reference) resolved else it }
        issue(resolved)
        return resolved
    }

    fun recoverUnavailable(reference: String) {
        val operation = operations.firstOrNull { it.reference == reference } ?: return
        if (operation.payment == PaymentStatus.NOT_STARTED) operation.affectedKey?.let { remove(it) }
    }

    // Una emisión tardía de fecha pasada queda vencida; no genera un código utilizable ni libera claves compradas.
    private fun issue(operation: PaymentOperation) {
        val newTickets = operation.lines.filter { owned(it.key) == null }.map {
            Ticket("SC-2026-${nextTicket++}", it,
                if (it.date < DemoCatalog.today) TicketStatus.EXPIRED else TicketStatus.AVAILABLE, operation.createdAt)
        }
        tickets = tickets + newTickets
        // Solo se retiran las líneas de esta operación: servicios agregados después permanecen seleccionados.
        selection = selection - operation.lines.map { it.key }.toSet()
    }
}
