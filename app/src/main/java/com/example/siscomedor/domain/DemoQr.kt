package com.example.siscomedor.domain

// Patrón visual estable de demostración: no codifica un QR real ni acredita ingreso institucional.
object DemoQr {
    const val size = 21

    fun cell(content: String, x: Int, y: Int): Boolean {
        // Los tres cuadrados orientan visualmente el patrón; pantalla y PNG llaman a esta misma función.
        val finder = when {
            x < 7 && y < 7 -> x to y
            x >= size - 7 && y < 7 -> (x - size + 7) to y
            x < 7 && y >= size - 7 -> x to (y - size + 7)
            else -> null
        }
        if (finder != null) {
            val (localX, localY) = finder
            return localX == 0 || localX == 6 || localY == 0 || localY == 6 ||
                (localX in 2..4 && localY in 2..4)
        }
        // La mezcla determinista mantiene las celdas al reabrir o consultar el mismo ticket, sin aleatoriedad nueva.
        val mixed = content.hashCode() xor (x * 73856093) xor (y * 19349663)
        return Integer.bitCount(mixed) % 2 == 0
    }
}
