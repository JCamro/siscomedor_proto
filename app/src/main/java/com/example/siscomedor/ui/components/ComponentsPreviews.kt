package com.example.siscomedor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.siscomedor.ui.theme.SisComeTheme

// Vista previa de las cuatro variantes de StatusBadge: disponible, pendiente, neutral y error.
@Preview(name = "StatusBadge · variantes", showBackground = true)
@Composable
private fun StatusBadgePreview() {
    SisComeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge("Disponible para usar")
                StatusBadge("Pago por verificar", pending = true)
                StatusBadge("Consumido", neutral = true)
                StatusBadge("Pago rechazado", error = true)
            }
        }
    }
}
