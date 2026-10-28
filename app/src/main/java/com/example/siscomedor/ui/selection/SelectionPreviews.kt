package com.example.siscomedor.ui.selection

import androidx.compose.foundation.layout.Box
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
import com.example.siscomedor.domain.SelectionLine
import com.example.siscomedor.domain.Service
import com.example.siscomedor.ui.theme.SisComeTheme

// Vistas previas del resumen de selección (el mismo contenido del panel lateral y de la hoja inferior).
// En Interactive Mode se puede quitar una línea, vaciar la selección y cambiar la billetera.
@Preview(name = "Selección · con servicios", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun SelectionWithLinesPreview() {
    var lines by remember {
        mutableStateOf(listOf(
            DemoCatalog.line("2026-10-08", Service.BREAKFAST, "traditional"),
            DemoCatalog.line("2026-10-08", Service.LUNCH, "vegetarian")
        ))
    }
    var wallet by remember { mutableStateOf(DemoCatalog.wallet) }
    SelectionPreviewFrame(lines, wallet, SelectionActions(
        onRemove = { key -> lines = lines.filter { it.key != key } },
        onClear = { lines = emptyList() },
        onSelectWallet = { wallet = it }
    ))
}

@Preview(name = "Selección · vacía", showBackground = true, widthDp = 360)
@Composable
private fun SelectionEmptyPreview() {
    SelectionPreviewFrame(emptyList(), DemoCatalog.wallet, SelectionActions())
}

@Composable
private fun SelectionPreviewFrame(lines: List<SelectionLine>, wallet: String, actions: SelectionActions) {
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.padding(20.dp)) {
                SelectionSummaryContent(
                    SelectionUiState(lines, emptySet(), emptySet(), wallet, canCheckout = lines.isNotEmpty()),
                    actions
                )
            }
        }
    }
}
