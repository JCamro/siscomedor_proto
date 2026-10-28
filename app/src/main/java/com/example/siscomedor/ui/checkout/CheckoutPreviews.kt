package com.example.siscomedor.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.siscomedor.data.DemoCatalog
import com.example.siscomedor.domain.Scenario
import com.example.siscomedor.domain.Service
import com.example.siscomedor.ui.CheckoutReview
import com.example.siscomedor.ui.Connection
import com.example.siscomedor.ui.theme.SisComeTheme

// Vistas previas del paso "Revisa antes de confirmar". Se muestran dentro de una columna con el mismo
// espaciado que AppDialog, sin abrir un Dialog real. En Interactive Mode se puede cambiar escenario y billetera.
@Composable
private fun CheckoutReviewPreviewFrame(connection: Connection) {
    val review = remember {
        CheckoutReview("DEMO-78421", listOf(
            DemoCatalog.line("2026-10-08", Service.BREAKFAST, "traditional"),
            DemoCatalog.line("2026-10-09", Service.LUNCH, "vegetarian")
        ))
    }
    var scenario by remember { mutableStateOf(Scenario.SUCCESS) }
    var wallet by remember { mutableStateOf(DemoCatalog.wallet) }
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CheckoutReviewContent(
                    review = review,
                    selectedWallet = wallet,
                    scenario = scenario,
                    notice = null,
                    connection = connection,
                    onSelectWallet = { wallet = it },
                    onChooseScenario = { scenario = it },
                    onConfirm = {}
                )
            }
        }
    }
}

@Preview(name = "Pago · revisión en línea", showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
private fun CheckoutReviewOnlinePreview() {
    CheckoutReviewPreviewFrame(Connection.ONLINE)
}

@Preview(name = "Pago · revisión sin conexión", showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
private fun CheckoutReviewOfflinePreview() {
    CheckoutReviewPreviewFrame(Connection.OFFLINE)
}
