package com.example.siscomedor

import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import com.example.siscomedor.ui.Overlay
import com.example.siscomedor.ui.ticketOverlay
import org.junit.Assert.*
import org.junit.Test

class CorrectionTest {
    @Test fun historicalResultRecoversOnlyItsExpiredTicketWithoutQr() {
        val session = DemoSession(includePaymentHistory = true)
        session.query("DEMO-78355")
        val targets = session.ticketsForOperation("DEMO-78355")
        val recovered = targets.single()
        assertEquals("2026-10-01", recovered.line.date)
        assertEquals(TicketStatus.EXPIRED, recovered.status)
        assertFalse(recovered.canShowQr)
        assertEquals(Overlay.TicketDetail(recovered.id, "DEMO-78355"),
            ticketOverlay(targets.map { it.id }, "DEMO-78355"))
        assertFalse(targets.any { it.id == "SC-2026-1048" })
        assertTrue(session.ticketsForOperation("missing").isEmpty())
        assertTrue(session.ticketsForOperation("DEMO-78310").isEmpty())
    }

    @Test fun multipleOperationTicketsKeepPickerScopeInsteadOfGlobalTargets() {
        val session = DemoSession()
        session.toggle(DemoCatalog.line("2026-10-08", Service.LUNCH, "vegetarian"))
        session.toggle(DemoCatalog.line("2026-10-09", Service.DINNER, "traditional"))
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.SUCCESS)
        val targets = session.ticketsForOperation("DEMO-1")
        assertEquals(snapshot.map { it.key }.toSet(), targets.map { it.line.key }.toSet())
        assertEquals(Overlay.TicketPicker("DEMO-1"), ticketOverlay(targets.map { it.id }, "DEMO-1"))
        assertFalse(targets.any { it.id == "SC-2026-1048" })
    }

    @Test fun everyVariantRetainsItsExactReferencePhoto() {
        val expected = listOf(
            "photo-1619957666910-bf7ba37cda1b", "photo-1623800849430-13c191263e9f",
            "photo-1698931351680-edb46f3ff75e", "photo-1623800849430-13c191263e9f",
            "photo-1755556889666-580731658686", "photo-1599354607446-c0aa31fb6c2e"
        ).map { "https://images.unsplash.com/$it?auto=format&fit=crop&w=320&q=80" }
        assertEquals(expected, Service.entries.flatMap { DemoCatalog.variants(it).map { variant -> variant.imageUrl } })
    }

    @Test fun qrRefreshSuccessKeepsIdentityAndRecordsOnlyDemoConsultation() {
        val ticket = DemoCatalog.tickets.first()
        val loading = QrRefreshState(ticket).begin()
        assertEquals(QrRefreshStatus.LOADING, loading.status)
        assertEquals(ticket.qrContent, loading.ticket.qrContent)
        val result = loading.finish(QrRefreshFixture.SUCCESS, online = true, checkedAt = "7 oct · 16:40")
        assertEquals(QrRefreshStatus.REFRESHED, result.status)
        assertEquals("7 oct · 16:40", result.lastConsultedAt)
        assertEquals(ticket, result.ticket)
    }

    @Test fun qrFailureAndOfflineKeepSavedContentAndLastSuccessfulConsultation() {
        val ticket = DemoCatalog.tickets.first()
        val refreshed = QrRefreshState(ticket).begin()
            .finish(QrRefreshFixture.SUCCESS, online = true, checkedAt = "7 oct · 16:40")
        val failed = refreshed.begin().finish(QrRefreshFixture.FAILURE, online = true, checkedAt = "later")
        assertEquals(QrRefreshStatus.FAILED, failed.status)
        val offline = failed.begin().finish(QrRefreshFixture.OFFLINE, online = true, checkedAt = "later")
        assertEquals(QrRefreshStatus.OFFLINE, offline.status)
        val lostNetwork = refreshed.begin().finish(QrRefreshFixture.SUCCESS, online = false, checkedAt = "later")
        assertEquals(QrRefreshStatus.OFFLINE, lostNetwork.status)
        listOf(failed, offline, lostNetwork).forEach {
            assertEquals(ticket.id, it.ticket.id)
            assertEquals(ticket.qrContent, it.ticket.qrContent)
            assertEquals("7 oct · 16:40", it.lastConsultedAt)
        }
    }

    @Test fun expiredAndConsumedTicketsCannotStartQrRefresh() {
        val expired = DemoCatalog.tickets.first().copy(status = TicketStatus.EXPIRED)
        val consumed = DemoCatalog.tickets.last()
        listOf(expired, consumed).forEach {
            assertFalse(it.canShowQr)
            assertEquals(QrRefreshStatus.SAVED, QrRefreshState(it).begin().status)
        }
    }
}
