package com.example.siscomedor.domain

enum class Service(val label: String, val hours: String, val saleEnd: String, val cents: Int) {
    BREAKFAST("Desayuno", "07:00–09:30", "09:00", 450),
    LUNCH("Almuerzo", "12:00–15:00", "14:30", 850),
    DINNER("Cena", "18:00–20:30", "20:00", 700)
}

data class Variant(val id: String, val name: String, val description: String, val imageUrl: String)
data class SelectionKey(val date: String, val service: Service)
data class SelectionLine(val date: String, val service: Service, val variant: Variant, val cents: Int) {
    val key get() = SelectionKey(date, service)
}
data class PublishedDate(val date: String, val day: String, val published: Boolean)

enum class PaymentStatus(val label: String) {
    NOT_STARTED("Pago no iniciado"), CONFIRMED("Pago confirmado"),
    UNKNOWN("Pago por verificar"), REJECTED("Pago rechazado")
}
enum class IssuanceStatus(val label: String) {
    NOT_STARTED("Sin emisión"), PENDING("Emisión pendiente"), COMPLETED("Tickets emitidos")
}
enum class TicketStatus(val label: String) {
    AVAILABLE("Disponible para usar"), CONSUMED("Consumido"), EXPIRED("Vencido")
}
enum class Scenario(val label: String, val hint: String) {
    SUCCESS("Éxito", "Pago simulado confirmado y tickets emitidos."),
    UNKNOWN("Pago por verificar", "No se sabe si hubo cargo. Consulta la misma referencia."),
    ISSUANCE("Emisión pendiente", "Pago confirmado; todavía no se emitieron tickets."),
    REJECTED("Pago rechazado", "Sin cargo. El reintento es solo un caso de demostración."),
    SOLD_OUT("Sin disponibilidad", "Un menú se agota durante la verificación, antes del pago.")
}

data class PaymentOperation(
    val reference: String,
    val createdAt: String,
    val lines: List<SelectionLine>,
    val payment: PaymentStatus,
    val issuance: IssuanceStatus,
    val scenario: Scenario,
    val affectedKey: SelectionKey? = null
) {
    val cents get() = lines.sumOf { it.cents }
    val unresolved get() = payment == PaymentStatus.UNKNOWN ||
        (payment == PaymentStatus.CONFIRMED && issuance == IssuanceStatus.PENDING)
}

data class Ticket(val id: String, val line: SelectionLine, val status: TicketStatus, val issuedAt: String) {
    val qrContent get() = "FICTICIO|$id|${line.date}|${line.service.name}|${line.variant.id}"
    val canShowQr get() = status == TicketStatus.AVAILABLE
}
