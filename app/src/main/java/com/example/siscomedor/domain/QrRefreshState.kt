package com.example.siscomedor.domain

enum class QrRefreshFixture(val label: String) {
    SUCCESS("Consulta exitosa"), FAILURE("Consulta fallida"), OFFLINE("Sin conexión")
}

enum class QrRefreshStatus { SAVED, LOADING, REFRESHED, FAILED, OFFLINE }

// Consultar cambia el estado local, no el ticket ni el contenido del QR; no valida credenciales institucionales.
data class QrRefreshState(
    val ticket: Ticket,
    val status: QrRefreshStatus = QrRefreshStatus.SAVED,
    val lastConsultedAt: String? = null
) {
    // copy conserva identidad y última consulta mientras carga; un registro de solo lectura no inicia la consulta.
    fun begin() = if (ticket.canShowQr) copy(status = QrRefreshStatus.LOADING) else this

    fun finish(fixture: QrRefreshFixture, online: Boolean, checkedAt: String): QrRefreshState {
        if (!ticket.canShowQr || status != QrRefreshStatus.LOADING) return this
        // Sin red prevalece sobre el escenario elegido. Error y desconexión no borran la última consulta exitosa.
        return when {
            !online || fixture == QrRefreshFixture.OFFLINE -> copy(status = QrRefreshStatus.OFFLINE)
            fixture == QrRefreshFixture.FAILURE -> copy(status = QrRefreshStatus.FAILED)
            else -> copy(status = QrRefreshStatus.REFRESHED, lastConsultedAt = checkedAt)
        }
    }
}
