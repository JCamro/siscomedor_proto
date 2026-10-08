package com.example.siscomedor.domain

// Los enum limitan estados válidos y guardan los importes en centavos para evitar redondeos acumulados.
enum class Service(val label: String, val hours: String, val saleEnd: String, val cents: Int) {
    BREAKFAST("Desayuno", "07:00–09:30", "09:00", 450),
    LUNCH("Almuerzo", "12:00–15:00", "14:30", 850),
    DINNER("Cena", "18:00–20:30", "20:00", 700)
}

data class Variant(val id: String, val name: String, val description: String, val imageUrl: String)
// La variante no forma parte de la clave: cambiarla reemplaza el menú, no crea otro ticket del mismo servicio.
data class SelectionKey(val date: String, val service: Service)
data class SelectionLine(val date: String, val service: Service, val variant: Variant, val cents: Int) {
    val key get() = SelectionKey(date, service)
}
data class PublishedDate(val date: String, val day: String, val published: Boolean)

// Pago, emisión y uso del ticket son dimensiones distintas: confirmar un pago no garantiza emisión inmediata.
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

// data class con val conserva el contexto de la operación; copy permite resolver estados sin cambiar sus líneas ni billetera.
data class PaymentOperation(
    val reference: String,
    val createdAt: String,
    val lines: List<SelectionLine>,
    val payment: PaymentStatus,
    val issuance: IssuanceStatus,
    val scenario: Scenario,
    val affectedKey: SelectionKey? = null,
    val wallet: String = "Billetera Demo ···· 2841"
) {
    val cents get() = lines.sumOf { it.cents }
    val unresolved get() = payment == PaymentStatus.UNKNOWN ||
        (payment == PaymentStatus.CONFIRMED && issuance == IssuanceStatus.PENDING)
}

data class Ticket(val id: String, val line: SelectionLine, val status: TicketStatus, val issuedAt: String) {
    // El contenido siempre deriva del ticket emitido, nunca de la selección actual; vencidos/consumidos no ofrecen QR.
    val qrContent get() = "FICTICIO|$id|${line.date}|${line.service.name}|${line.variant.id}"
    val canShowQr get() = status == TicketStatus.AVAILABLE
}
