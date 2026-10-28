package com.example.siscomedor.ui.payments

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.siscomedor.domain.DemoSession
import com.example.siscomedor.domain.PaymentStatus
import com.example.siscomedor.ui.theme.SisComeTheme

// Vista previa de Pagos con el historial de demo (confirmado, por verificar y rechazado).
// En Interactive Mode los filtros funcionan porque el filtro vive en un estado local.
@Preview(name = "Pagos · historial", showBackground = true, widthDp = 400, heightDp = 1600)
@Composable
private fun PaymentsHistoryPreview() {
    val operations = remember { DemoSession(includePaymentHistory = true).operations }
    var filter by remember { mutableStateOf<PaymentStatus?>(null) }
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PaymentsContent(operations, filter, onFilterChange = { filter = it }, onOpenDetail = {})
        }
    }
}
