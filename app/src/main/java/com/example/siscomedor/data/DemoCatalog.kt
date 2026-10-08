package com.example.siscomedor.data

import com.example.siscomedor.domain.*

object DemoCatalog {
    // Fechas ISO y reloj fijo hacen reproducibles los escenarios; no representan disponibilidad en tiempo real.
    const val today = "2026-10-07"
    const val time = "16:40"
    const val wallet = "Billetera Demo ···· 2841"
    val wallets = listOf(wallet, "Billetera Demo ···· 7390")
    const val student = "Antonia Muñoz"
    val dates = listOf(
        PublishedDate(today, "HOY", true),
        PublishedDate("2026-10-08", "JUE", true),
        PublishedDate("2026-10-09", "VIE", true),
        PublishedDate("2026-10-10", "SÁB", false),
        PublishedDate("2026-10-12", "LUN", true)
    )
    // when enumera cada servicio; las variantes y fotos son ejemplos, sin consulta institucional.
    fun variants(service: Service): List<Variant> = when (service) {
        Service.BREAKFAST -> listOf(
            Variant("traditional", "Desayuno clásico", "Pan con pollo, fruta de estación y bebida caliente",
                photo("photo-1619957666910-bf7ba37cda1b")),
            Variant("vegetarian", "Desayuno vegetariano", "Pan con palta, fruta de estación y bebida caliente",
                photo("photo-1623800849430-13c191263e9f"))
        )
        Service.LUNCH -> listOf(
            Variant("traditional", "Menú tradicional", "Pollo guisado, arroz primavera y ensalada fresca",
                photo("photo-1698931351680-edb46f3ff75e")),
            Variant("vegetarian", "Menú vegetariano", "Pastel de choclo con proteína vegetal y ensalada fresca",
                photo("photo-1623800849430-13c191263e9f"))
        )
        Service.DINNER -> listOf(
            Variant("traditional", "Cena tradicional", "Arroz con pollo, verduras salteadas y fruta",
                photo("photo-1755556889666-580731658686")),
            Variant("vegetarian", "Cena vegetariana", "Arroz con verduras, menestra y fruta",
                photo("photo-1599354607446-c0aa31fb6c2e"))
        )
    }
    private fun photo(id: String) = "https://images.unsplash.com/$id?auto=format&fit=crop&w=320&q=80"
    fun line(date: String, service: Service, variantId: String) =
        SelectionLine(date, service, variants(service).first { it.id == variantId }, service.cents)

    fun published(date: String) = dates.any { it.date == date && it.published }
    fun closed(key: SelectionKey) = key.date == today && time >= key.service.saleEnd
    // La elegibilidad comprueba publicación, horario, variante y precio antes de aceptar una línea.
    fun eligible(line: SelectionLine) = published(line.date) && !closed(line.key) &&
        line.variant in variants(line.service) && line.cents == line.service.cents

    // El historial inicial es independiente del carrito y se restaura al reiniciar el proceso.
    val tickets = listOf(
        Ticket("SC-2026-1048", line(today, Service.LUNCH, "traditional"), TicketStatus.AVAILABLE, "7 oct 2026 · 11:42"),
        Ticket("SC-2026-0924", line("2026-10-02", Service.LUNCH, "traditional"), TicketStatus.CONSUMED, "2 oct 2026 · 11:48")
    )
    val payments = listOf(
        PaymentOperation("DEMO-78392", "2 oct 2026 · 11:48", listOf(line("2026-10-02", Service.LUNCH, "traditional")), PaymentStatus.CONFIRMED, IssuanceStatus.COMPLETED, Scenario.SUCCESS),
        PaymentOperation("DEMO-78355", "1 oct 2026 · 18:06", listOf(line("2026-10-01", Service.DINNER, "vegetarian")), PaymentStatus.UNKNOWN, IssuanceStatus.NOT_STARTED, Scenario.UNKNOWN),
        PaymentOperation("DEMO-78310", "30 sep 2026 · 08:14", listOf(line("2026-09-30", Service.BREAKFAST, "traditional")), PaymentStatus.REJECTED, IssuanceStatus.NOT_STARTED, Scenario.REJECTED)
    )
}
