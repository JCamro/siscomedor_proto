package com.example.siscomedor

import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.*
import org.junit.Assert.*
import org.junit.Test

class DemoSessionTest {
    private fun line(date: String = "2026-10-08", service: Service = Service.LUNCH, variant: String = "traditional") =
        DemoCatalog.line(date, service, variant)

    @Test fun variantReplacementAndToggleNeverAddQuantity() {
        val session = DemoSession()
        session.toggle(line())
        session.toggle(line(variant = "vegetarian"))
        assertEquals(1, session.selection.size)
        assertEquals("vegetarian", session.selection.values.single().variant.id)
        session.toggle(line(variant = "vegetarian"))
        assertTrue(session.selection.isEmpty())
    }

    @Test fun fullDateAndServiceDefineUniquenessNotVariant() {
        val session = DemoSession()
        session.toggle(line())
        session.toggle(line(service = Service.DINNER))
        session.toggle(line(date = "2026-10-09"))
        assertEquals(3, session.selection.size)
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.SUCCESS)
        session.toggle(line(variant = "vegetarian"))
        assertTrue(session.selection.isEmpty())
        session.submit("DEMO-1", snapshot, Scenario.SUCCESS)
        assertEquals(1, session.operations.size)
        assertEquals(3, session.tickets.count { it.line.key in snapshot.map { l -> l.key } })
    }

    @Test fun unknownPaymentLocksKeysAndQueryIsIdempotent() {
        val session = DemoSession()
        session.toggle(line())
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.UNKNOWN)
        session.toggle(line(variant = "vegetarian"))
        session.remove(line().key)
        assertEquals(snapshot, session.lines)
        assertFalse(session.canSubmit(snapshot))
        session.submit("DEMO-2", snapshot, Scenario.SUCCESS)
        assertEquals(1, session.operations.size)
        session.query("DEMO-1")
        session.query("DEMO-1")
        assertEquals(1, session.operations.size)
        assertEquals(PaymentStatus.CONFIRMED, session.operations.single().payment)
        assertEquals(1, session.tickets.count { it.line.key == line().key })
    }

    @Test fun confirmedPaymentCanHavePendingIssuance() {
        val session = DemoSession()
        session.toggle(line())
        session.submit("DEMO-1", session.lines, Scenario.ISSUANCE)
        assertEquals(PaymentStatus.CONFIRMED, session.operations.single().payment)
        assertEquals(IssuanceStatus.PENDING, session.operations.single().issuance)
        assertFalse(session.tickets.any { it.line.key == line().key })
        session.query("DEMO-1")
        assertEquals(IssuanceStatus.COMPLETED, session.operations.single().issuance)
    }

    @Test fun queryUsesFrozenSnapshotAndPreservesUnrelatedSelection() {
        val session = DemoSession()
        session.toggle(line(variant = "vegetarian"))
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.UNKNOWN)
        session.toggle(line(date = "2026-10-09", service = Service.DINNER))
        session.query("DEMO-1")
        assertEquals(snapshot, session.operations.single().lines)
        assertEquals("vegetarian", session.tickets.single { it.line.key == line().key }.line.variant.id)
        assertEquals("2026-10-09", session.lines.single().date)
    }

    @Test fun laterPurchasesRetainEarlierVariantsAndGetDistinctReferences() {
        val session = DemoSession()
        session.toggle(line(variant = "vegetarian"))
        session.submit("DEMO-1", session.lines, Scenario.SUCCESS)
        session.toggle(line(date = "2026-10-09"))
        session.submit("DEMO-2", session.lines, Scenario.SUCCESS)
        assertEquals(2, session.operations.size)
        assertEquals("vegetarian", session.tickets.single { it.line.key == line().key }.line.variant.id)
    }

    @Test fun operationKeepsSelectedWalletWhenQueried() {
        val session = DemoSession()
        session.toggle(line())
        val selectedWallet = "Billetera Demo ···· 7390"
        session.submit("DEMO-1", session.lines, Scenario.UNKNOWN, selectedWallet)
        assertEquals(selectedWallet, session.operations.single().wallet)
        session.query("DEMO-1")
        assertEquals(selectedWallet, session.operations.single().wallet)
    }

    @Test fun soldOutRecoveryIsExplicitAndNeverCharges() {
        val session = DemoSession()
        session.toggle(line())
        session.toggle(line(service = Service.DINNER))
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.SOLD_OUT)
        assertEquals(snapshot, session.lines)
        assertEquals(PaymentStatus.NOT_STARTED, session.operations.single().payment)
        session.recoverUnavailable("DEMO-1")
        assertEquals(1, session.selection.size)
        assertEquals(snapshot.first(), session.lines.single())
    }

    @Test fun soldOutKeyCannotBePaidAgainAfterClosingResult() {
        val session = DemoSession()
        session.toggle(line())
        val snapshot = session.lines
        session.submit("DEMO-1", snapshot, Scenario.SOLD_OUT)
        assertFalse(session.canSubmit(snapshot))
        assertNull(session.submit("DEMO-2", snapshot, Scenario.SUCCESS))
        session.recoverUnavailable("DEMO-1")
        session.toggle(line(variant = "vegetarian"))
        assertTrue(session.selection.isEmpty())
    }

    @Test fun resolvingHistoricalPendingOperationDoesNotCreateUsablePastTicket() {
        val session = DemoSession(includePaymentHistory = true)
        session.query("DEMO-78355")
        val ticket = session.tickets.single { it.line.date == "2026-10-01" }
        assertEquals(TicketStatus.EXPIRED, ticket.status)
    }

    @Test fun unpublishedClosedAndConsumedKeysAreNotEligible() {
        val session = DemoSession()
        session.toggle(line(date = "2026-10-10"))
        session.toggle(line(date = "2026-10-07", service = Service.BREAKFAST))
        session.toggle(line(date = "2026-10-02"))
        assertTrue(session.selection.isEmpty())
    }

    @Test fun demoQrRetainsTicketIdentityAndHasStableCells() {
        val ticket = DemoCatalog.tickets.first()
        val reopened = ticket.copy()
        assertEquals(ticket.qrContent, reopened.qrContent)
        val cells = (0 until DemoQr.size).flatMap { y -> (0 until DemoQr.size).map { x -> DemoQr.cell(ticket.qrContent, x, y) } }
        val repeated = (0 until DemoQr.size).flatMap { y -> (0 until DemoQr.size).map { x -> DemoQr.cell(reopened.qrContent, x, y) } }
        assertEquals(cells, repeated)
        assertTrue(cells.any { it })
        assertTrue(cells.any { !it })
        assertNotEquals(ticket.qrContent, ticket.copy(id = "SC-2026-9999").qrContent)
    }
}
