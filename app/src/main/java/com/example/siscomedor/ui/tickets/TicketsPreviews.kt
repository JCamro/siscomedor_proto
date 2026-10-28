package com.example.siscomedor.ui.tickets

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.siscomedor.domain.DemoSession
import com.example.siscomedor.domain.TicketStatus
import com.example.siscomedor.ui.theme.SisComeTheme

// Vista previa de Mis tickets con los registros de DemoSession (sin red ni ViewModel).
// En Interactive Mode los filtros funcionan porque el filtro vive en un estado local.
@Preview(name = "Mis tickets", showBackground = true, widthDp = 400, heightDp = 1400)
@Composable
private fun TicketsListPreview() {
    val tickets = remember { DemoSession().tickets }
    var filter by remember { mutableStateOf<TicketStatus?>(null) }
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TicketsContent(tickets, filter, onFilterChange = { filter = it }, onOpenTicket = {})
        }
    }
}
