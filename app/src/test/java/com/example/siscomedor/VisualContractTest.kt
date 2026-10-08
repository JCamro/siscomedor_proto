package com.example.siscomedor

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Source wiring checks, not screenshot or accessibility certification. */
class VisualContractTest {
    private fun source(path: String) = File("src/main/java/com/example/siscomedor/$path").readText()

    @Test fun bundledFontMatchesVerifiedUpstreamBlobAndShipsLicense() {
        val bytes = File("src/main/res/font/inter.ttf").readBytes()
        val blob = "blob ${bytes.size}\u0000".toByteArray() + bytes
        val digest = MessageDigest.getInstance("SHA-1").digest(blob).joinToString("") { "%02x".format(it) }
        assertEquals("4ab79e0102bbe0ffa1ed879b13e52ac8c6487833", digest)
        val license = File("src/main/assets/fonts/OFL.txt").readText()
        assertTrue(license.contains("SIL OPEN FONT LICENSE Version 1.1"))
        assertTrue(license.contains("The Inter Project Authors"))
    }

    @Test fun menuActuallyRendersCatalogPhotos() {
        val home = source("ui/home/HomeScreen.kt")
        val components = source("ui/components/Components.kt")
        assertTrue(home.contains("MenuPhoto(variant.imageUrl"))
        assertTrue(components.contains("SubcomposeAsyncImage("))
        assertTrue(components.contains("ContentScale.Crop"))
        assertTrue(components.contains("loading ="))
        assertTrue(components.contains("error ="))
    }

    @Test fun servicesShareAvailableWidthRatherThanOverflowing() {
        val home = source("ui/home/HomeScreen.kt")
        assertFalse(home.contains("widthIn(min = 128.dp)"))
        assertTrue(home.contains("Modifier.weight(1f)"))
        assertTrue(home.contains("LocalDensity.current.fontScale"))
    }

    @Test fun typographyAndContainerRolesAreExplicit() {
        val theme = source("ui/theme/Theme.kt")
        assertTrue(theme.contains("R.font.inter"))
        assertTrue(theme.contains("typography ="))
        assertTrue(theme.contains("surfaceContainer ="))
        assertTrue(theme.contains("surfaceTint ="))
    }

    @Test fun customLoopsHaveSystemNoMotionBranch() {
        val components = source("ui/components/Components.kt")
        assertTrue(components.contains("MotionDurationScale"))
        assertTrue(components.contains("if (!motionEnabled())"))
    }

    @Test fun confirmationKeepsSimulationDisclosure() {
        val checkout = source("ui/checkout/CheckoutDialog.kt")
        assertTrue(checkout.contains("No se realizará ningún cargo real."))
        assertTrue(checkout.contains("Text(\"Confirmar pago\")"))
        assertFalse(checkout.contains("Confirmar pago simulado"))
        assertTrue(checkout.contains("Opciones de demostración"))
        assertTrue(checkout.contains("FlowRow("))
    }

    @Test fun recentActivityShowsTicketRecordsAndRoutesToTicketHistory() {
        val home = source("ui/home/HomeScreen.kt")
        assertTrue(home.contains("model.records.tickets.sortedByDescending"))
        assertTrue(home.contains("ticket.line.variant.name"))
        assertTrue(home.contains("ticket.line.cents"))
        assertTrue(home.contains("model.openTickets(ticket.id)"))
        assertTrue(home.contains("model.navigate(Destination.TICKETS)"))
        assertFalse(home.contains("model.records.operations.take(2)"))
    }

    @Test fun walletSelectionIsSharedAndOperationDetailUsesFrozenWallet() {
        val checkout = source("ui/checkout/CheckoutDialog.kt")
        val selection = source("ui/selection/SelectionUi.kt")
        val payments = source("ui/payments/PaymentsUi.kt")
        assertTrue(checkout.contains("WalletSummary(model.selectedWallet, model::selectWallet)"))
        assertTrue(selection.contains("WalletSummary(model.selectedWallet, model::selectWallet)"))
        assertTrue(payments.contains("WalletSummary(operation.wallet, null)"))
    }

    @Test fun menuFooterAndServiceMetadataUseReferenceStructure() {
        val home = source("ui/home/HomeScreen.kt")
        assertTrue(home.contains("horizontalArrangement = Arrangement.SpaceBetween"))
        assertTrue(home.contains("Text(if (active) \"Agregado\" else \"Agregar\")"))
        assertTrue(home.contains("Modifier.heightIn(min = 48.dp)"))
        assertTrue(home.contains("AppIcon(\"calendar\""))
        assertTrue(home.contains("AppIcon(\"clock\""))
        assertTrue(home.contains("color = MaterialTheme.colorScheme.surfaceContainerHigh"))
    }

    @Test fun closeControlIsIndependentOfScrollingDialogHeader() {
        val components = source("ui/components/Components.kt")
        assertTrue(components.contains("showTitle: Boolean = true"))
        assertTrue(components.contains("Modifier.align(Alignment.TopEnd).padding(8.dp)"))
        assertTrue(components.contains("padding(end = 52.dp)"))
    }

    @Test fun resultKeepsOperationScopeAndDoesNotDuplicateGenericHeader() {
        val checkout = source("ui/checkout/CheckoutDialog.kt")
        assertTrue(checkout.contains("showTitle = result == null"))
        assertTrue(checkout.contains("Ticket emitido correctamente"))
        assertTrue(checkout.contains("Tickets emitidos correctamente"))
        assertTrue(checkout.contains("model.pickerTickets(operation.reference)"))
        assertFalse(checkout.contains("Cargo simulado confirmado"))
    }
}
